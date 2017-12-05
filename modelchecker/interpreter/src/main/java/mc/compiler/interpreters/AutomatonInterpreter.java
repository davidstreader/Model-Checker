package mc.compiler.interpreters;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import mc.Constant;
import mc.compiler.Guard;
import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonNode;
import mc.process_models.automata.operations.AutomataOperations;
import java.util.*;
import java.util.stream.Collectors;
import mc.compiler.LocalCompiler;

public class AutomatonInterpreter implements ProcessModelInterpreter {

    private AutomataOperations operations;

    private Map<String, ProcessModel> processMap;
    private Map<String, AutomatonNode> referenceMap;
    private Stack<ProcessModel> processStack;
    private List<Automaton> combinedProcesses;
    private VariableSetNode variables;
    private LocalCompiler compiler;
    private Set<String> variableList;
    private Context context;
    private int subProcessCount = 0;
    public AutomatonInterpreter(){
        this.operations = new AutomataOperations();
        reset();
    }

    public ProcessModel interpret(ProcessNode processNode, Map<String, ProcessModel> processMap, LocalCompiler compiler, Context context) throws CompilationException, InterruptedException {
        reset();
        this.context = context;
        this.compiler = compiler;
        this.variableList = new HashSet<>();
        this.processMap = processMap;
        String identifier = processNode.getIdentifier();
        this.variables = processNode.getVariables();
        interpretProcess(processNode.getProcess(), identifier);

        Automaton automaton = ((Automaton)processStack.pop()).copy();

        if(processNode.hasRelabels()){
            processRelabelling(automaton, processNode.getRelabels());
        }

        if(processNode.hasHiding()){
            processHiding(automaton, processNode.getHiding());
        }

        return labelAutomaton(automaton);
    }

    public ProcessModel interpret(ASTNode astNode, String identifier, Map<String, ProcessModel> processMap, Context context) throws CompilationException, InterruptedException {
        reset();
        this.context = context;
        this.processMap = processMap;

        interpretProcess(astNode, identifier);

        Automaton automaton = ((Automaton)processStack.pop()).copy();


        return labelAutomaton(automaton);
    }

    private void interpretProcess(ASTNode astNode, String identifier) throws CompilationException, InterruptedException {
        if(astNode instanceof IdentifierNode){
            String reference = ((IdentifierNode)astNode).getIdentifier();
            if (this.variables != null) {
                ProcessNode node = (ProcessNode) compiler.getProcessNodeMap().get(reference).copy();
                //Use the current variable set when recompiling.
                node.setVariables(this.variables);
                node = compiler.compile(node,context);
                ProcessModel model = new AutomatonInterpreter().interpret(node,this.processMap, compiler,context);
                processStack.push(model);
            } else {
                ProcessModel model = processMap.get(reference);
                processStack.push(model);
            }
            combinedProcesses.add((Automaton) processStack.peek());
        }
        else if(astNode instanceof ProcessRootNode){
            ProcessRootNode root = (ProcessRootNode)astNode;

            interpretProcess(root.getProcess(), identifier);
            Automaton automaton = ((Automaton)processStack.pop()).copy();

            automaton = processLabellingAndRelabelling(automaton, root);
            if(root.hasHiding()){
                processHiding(automaton, root.getHiding());
                automaton.setHiding(root.getHiding());
            }

            processStack.push(automaton);
        }
        else{
            Automaton automaton = new Automaton(identifier);
            if (variables != null) {
                automaton.setHiddenVariables(variables.getVariables());
                automaton.setHiddenVariablesLocation(variables.getLocation());
            }

            automaton.setVariables(variableList);
            automaton.setVariablesLocation(astNode.getLocation());

            interpretNode(astNode, automaton, automaton.getRoot());
            processStack.push(automaton);
        }
    }
    private void interpretNode(ASTNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException {
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        // check if the current ast node has a reference attached
        if(astNode.hasReferences()){
            for(String reference : astNode.getReferences())
                referenceMap.put(reference, currentNode);

            currentNode.setReferences(astNode.getReferences());
        }
        if (astNode.getModelVariables() != null) {
            Map<String,Object> varMap = astNode.getModelVariables();
            varMap.keySet().stream().map(s->s.substring(1)).forEach(variableList::add);
            currentNode.setVariables(varMap);
        }
        currentNode.copyPropertiesFromASTNode(astNode);
        if(astNode instanceof ProcessRootNode){
            interpretNode((ProcessRootNode)astNode, automaton, currentNode);
        }
        else if(astNode instanceof SequenceNode){
            interpretNode((SequenceNode)astNode, automaton, currentNode);
        }
        else if(astNode instanceof ChoiceNode){
            interpretNode((ChoiceNode)astNode, automaton, currentNode);
        }
        else if(astNode instanceof CompositeNode){
            interpretNode((CompositeNode)astNode, automaton, currentNode);
        }
        else if(astNode instanceof IdentifierNode){
            interpretNode((IdentifierNode)astNode, automaton, currentNode);
        }
        else if(astNode instanceof FunctionNode){
            interpretNode((FunctionNode)astNode, automaton, currentNode);
        }
        else if(astNode instanceof TerminalNode){
            interpretNode((TerminalNode)astNode, automaton, currentNode);
        }
    }


    private void interpretNode(ProcessRootNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException {
        interpretProcess(astNode.getProcess(), automaton.getId() + "." + ++subProcessCount);
        Automaton model = ((Automaton)processStack.pop()).copy();
        processLabellingAndRelabelling(model, astNode);

        if(astNode.hasHiding()){
            processHiding(model, astNode.getHiding());
        }

        AutomatonNode oldRoot = automaton.addAutomaton(model);
        automaton.combineNodes(currentNode, oldRoot,context);
    }

    private void interpretNode(SequenceNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException {
        String action = astNode.getFrom().getAction();

        AutomatonNode nextNode;
        Guard foundGuard = null;
        if (currentNode.getGuard() != null) {
            foundGuard = (Guard)astNode.getGuard();
            currentNode.setGuard(null);
        }
        // check if the next ast node is a reference node
        if(astNode.getTo() instanceof ReferenceNode){
            ReferenceNode reference = (ReferenceNode)astNode.getTo();
            nextNode = referenceMap.get(reference.getReference());
            automaton.addEdge(action, currentNode, nextNode,foundGuard);
        }
        else {
            nextNode = automaton.addNode();
            automaton.addEdge(action, currentNode, nextNode,foundGuard);
            interpretNode(astNode.getTo(), automaton, nextNode);
        }
    }

    private void interpretNode(ChoiceNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException {
        interpretNode(astNode.getFirstProcess(), automaton, currentNode);
        interpretNode(astNode.getSecondProcess(), automaton, currentNode);
    }
    private void interpretNode(CompositeNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException {
        interpretProcess(astNode.getFirstProcess(), automaton.getId() + ".pc1");
        interpretProcess(astNode.getSecondProcess(), automaton.getId() + ".pc2");

        ProcessModel model2 = processStack.pop();
        ProcessModel model1 = processStack.pop();

        if(!(model1 instanceof Automaton) || !(model2 instanceof Automaton))
            throw new CompilationException(getClass(),"Expecting an automaton, received: "+model1.getClass().getSimpleName()+","+model2.getClass().getSimpleName(),astNode.getLocation());

        Automaton comp = operations.parallelComposition(automaton.getId(), ((Automaton)model1).copy(), ((Automaton)model2).copy());
        AutomatonNode oldRoot = automaton.addAutomaton(comp);
        automaton.combineNodes(currentNode, oldRoot,context);

    }
    private void interpretNode(IdentifierNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException {
        // check that the reference is to an automaton
        ProcessModel model = processMap.get(astNode.getIdentifier());
        if(!(model instanceof Automaton))
            throw new CompilationException(getClass(),"Unable to find identifier: "+astNode.getIdentifier(),astNode.getLocation());


        Automaton next = ((Automaton)model).copy();
        addAutomaton(currentNode, automaton, next);
    }
    private void interpretNode(FunctionNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException, InterruptedException {
        interpretProcess(astNode.getProcess(), automaton.getId() + ".fn");
        ProcessModel model = processStack.pop();
        if (model == null)
            throw new CompilationException(getClass(),"Expecting an automaton, received an undefined process.",astNode.getLocation());

        if(! (model instanceof Automaton ) )
            throw new CompilationException(getClass(),"Expecting an automaton, received a: "+model.getClass().getSimpleName(),astNode.getLocation());

        Automaton processed;
        switch(astNode.getFunction()){
            case "abs":
                boolean isFair = astNode.isFair();
                boolean prune = astNode.needsPruning();
                processed = operations.abstraction(((Automaton)model).copy(), isFair, prune,context);
             break;

            case "prune":
                 processed = operations.prune(((Automaton)model).copy(),context);
            break;

            case "simp":
                 processed = operations.simplification(((Automaton)model).copy(),astNode.getReplacements(),context);
             break;

            case "safe":
                // automata cannot contain unreachable states therefore they are always safe
                processed = ((Automaton)model).copy();
            break;

            case "nfa2dfa":
                    processed = operations.nfaToDFA(labelAutomaton(((Automaton)model)).copy());
             break;

            default:
                throw new CompilationException(getClass(),"Expecting a known function, received: "+astNode.getFunction(),astNode.getLocation());
        }

        addAutomaton(currentNode, automaton, processed);
    }

    private void interpretNode(TerminalNode astNode, Automaton automaton, AutomatonNode currentNode){
        currentNode.setTerminal(astNode.getTerminal());
    }

    private Automaton processLabellingAndRelabelling(Automaton automaton, ProcessRootNode astNode) throws CompilationException {
        if(astNode.hasLabel()){
            automaton = operations.labelAutomaton(automaton, astNode.getLabel());
        }
        if(astNode.hasRelabelSet()){
            processRelabelling(automaton, astNode.getRelabelSet());
        }

        return automaton;
    }

    private void addAutomaton(AutomatonNode currentNode, Automaton automaton1, Automaton automaton2) throws CompilationException, InterruptedException {
        List<String> references = new ArrayList<>();

        if(currentNode.getReferences() != null){
            references.addAll(currentNode.getReferences());
        }

        AutomatonNode oldRoot = automaton1.addAutomaton(automaton2);

        if(oldRoot.getReferences() != null){
            references.addAll(oldRoot.getReferences());
        }

        AutomatonNode node = automaton1.combineNodes(currentNode, oldRoot,context);
        references.forEach(id -> referenceMap.put(id, node));
    }

    private void processRelabelling(Automaton automaton, RelabelNode relabels){
        automaton.setAlphabetBeforeHiding(new HashSet<>(automaton.getAlphabet()));
        automaton.setRelabels(relabels.getRelabels());

        for(RelabelElementNode element : relabels.getRelabels()){
            automaton.relabelEdges(element.getOldLabel(), element.getNewLabel());
        }
    }

    private void processHiding(Automaton automaton, HidingNode hiding) throws CompilationException {
        Set<String> alphabet = automaton.getAlphabet();
        if (automaton.getAlphabetBeforeHiding() == null)
            automaton.setAlphabetBeforeHiding(new HashSet<>(automaton.getAlphabet()));
        Set<String> hidden = new HashSet<>(hiding.getSet().getSet());
        String type = hiding.getType();
        if (type.equals("includes")) {
            for (String action : hidden) {
                if (alphabet.contains(action)) {
                    automaton.relabelEdges(action, Constant.HIDDEN);
                } else {
                    throw new CompilationException(AutomatonInterpreter.class, "Unable to find action " + action + " for hiding.", hiding.getLocation());
                }
            }
        } else if (type.equals("excludes")) {
            for (String action : new ArrayList<>(alphabet)) {
                if (!hidden.contains(action)) {
                    automaton.relabelEdges(action, Constant.HIDDEN);
                }
            }
        }
    }

    private Automaton labelAutomaton(Automaton automaton) throws CompilationException {
        Set<String> visited = new HashSet<>();

        Queue<AutomatonNode> fringe = new LinkedList<>();
        fringe.offer(automaton.getRoot());

        int label = 0;
        while(!fringe.isEmpty()){
            AutomatonNode current = fringe.poll();

            if(visited.contains(current.getId())){
                continue;
            }

            current.getOutgoingEdges().forEach(edge -> fringe.offer(edge.getTo()));
            current.setLabelNumber(label++);

            visited.add(current.getId());
        }

        return automaton;
    }

    private void reset(){
        this.referenceMap = new HashMap<>();
        this.processStack = new Stack<>();
        combinedProcesses = new ArrayList<>();
    }
}
