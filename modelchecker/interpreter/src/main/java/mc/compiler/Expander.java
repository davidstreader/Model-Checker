package mc.compiler;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import mc.compiler.ast.*;
import mc.compiler.iterator.IndexIterator;
import mc.exceptions.CompilationException;
import mc.util.Location;
import mc.util.LogAST;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;
import mc.util.expr.ExpressionPrinter;
import mc.util.expr.VariableCollector;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Expander {

    private final Pattern VAR_PATTERN = Pattern.compile("\\$[a-z][a-zA-Z0-9_]*");

    private Map<String, Expr> globalVariableMap;
    private ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private Map<String,List<String>> identMap = new HashMap<>();
    private Set<String> hiddenVariables = new HashSet<>();
    public AbstractSyntaxTree expand(AbstractSyntaxTree ast, BlockingQueue<Object> messageQueue, Context context) throws CompilationException, InterruptedException {
        globalVariableMap = ast.getVariableMap();

        List<ProcessNode> processes = ast.getProcesses();
        for (ProcessNode process : processes) {
            expand(process, messageQueue, context);
        }

        List<OperationNode> operations = ast.getOperations();
        for (OperationNode operation : operations) {
            Map<String, Object> variableMap = new HashMap<>();
            ASTNode process1 = expand(operation.getFirstProcess(), variableMap, context);
            ASTNode process2 = expand(operation.getSecondProcess(), variableMap, context);

            operation.setFirstProcess(process1);
            operation.setSecondProcess(process2);
        }

        return ast;
    }
    public ProcessNode expand(ProcessNode process, BlockingQueue<Object> messageQueue, Context context) throws CompilationException, InterruptedException {
        messageQueue.add(new LogAST("Expanding:",process));
        identMap.clear();
        if (process.hasVariableSet())
            hiddenVariables = process.getVariables().getVariables();
        else hiddenVariables = Collections.emptySet();
        Map<String, Object> variableMap = new HashMap<>();
        for (LocalProcessNode node : process.getLocalProcesses()) {
            identMap.put(node.getIdentifier(),new ArrayList<>());
            if (node.getRanges() != null) {
                for (IndexNode in : node.getRanges().getRanges()) {
                    identMap.get(node.getIdentifier()).add(in.getVariable());
                }
            }
        }
        ASTNode root = expand(process.getProcess(), variableMap, context);
        process.setProcess(root);
        List<LocalProcessNode> localProcesses = expandLocalProcesses(process.getLocalProcesses(), variableMap, context);
        process.setLocalProcesses(localProcesses);
        return process;
    }
    private List<LocalProcessNode> expandLocalProcesses(List<LocalProcessNode> localProcesses, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        List<LocalProcessNode> newLocalProcesses = new ArrayList<>();
        for (LocalProcessNode localProcess : localProcesses) {
            if (localProcess.getRanges() == null) {
                ASTNode root = expand(localProcess.getProcess(), variableMap, context);
                localProcess.setProcess(root);
                newLocalProcesses.add(localProcess);
            } else {
                newLocalProcesses.addAll(expandLocalProcesses(localProcess, variableMap, localProcess.getRanges().getRanges(), 0, context));
            }
        }

        return newLocalProcesses;
    }

    private List<LocalProcessNode> expandLocalProcesses(LocalProcessNode localProcess, Map<String, Object> variableMap, List<IndexNode> ranges, int index, Context context) throws CompilationException, InterruptedException {
        List<LocalProcessNode> newLocalProcesses = new ArrayList<>();
        if(index < ranges.size()){
            IndexNode range = ranges.get(index);
            IndexIterator iterator = IndexIterator.construct(expand(range,context));
            String variable = range.getVariable();
            if (!hiddenVariables.contains(variable.substring(1))) {
                localProcess.setIdentifier(localProcess.getIdentifier() + "[" + variable + "]");
                while(iterator.hasNext()){
                    variableMap.put(variable, iterator.next());
                    newLocalProcesses.addAll(expandLocalProcesses((LocalProcessNode) localProcess.copy(), variableMap, ranges, index + 1,context));
                }
            } else {
                localProcess.setIdentifier(localProcess.getIdentifier() + "[" + variable + "]");
                newLocalProcesses.addAll(expandLocalProcesses((LocalProcessNode) localProcess.copy(), variableMap, ranges, index + 1,context));
            }
        }
        else{
            LocalProcessNode clone = (LocalProcessNode)localProcess.copy();
            ASTNode root = expand(clone.getProcess(), variableMap, context);
            clone.setIdentifier(processVariables(clone.getIdentifier(), variableMap, clone.getLocation(),context));
            clone.setProcess(root);
            newLocalProcesses.add(clone);
        }

        return newLocalProcesses;
    }

    private ASTNode expand(ASTNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        if(astNode instanceof ProcessRootNode){
            astNode = expand((ProcessRootNode)astNode, variableMap, context);
        }
        else if(astNode instanceof ActionLabelNode){
            astNode = expand((ActionLabelNode)astNode, variableMap, context);
        }
        else if(astNode instanceof IndexNode){
            astNode = expand((IndexNode)astNode, variableMap, context);
        }
        else if(astNode instanceof SequenceNode){
            astNode = expand((SequenceNode)astNode, variableMap, context);
        }
        else if(astNode instanceof ChoiceNode){
            astNode = expand((ChoiceNode)astNode, variableMap, context);
        }
        else if(astNode instanceof CompositeNode){
            astNode = expand((CompositeNode)astNode, variableMap, context);
        }
        else if(astNode instanceof IfStatementNode){
            astNode = expand((IfStatementNode)astNode, variableMap, context);
        }
        else if(astNode instanceof FunctionNode){
            astNode = expand((FunctionNode)astNode, variableMap, context);
        }
        else if(astNode instanceof IdentifierNode){
            astNode = expand((IdentifierNode)astNode, variableMap, context);
        }
        else if(astNode instanceof ForAllStatementNode){
            astNode = expand((ForAllStatementNode)astNode, variableMap, context);
        }
        //Create a temporary variable map that does not contain hidden variables and store it.
        HashMap<String,Object> tmpVarMap = new HashMap<>(variableMap);
        tmpVarMap.keySet().removeIf(s -> hiddenVariables.contains(s.substring(1)));
        astNode.setModelVariables(tmpVarMap);
        return astNode;
    }

    private ASTNode expand(ProcessRootNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        if(astNode.hasLabel()) {
            astNode.setLabel(processVariables(astNode.getLabel(), variableMap, astNode.getLocation(),context));
        }
        ASTNode process = expand(astNode.getProcess(), variableMap, context);
        astNode.setProcess(process);

        if(astNode.hasRelabelSet()){
            astNode.setRelabelSet(expand(astNode.getRelabelSet(),context));
        }

        if(astNode.hasHiding()){
            HidingNode hiding = astNode.getHiding();
            hiding.setSet(expand(hiding.getSet(),context));
            astNode.setHiding(hiding);
        }

        return astNode;
    }

    private ActionLabelNode expand(ActionLabelNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        String action = processVariables(astNode.getAction(), variableMap, astNode.getLocation(),context);
        astNode.setAction(action);
        return astNode;
    }

    private ASTNode expand(IndexNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        IndexIterator iterator = IndexIterator.construct(expand(astNode,context));
        Stack<ASTNode> iterations = new Stack<>();
        while(iterator.hasNext()){
            Object element = iterator.next();
            variableMap.put(astNode.getVariable(), element);
            iterations.push(expand(astNode.getProcess().copy(), variableMap, context));
        }

        ASTNode node = iterations.pop();
        while(!iterations.isEmpty()){
            ASTNode nextNode = iterations.pop();
            node = new ChoiceNode(nextNode, node, astNode.getLocation());
        }

        return node;
    }

    private ASTNode expand(IndexNode astNode, Context context) throws CompilationException, InterruptedException {
        // expand out nested indices
        ASTNode range = astNode;
        while(range instanceof IndexNode){
            range = ((IndexNode)range).getRange();
        }

        //if the range is a set then it may need expanding
        if(range instanceof SetNode){
            range = expand((SetNode)range, context);
        }

        return range;
    }

    private SequenceNode expand(SequenceNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        //add a guard to every sequenceNode. This will only contain next data.
        Guard guard = new Guard();
        if (astNode.getTo() instanceof IdentifierNode) {
            //Parse the next values from this IdentifierNode
            guard.parseNext(((IdentifierNode) astNode.getTo()).getIdentifier(), globalVariableMap, identMap,astNode.getTo().getLocation());
            //There were next values, so assign to the metadata
            if (guard.hasData())
                astNode.setGuard(guard);
        }
        ActionLabelNode from = expand(astNode.getFrom(), variableMap, context);
        ASTNode to = expand(astNode.getTo(), variableMap, context);
        astNode.setFrom(from);
        astNode.setTo(to);
        return astNode;
    }

    private ASTNode expand(ChoiceNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        ASTNode process1 = expand(astNode.getFirstProcess(), variableMap, context);
        ASTNode process2 = expand(astNode.getSecondProcess(), variableMap, context);

        // check if either one of the branches is empty
        if(process1 instanceof EmptyNode || process1 instanceof TerminalNode){
            return process2;
        }
        else if(process2 instanceof EmptyNode || process2 instanceof TerminalNode){
            return process1;
        }

        astNode.setFirstProcess(process1);
        astNode.setSecondProcess(process2);
        return astNode;
    }

    private ASTNode expand(CompositeNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        ASTNode process1 = expand(astNode.getFirstProcess(), variableMap, context);
        ASTNode process2 = expand(astNode.getSecondProcess(), variableMap, context);

        // check if either one of the branches is empty
        if(process1 instanceof EmptyNode || process1 instanceof TerminalNode){
            return process2;
        }
        else if(process2 instanceof EmptyNode || process2 instanceof TerminalNode){
            return process1;
        }

        astNode.setFirstProcess(process1);
        astNode.setSecondProcess(process2);
        return astNode;
    }

    private ASTNode expand(IfStatementNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        VariableCollector collector = new VariableCollector();
        Map<String,Integer> vars = collector.getVariables(astNode.getCondition(),variableMap);
        Guard trueGuard = new Guard(astNode.getCondition(),vars,hiddenVariables);
        Guard falseGuard = new Guard(astNode.getCondition(),vars,hiddenVariables);
        ASTNode trueBranch = expand(astNode.getTrueBranch(), variableMap, context);
        if (trueBranch.getGuard() != null)
            trueGuard.mergeWith((Guard)trueBranch.getGuard());

        trueBranch.setGuard(trueGuard);
        ASTNode falseBranch = null;

        if (astNode.hasFalseBranch()) {
            falseBranch = expand(astNode.getFalseBranch(), variableMap, context);
            if (falseBranch.getGuard() != null)
                falseGuard.mergeWith((Guard)falseBranch.getGuard());
            falseBranch.setGuard(falseGuard);
        }
        //Check if there are any hidden variables inside both the variableMap and the expression
        if (vars.keySet().stream().map(s -> s.substring(1)).anyMatch(s -> hiddenVariables.contains(s))) {
            if (astNode.hasFalseBranch()) {
                return new ChoiceNode(trueBranch, falseBranch, astNode.getLocation());
            } else {
                return trueBranch;
            }
        }
        //Collect all hidden variables, including variables that aren't in variableMap.
        vars = collector.getVariables(astNode.getCondition(), hiddenVariables.stream().collect(Collectors.toMap(s->"$"+s,s->0)));
        boolean hiddenVariableFound = vars.keySet().stream().map(s -> s.substring(1)).anyMatch(s -> hiddenVariables.contains(s));
        if(evaluateCondition(astNode.getCondition(), variableMap, context)){
            //If a hidden variable is found in the current expression
            if (astNode.hasFalseBranch() && hiddenVariableFound) {
                ASTNode falseBranch2 = astNode.getFalseBranch();
                //See if we can find an else with no if tied to it
                while (falseBranch2 instanceof IfStatementNode) {
                    vars = collector.getVariables(((IfStatementNode) falseBranch2).getCondition(), hiddenVariables.stream().collect(Collectors.toMap(s->"$"+s,s->0)));
                    if(vars.keySet().stream().map(s -> s.substring(1)).anyMatch(s -> hiddenVariables.contains(s))) break;
                    falseBranch2 = ((IfStatementNode) falseBranch2).getFalseBranch();
                }
                //One was found, we must include it as it is possible for there to be hidden variables that can go through that branch.
                if (falseBranch2 != null) {
                    return new ChoiceNode(trueBranch, falseBranch, astNode.getLocation());
                }
            }
            return trueBranch;
        }
        else if(astNode.hasFalseBranch()){
            return falseBranch;
        }
        return new EmptyNode();
    }

    private FunctionNode expand(FunctionNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        ASTNode process = expand(astNode.getProcess(), variableMap, context);
        if (astNode.getReferences() != null) {
            Set<String> unReplacements = (Set<String>) astNode.getReplacements();
            HashMap<String, Expr> replacements = new HashMap<>();
            for (String str : unReplacements) {
                String var = str.substring(0, str.indexOf('='));
                String exp = str.substring(str.indexOf('=') + 1);
                Expr expression;
                if (globalVariableMap.containsKey(exp)) {
                    expression = globalVariableMap.get(exp);
                } else {
                    expression = Expression.constructExpression(exp,astNode.getLocation(), context);
                }
                replacements.put("$"+var, expression);
            }
            astNode.setReplacements(replacements);
        }
        astNode.setProcess(process);
        return astNode;
    }


    private IdentifierNode expand(IdentifierNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        String identifier = processVariables(astNode.getIdentifier(), variableMap, astNode.getLocation(), context);
        astNode.setIdentifer(identifier);
        return astNode;
    }

    private ASTNode expand(ForAllStatementNode astNode, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        Stack<ASTNode> nodes = expand(astNode.getProcess(), variableMap, astNode.getRanges().getRanges(), 0,context);

        ASTNode node = nodes.pop();
        while(!nodes.isEmpty()){
            ASTNode nextNode = nodes.pop();
            node = new CompositeNode(nextNode, node, astNode.getLocation());
        }

        return node;
    }

    private Stack<ASTNode> expand(ASTNode process, Map<String, Object> variableMap, List<IndexNode> ranges, int index, Context context) throws CompilationException, InterruptedException {
        Stack<ASTNode> nodes = new Stack<>();

        if(index < ranges.size()){
            IndexNode node = ranges.get(index);
            IndexIterator iterator = IndexIterator.construct(expand(node,context));
            String variable = node.getVariable();

            while(iterator.hasNext()){
                variableMap.put(variable, iterator.next());
                nodes.addAll(expand(process, variableMap, ranges, index + 1,context));
            }
        }
        else{
            process = expand(process.copy(), variableMap, context);
            nodes.add(process);
        }

        return nodes;
    }

    private RelabelNode expand(RelabelNode relabel, Context context) throws CompilationException, InterruptedException {
        List<RelabelElementNode> relabels = new ArrayList<>();

        for(RelabelElementNode element : relabel.getRelabels()){
            if(!element.hasRanges()){
                relabels.add(element);
            }
            else{
                Map<String, Object> variableMap = new HashMap<>();
                relabels.addAll(expand(element, variableMap, element.getRanges().getRanges(), 0,context));
            }
        }

        return new RelabelNode(relabels, relabel.getLocation());
    }

    private List<RelabelElementNode> expand(RelabelElementNode element, Map<String, Object> variableMap, List<IndexNode> ranges, int index, Context context) throws CompilationException, InterruptedException {
        List<RelabelElementNode> elements = new ArrayList<>();

        if(index < ranges.size()){
            IndexNode node = ranges.get(index);
            IndexIterator iterator = IndexIterator.construct(expand(node,context));
            String variable = node.getVariable();

            while(iterator.hasNext()){
                variableMap.put(variable, iterator.next());
                elements.addAll(expand(element, variableMap, ranges, index + 1,context));
            }
        }
        else{
            String newLabel = processVariables(element.getNewLabel(), variableMap, element.getLocation(), context);
            String oldLabel = processVariables(element.getOldLabel(), variableMap, element.getLocation(), context);
            elements.add(new RelabelElementNode(newLabel, oldLabel, element.getLocation()));
        }

        return elements;
    }

    private SetNode expand(SetNode set, Context context) throws CompilationException, InterruptedException {
        // check if any ranges were defined for this set
        Map<Integer, RangesNode> rangeMap = set.getRangeMap();
        if(rangeMap.isEmpty()){
            return set;
        }

        List<String> actions = set.getSet();
        List<String> newActions = new ArrayList<>();
        for(int i = 0; i < actions.size(); i++){
            if(rangeMap.containsKey(i)){
                Map<String, Object> variableMap = new HashMap<>();
                newActions.addAll(expand(actions.get(i), variableMap, rangeMap.get(i).getRanges(), 0, context));
            }
            else{
                newActions.add(actions.get(i));
            }
        }

        return new SetNode(newActions, set.getLocation());
    }

    private List<String> expand(String action, Map<String, Object> variableMap, List<IndexNode> ranges, int index, Context context) throws CompilationException, InterruptedException {
        List<String> actions = new ArrayList<>();
        if(index < ranges.size()){
            IndexNode node = ranges.get(index);
            IndexIterator iterator = IndexIterator.construct(expand(node,context));
            String variable = node.getVariable();

            while(iterator.hasNext()){
                variableMap.put(variable, iterator.next());
                actions.addAll(expand(action, variableMap, ranges, index + 1,context));
            }
        }
        else{
            actions.add(processVariables(action, variableMap, getFullRangeLocation(ranges), context));
        }

        return actions;
    }
    //Get the location from a ranges node.
    private Location getFullRangeLocation(List<IndexNode> ranges) {
        Location start = ranges.get(0).getLocation();
        Location end = ranges.get(ranges.size()-1).getLocation();
        return new Location(start.getLineStart(),start.getColStart(),end.getLineEnd(),end.getColEnd(),start.getStartIndex(),end.getEndIndex());
    }

    private boolean evaluateCondition(BoolExpr condition, Map<String, Object> variableMap, Context context) throws CompilationException, InterruptedException {
        Map<String, Integer> variables = new HashMap<>();
        for(String key : variableMap.keySet()){
            Object value = variableMap.get(key);
            if(value instanceof Integer){
                variables.put(key, (Integer)value);
            }
        }
        return Expression.isSolvable(condition,variables,context);
    }

    private String processVariables(String string, Map<String, Object> variableMap, Location location, Context context) throws CompilationException, InterruptedException {
        Map<String, Integer> integerMap = constructIntegerMap(variableMap);
        //Construct a pattern with all hidden variables removed.
        Pattern pattern = Pattern.compile(VAR_PATTERN+hiddenVariables.stream().map(s->"(?<!\\$"+s+")").collect(Collectors.joining())+"\\b");
        while(true){
            Matcher matcher = pattern.matcher(string);
            if(matcher.find()){
                String variable = matcher.group();
                // check if the variable is a global variable
                if(globalVariableMap.containsKey(variable)){
                    Expr expression = globalVariableMap.get(variable);
                    if (containsHidden(expression)) {
                        string = string.replaceAll(Pattern.quote(variable) + "\\b", "" + ExpressionPrinter.printExpression(expression).replace("$",""));
                    } else {
                        int result = evaluator.evaluateExpression(expression, integerMap, context);
                        string = string.replaceAll(Pattern.quote(variable) + "\\b", "" + result);
                    }
                }
                else if(integerMap.containsKey(variable)){
                    string = string.replaceAll(Pattern.quote(variable)+"\\b","" + integerMap.get(variable));
                }
                else if(variableMap.containsKey(variable)){
                    string = string.replaceAll(Pattern.quote("[" + variable + "]"), "" + variableMap.get(variable));
                } else {
                    throw new CompilationException(Expander.class,"Unable to find a variable replacement for: "+variable,location);
                }
            }
            else{
                break;
            }
        }

        return string;
    }

    private boolean containsHidden(Expr ex) {
        //If there is an and inside this expression, then don't check its variables as it is added on its own.
        if (ex.isAnd()) return false;
        if (ex.isConst()) {
            return hiddenVariables.contains(ex.toString().substring(1));
        }
        for (Expr expr : ex.getArgs()) {
            if (containsHidden(expr)) return true;
        }
        return false;
    }

    private Map<String, Integer> constructIntegerMap(Map<String, Object> variableMap){
        Map<String, Integer> integerMap = new HashMap<>();
        for(String key : variableMap.keySet()){
            if(variableMap.get(key) instanceof Integer){
                integerMap.put(key, (Integer)variableMap.get(key));
            }
        }

        return integerMap;
    }
}
