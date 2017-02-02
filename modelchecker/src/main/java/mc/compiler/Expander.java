package mc.compiler;

import com.microsoft.z3.BoolExpr;
import mc.compiler.ast.*;
import mc.compiler.iterator.IndexIterator;
import mc.solver.ExpressionSolver;
import mc.util.expr.*;
import mc.webserver.LogMessage;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Expander {

    private Map<String, String> globalVariableMap;
    private List<IndexNode> ranges;
    public AbstractSyntaxTree expand(AbstractSyntaxTree ast){
        globalVariableMap = ast.getVariableMap();

        List<ProcessNode> processes = ast.getProcesses();
        for(int i = 0; i < processes.size(); i++){
            ProcessNode process = processes.get(i);
            new LogMessage("Expanding:",process).send();
            Map<String, Object> variableMap = new HashMap<String, Object>();
            ASTNode root = expand(process.getProcess(), variableMap);
            process.setProcess(root);

            List<LocalProcessNode> localProcesses = expandLocalProcesses(process.getLocalProcesses(), variableMap);
            process.setLocalProcesses(localProcesses);
        }

        List<OperationNode> operations = ast.getOperations();
        for(int i = 0; i < operations.size(); i++){
            OperationNode operation = operations.get(i);
            Map<String, Object> variableMap = new HashMap<String, Object>();
            ASTNode process1 = expand(operation.getFirstProcess(), variableMap);
            ASTNode process2 = expand(operation.getSecondProcess(), variableMap);

            operation.setFirstProcess(process1);
            operation.setSecondProcess(process2);
        }

        return ast;
    }

    private List<LocalProcessNode> expandLocalProcesses(List<LocalProcessNode> localProcesses, Map<String, Object> variableMap){
        List<LocalProcessNode> newLocalProcesses = new ArrayList<LocalProcessNode>();

        for(int i = 0; i < localProcesses.size(); i++){
            LocalProcessNode localProcess = localProcesses.get(i);
            if(localProcess.getRanges() == null){
                ASTNode root = expand(localProcess.getProcess(), variableMap);
                localProcess.setProcess(root);
                newLocalProcesses.add(localProcess);
            }
            else{
                newLocalProcesses.addAll(expandLocalProcesses(localProcess, variableMap, localProcess.getRanges().getRanges(), 0));
            }
        }

        return newLocalProcesses;
    }

    private List<LocalProcessNode> expandLocalProcesses(LocalProcessNode localProcess, Map<String, Object> variableMap, List<IndexNode> ranges, int index){
        List<LocalProcessNode> newLocalProcesses = new ArrayList<LocalProcessNode>();
        this.ranges = ranges;
        if(index < ranges.size()){
            IndexNode range = ranges.get(index);
            IndexIterator iterator = IndexIterator.construct(range.getRange());
            String variable = range.getVariable();
            localProcess.setIdentifier(localProcess.getIdentifier() + "[" + variable + "]");
            while(iterator.hasNext()){
                variableMap.put(variable, iterator.next());
                newLocalProcesses.addAll(expandLocalProcesses((LocalProcessNode) localProcess.clone(), variableMap, ranges, index + 1));
            }
        }
        else{
            LocalProcessNode clone = (LocalProcessNode)localProcess.clone();
            ASTNode root = expand(clone.getProcess(), variableMap);
            clone.setIdentifier(processVariables(clone.getIdentifier(), variableMap));
            clone.setProcess(root);
            newLocalProcesses.add(clone);
        }

        return newLocalProcesses;
    }

    private ASTNode expand(ASTNode astNode, Map<String, Object> variableMap){
        if(astNode instanceof ActionLabelNode){
            astNode = expand((ActionLabelNode)astNode, variableMap);
        }
        else if(astNode instanceof IndexNode){
            astNode = expand((IndexNode)astNode, variableMap);
        }
        else if(astNode instanceof SequenceNode){
            astNode = expand((SequenceNode)astNode, variableMap);
        }
        else if(astNode instanceof ChoiceNode){
            astNode = expand((ChoiceNode)astNode, variableMap);
        }
        else if(astNode instanceof CompositeNode){
            astNode = expand((CompositeNode)astNode, variableMap);
        }
        else if(astNode instanceof IfStatementNode){
            astNode = expand((IfStatementNode)astNode, variableMap);
        }
        else if(astNode instanceof FunctionNode){
            astNode = expand((FunctionNode)astNode, variableMap);
        }
        else if(astNode instanceof IdentifierNode){
            astNode = expand((IdentifierNode)astNode, variableMap);
        }
        else if(astNode instanceof ForAllStatementNode){
            astNode = expand((ForAllStatementNode)astNode, variableMap);
        }

        if(astNode.hasLabel()){
            astNode.setLabel(processVariables(astNode.getLabel(), variableMap));
        }

        return astNode;
    }

    private ActionLabelNode expand(ActionLabelNode astNode, Map<String, Object> variableMap){
        String action = processVariables(astNode.getAction(), variableMap);
        astNode.setAction(action);
        return astNode;
    }

    private ASTNode expand(IndexNode astNode, Map<String, Object> variableMap){
        IndexIterator iterator = IndexIterator.construct(astNode.getRange());
        Stack<ASTNode> iterations = new Stack<ASTNode>();
        while(iterator.hasNext()){
            Object element = iterator.next();
            variableMap.put(astNode.getVariable(), element);
            iterations.push(expand(astNode.getProcess(), variableMap));
        }

        ASTNode node = iterations.pop();
        while(!iterations.isEmpty()){
            ASTNode nextNode = iterations.pop();
            node = new ChoiceNode(nextNode, node, astNode.getLocation());
        }

        return node;
    }

    private SequenceNode expand(SequenceNode astNode, Map<String, Object> variableMap){
        //add a guard to every sequenceNode. This will only contain next data.
        Guard guard = new Guard();
        if (astNode.getTo() instanceof IdentifierNode) {
            //Parse the next values from this IdentifierNode
            guard.parseNext(((IdentifierNode) astNode.getTo()).getIdentifier(), globalVariableMap, ranges);
            //There were next values, so assign to the metadata
            if (guard.hasData())
                astNode.getMetaData().put("guard",guard);
        }
        ActionLabelNode from = expand(astNode.getFrom(), variableMap);
        ASTNode to = expand(astNode.getTo(), variableMap);
        astNode.setFrom(from);
        astNode.setTo(to);
        return astNode;
    }

    private ASTNode expand(ChoiceNode astNode, Map<String, Object> variableMap){
        ASTNode process1 = expand(astNode.getFirstProcess(), variableMap);
        ASTNode process2 = expand(astNode.getSecondProcess(), variableMap);

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

    private ASTNode expand(CompositeNode astNode, Map<String, Object> variableMap){
        ASTNode process1 = expand(astNode.getFirstProcess(), variableMap);
        ASTNode process2 = expand(astNode.getSecondProcess(), variableMap);

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

    private ASTNode expand(IfStatementNode astNode, Map<String, Object> variableMap){
        Guard guard = new Guard();
        guard.setGuard(astNode.getCondition());
        guard.setVariables(new ExpressionPrinter().getVariables(astNode.getCondition(),variableMap));
        boolean condition = evaluateCondition(astNode.getCondition(), variableMap);
        if(condition){
            ASTNode expand = expand(astNode.getTrueBranch(), variableMap);
            if (expand.getMetaData().containsKey("guard"))
                guard.mergeWith((Guard) expand.getMetaData().get("guard"));
            expand.getMetaData().put("guard",guard);
            return expand;
        }
        else if(astNode.hasFalseBranch()){
            ASTNode expand = expand(astNode.getFalseBranch(), variableMap);
            if (expand.getMetaData().containsKey("guard"))
                guard.mergeWith((Guard) expand.getMetaData().get("guard"));
            expand.getMetaData().put("guard",guard);
            return expand;
        }

        return new EmptyNode();
    }

    private FunctionNode expand(FunctionNode astNode, Map<String, Object> variableMap){
        ASTNode process = expand(astNode.getProcess(), variableMap);
        astNode.setProcess(process);
        return astNode;
    }

    private IdentifierNode expand(IdentifierNode astNode, Map<String, Object> variableMap){
        String identifier = processVariables(astNode.getIdentifier(), variableMap);
        astNode.setIdentifer(identifier);
        return astNode;
    }

    private ASTNode expand(ForAllStatementNode astNode, Map<String, Object> variableMap){
        Stack<ASTNode> nodes = expand(astNode.getProcess(), variableMap, astNode.getRanges().getRanges(), 0);

        ASTNode node = nodes.pop();
        while(!nodes.isEmpty()){
            ASTNode nextNode = nodes.pop();
            node = new CompositeNode(nextNode, node, astNode.getLocation());
        }

        return node;
    }

    private Stack<ASTNode> expand(ASTNode process, Map<String, Object> variableMap, List<IndexNode> ranges, int index){
        Stack<ASTNode> nodes = new Stack<ASTNode>();

        if(index < ranges.size()){
            IndexNode node = ranges.get(index);
            IndexIterator iterator = IndexIterator.construct(node.getRange());
            String variable = node.getVariable();

            while(iterator.hasNext()){
                variableMap.put(variable, iterator.next());
                nodes.addAll(expand(process, variableMap, ranges, index + 1));
            }
        }
        else{
            process = expand(process.clone(), variableMap);
            nodes.add(process);
        }

        return nodes;
    }

    private int evaluateExpression(Expression expression, Map<String, Object> variableMap){
        // remove all strings from the variableMap
        Map<String, Integer> variables = new HashMap<>();
        for(String key : variableMap.keySet()){
            Object value = variableMap.get(key);
            if(value instanceof Integer){
                variables.put(key, (Integer)value);
            }
        }
        Expression ex = ExpressionSolver.simplify(expression, variables);
        if (ex instanceof BooleanOperand) return ((BooleanOperand) ex).getValue()?1:0;
        if (ex instanceof IntegerOperand) return ((IntegerOperand) ex).getValue();
        //TODO: throw better error
        throw new IllegalStateException("There was an undefined variable in that statement.");
    }

    private boolean evaluateCondition(Expression condition, Map<String, Object> variableMap){
        Map<String, Integer> variables = new HashMap<String, Integer>();
        for(String key : variableMap.keySet()){
            Object value = variableMap.get(key);
            if(value instanceof Integer){
                variables.put(key, (Integer)value);
            }
        }
        Expression ex = ExpressionSolver.simplify(condition,variables);
        return ex instanceof BooleanOperand && ((BooleanOperand) ex).getValue();
    }

    private String processVariables(String string, Map<String, Object> variableMap){
        Map<String,String> varMap = new HashMap<>();
        varMap.putAll(globalVariableMap);
        for (String key : variableMap.keySet()) {
            varMap.put(key,variableMap.get(key).toString());
        }
        Pattern pattern = Pattern.compile("\\$[a-z][a-zA-Z0-9_]*");
        while(true){
            Matcher matcher = pattern.matcher(string);
            if(matcher.find()){
                String variable = matcher.group();
                // check if variable is a global variable
                if(globalVariableMap.containsKey(variable)){
                    Expression expression = Expression.constructExpression(variable,varMap);
                    int result = evaluateExpression(expression, variableMap);
                    string = string.replaceAll(Matcher.quoteReplacement(variable)+"\\b","" + result);
                }
                else if(variableMap.containsKey(variable)){
                    string = string.replaceAll(Matcher.quoteReplacement(variable)+"\\b", variableMap.get(variable).toString());
                }
            }
            else{
                break;
            }
        }
        return string;
    }
}
