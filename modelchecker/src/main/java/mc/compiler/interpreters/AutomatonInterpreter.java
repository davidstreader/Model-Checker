package mc.compiler.interpreters;

import lombok.SneakyThrows;
import mc.Constant;
import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import mc.process_models.automata.operations.AutomataOperations;

import java.util.*;

public class AutomatonInterpreter implements ProcessModelInterpreter {

    private AutomataOperations operations;

    private Map<String, ProcessModel> processMap;
    private Map<String, AutomatonNode> referenceMap;
    private Stack<ProcessModel> processStack;

    public AutomatonInterpreter(){
        this.operations = new AutomataOperations();
        reset();
    }

    public ProcessModel interpret(ProcessNode processNode, Map<String, ProcessModel> processMap) throws CompilationException {
        reset();
        this.processMap = processMap;
        String identifier = processNode.getIdentifier();

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

    public ProcessModel interpret(ASTNode astNode, String identifier, Map<String, ProcessModel> processMap) throws CompilationException {
        reset();
        this.processMap = processMap;

        interpretProcess(astNode, identifier);

        Automaton automaton = ((Automaton)processStack.pop()).copy();

        return labelAutomaton(automaton);
    }

    private void interpretProcess(ASTNode astNode, String identifier) throws CompilationException {
        if(astNode instanceof IdentifierNode){
            String reference = ((IdentifierNode)astNode).getIdentifier();
            ProcessModel model = processMap.get(reference);
            processStack.push(model);
        }
        else if(astNode instanceof ProcessRootNode){
            ProcessRootNode root = (ProcessRootNode)astNode;

            interpretProcess(root.getProcess(), identifier);
            Automaton automaton = ((Automaton)processStack.pop()).copy();

            automaton = processLabellingAndRelabelling(automaton, root);

            if(root.hasHiding()){
                processHiding(automaton, root.getHiding());
            }

            processStack.push(automaton);
        }
        else{
            Automaton automaton = new Automaton(identifier);
            automaton.addMetaData("location",astNode.getLocation());
            interpretNode(astNode, automaton, automaton.getRoot());
            processStack.push(automaton);
        }
    }

    private void interpretNode(ASTNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException {
        // check if the current ast node has a reference attached
        if(astNode.hasReferences()){
            for(String reference : astNode.getReferences()){
                referenceMap.put(reference, currentNode);
            }
            currentNode.addMetaData("reference", astNode.getReferences());
        }
        if (astNode.getMetaData().containsKey("variables")) {
            currentNode.addMetaData("variables",astNode.getMetaData().get("variables"));
        }
        currentNode.getMetaData().putAll(astNode.getMetaData());
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

    private int subProcessCount = 0;
    private void interpretNode(ProcessRootNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException{
        interpretProcess(astNode.getProcess(), automaton.getId() + "." + ++subProcessCount);
        Automaton model = ((Automaton)processStack.pop()).copy();

        processLabellingAndRelabelling(model, astNode);

        if(astNode.hasHiding()){
            processHiding(model, astNode.getHiding());
        }

        AutomatonNode oldRoot = automaton.addAutomaton(model);
        automaton.combineNodes(currentNode, oldRoot);
    }

    private void interpretNode(SequenceNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException {
        String action = astNode.getFrom().getAction();
        Map<String,Object> metadata = new HashMap<>();
        if (action.contains("?")) {
            metadata.put("receiver",true);
            action = action.replace("?","");
        } else if (action.contains("!")) {
            metadata.put("broadcaster",true);
            action = action.replace("!","");
        }
        AutomatonNode nextNode;
        AutomatonEdge nextEdge;
        // check if the next ast node is a reference node
        if(astNode.getTo() instanceof ReferenceNode){
            ReferenceNode reference = (ReferenceNode)astNode.getTo();
            nextNode = referenceMap.get(reference.getReference());
            nextEdge = automaton.addEdge(action, currentNode, nextNode);
            nextEdge.getMetaData().putAll(metadata);
        }
        else {
            nextNode = automaton.addNode();
            nextEdge = automaton.addEdge(action, currentNode, nextNode);
            interpretNode(astNode.getTo(), automaton, nextNode);
            nextEdge.getMetaData().putAll(metadata);
        }

        if (currentNode.getMetaData().containsKey("guard")) {
            nextEdge.addMetaData("guard",astNode.getMetaData().get("guard"));
            currentNode.getMetaData().remove("guard");
        }
    }

    private void interpretNode(ChoiceNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException {
        interpretNode(astNode.getFirstProcess(), automaton, currentNode);
        interpretNode(astNode.getSecondProcess(), automaton, currentNode);
    }
    private void interpretNode(CompositeNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException {
        interpretProcess(astNode.getFirstProcess(), automaton.getId() + ".pc1");
        interpretProcess(astNode.getSecondProcess(), automaton.getId() + ".pc2");

        ProcessModel model2 = processStack.pop();
        ProcessModel model1 = processStack.pop();
        if(!(model1 instanceof Automaton) || !(model2 instanceof Automaton)){
            throw new CompilationException(getClass(),"Expecting an automaton, received: "+model1.getClass().getSimpleName()+","+model2.getClass().getSimpleName(),astNode.getLocation());
        }

        Automaton comp = operations.parallelComposition(automaton.getId(), ((Automaton)model1).copy(), ((Automaton)model2).copy());

        AutomatonNode oldRoot = automaton.addAutomaton(comp);
        automaton.combineNodes(currentNode, oldRoot);

    }
    private void interpretNode(IdentifierNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException {
        // check that the reference is to an automaton
        ProcessModel model = processMap.get(astNode.getIdentifier());
        if(!(model instanceof Automaton)){
            throw new CompilationException(getClass(),"Unable to find identifier: "+astNode.getIdentifier(),astNode.getLocation());
        }

        Automaton next = ((Automaton)model).copy();
        addAutomaton(currentNode, automaton, next);
    }
    private void interpretNode(FunctionNode astNode, Automaton automaton, AutomatonNode currentNode) throws CompilationException {
        interpretProcess(astNode.getProcess(), automaton.getId() + ".fn");
        ProcessModel model = processStack.pop();
        if (model == null) {
            throw new CompilationException(getClass(),"Expecting an automaton, received an undefined process.",astNode.getLocation());
        }
        Automaton processed;
        switch(astNode.getFunction()){
            case "abs":
                if(model instanceof Automaton){
                    processed = operations.abstraction(((Automaton)model).copy());
                    break;
                }
                throw new CompilationException(getClass(),"Expecting an automaton, received a: "+model.getClass().getSimpleName(),astNode.getLocation());
            case "prune":
                if(model instanceof Automaton){
                    processed = operations.prune(((Automaton)model).copy());
                    break;
                }
                throw new CompilationException(getClass(),"Expecting an automaton, received a: "+model.getClass().getSimpleName(),astNode.getLocation());
            case "simp":
                if(model instanceof Automaton){
                    processed = operations.simplification(((Automaton)model).copy());
                    break;
                }
                throw new CompilationException(getClass(),"Expecting an automaton, received a: "+model.getClass().getSimpleName(),astNode.getLocation());
            case "safe":
                if(model instanceof Automaton){
                    // automata cannot contain unreachable states therefore they are always safe
                    processed = ((Automaton)model).copy();
                    break;
                }
                throw new CompilationException(getClass(),"Expecting an automaton, received a: "+model.getClass().getSimpleName(),astNode.getLocation());
            case "nfa2dfa":
                if(model instanceof Automaton){
                    processed = operations.nfa2dfa(labelAutomaton(((Automaton)model)).copy());
                    break;
                }
                throw new CompilationException(getClass(),"Expecting an automaton, received a: "+model.getClass().getSimpleName(),astNode.getLocation());
            default:
                throw new CompilationException(getClass(),"Expecting a known function, received: "+astNode.getFunction(),astNode.getLocation());
        }

        addAutomaton(currentNode, automaton, processed);
    }

    private void interpretNode(TerminalNode astNode, Automaton automaton, AutomatonNode currentNode){
        currentNode.addMetaData("isTerminal", astNode.getTerminal());
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

    private void addAutomaton(AutomatonNode currentNode, Automaton automaton1, Automaton automaton2) throws CompilationException {
        List<String> references = new ArrayList<String>();

        if(currentNode.hasMetaData("reference")){
            references.addAll((Set<String>)currentNode.getMetaData("reference"));
        }

        AutomatonNode oldRoot = automaton1.addAutomaton(automaton2);

        if(oldRoot.hasMetaData("reference")){
            references.addAll((Set<String>)oldRoot.getMetaData("reference"));
        }

        AutomatonNode node = automaton1.combineNodes(currentNode, oldRoot);
        references.forEach(id -> referenceMap.put(id, node));
    }

    private void processRelabelling(Automaton automaton, RelabelNode relabels){
        for(RelabelElementNode element : relabels.getRelabels()){
            automaton.relabelEdges(element.getOldLabel(), element.getNewLabel());
        }
    }

    private void processHiding(Automaton automaton, HidingNode hiding){
        Set<String> alphabet = automaton.getAlphabet();
        String type = hiding.getType();

        for(String action : hiding.getSet().getSet()){
            if(alphabet.contains(action) && type.equals("includes")){
                automaton.relabelEdges(action, Constant.HIDDEN);
            }
            else if(!alphabet.contains(action) && type.equals("excludes")){
                automaton.relabelEdges(action, Constant.HIDDEN);
            }
        }
    }

    private Automaton labelAutomaton(Automaton automaton) throws CompilationException {
        Set<String> visited = new HashSet<String>();

        Queue<AutomatonNode> fringe = new LinkedList<AutomatonNode>();
        fringe.offer(automaton.getRoot());

        int label = 0;
        while(!fringe.isEmpty()){
            AutomatonNode current = fringe.poll();

            if(visited.contains(current.getId())){
                continue;
            }

            current.getOutgoingEdges().forEach(edge -> fringe.offer(edge.getTo()));
            if(!current.hasMetaData("dfa")) {
                current.addMetaData("label", label++);
            }

            visited.add(current.getId());
        }

        return automaton;
    }

    private void reset(){
        this.referenceMap = new HashMap<String, AutomatonNode>();
        this.processStack = new Stack<ProcessModel>();
    }
}
