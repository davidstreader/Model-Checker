package mc.compiler.interpreters;

import mc.compiler.ast.*;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonNode;
import mc.process_models.automata.operations.AutomataOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class AutomatonInterpreter {

    private AutomataOperations operations;

    private Map<String, ProcessModel> processMap;
    private Map<Integer, AutomatonNode> referenceMap;
    private Stack<ProcessModel> processStack;

    public AutomatonInterpreter(){
        this.operations = new AutomataOperations();
        reset();
    }

    public ProcessModel interpret(ProcessNode processNode, Map<String, ProcessModel> processMap){
        reset();
        this.processMap = processMap;
        String identifier = processNode.getIdentifier();
        interpretProcess(processNode.getProcess(), identifier);

        return processStack.pop();
    }

    private void interpretProcess(ASTNode astNode, String identifier){
        if(astNode instanceof IdentifierNode){
            String reference = ((IdentifierNode)astNode).getIdentifier();
            processStack.push(processMap.get(reference));
        }
        else{
            Automaton automaton = new Automaton(identifier);
            interpretNode(astNode, automaton, automaton.getRoot());
            processStack.push(automaton);
        }
    }

    private void interpretNode(ASTNode astNode, Automaton automaton, AutomatonNode currentNode){
        // check if the current ast node has a reference attached
        if(astNode.hasReferenceId()){
            referenceMap.put(astNode.getReferenceId(), currentNode);
        }

        if(astNode instanceof SequenceNode){
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

    private void interpretNode(SequenceNode astNode, Automaton automaton, AutomatonNode currentNode){
        String action = astNode.getFrom().getAction();

        // check if the next ast node is a reference node
        if(astNode.getTo() instanceof ReferenceNode){
            ReferenceNode reference = (ReferenceNode)astNode.getTo();
            AutomatonNode nextNode = referenceMap.get(reference.getReference());
            automaton.addEdge(action, currentNode, nextNode);
        }
        else {
            AutomatonNode nextNode = automaton.addNode();
            automaton.addEdge(action, currentNode, nextNode);
            interpretNode(astNode.getTo(), automaton, nextNode);
        }
    }

    private void interpretNode(ChoiceNode astNode, Automaton automaton, AutomatonNode currentNode){
        interpretNode(astNode.getFirstProcess(), automaton, currentNode);
        interpretNode(astNode.getSecondProcess(), automaton, currentNode);
    }

    private void interpretNode(CompositeNode astNode, Automaton automaton, AutomatonNode currentNode){
        interpretProcess(astNode.getFirstProcess(), automaton.getId() + ".pc1");
        interpretProcess(astNode.getSecondProcess(), automaton.getId() + ".pc2");

        ProcessModel model2 = processStack.pop();
        ProcessModel model1 = processStack.pop();
        if(!(model1 instanceof Automaton) || !(model2 instanceof Automaton)){
            // TODO: throw error
        }

        Automaton comp = operations.parallelComposition(automaton.getId(), (Automaton)model1, (Automaton)model2);
        comp = operations.removeUnreachableNodes(comp);

        automaton.addAutomaton(comp);
        automaton.combineNodes(currentNode, comp.getRoot());

    }

    private void interpretNode(IdentifierNode astNode, Automaton automaton, AutomatonNode currentNode){
        // check that the reference is to an automaton
        ProcessModel model = processMap.get(astNode.getIdentifier());
        if(!(model instanceof Automaton)){
            // TODO: throw error
        }

        Automaton next = (Automaton)((Automaton)model).clone();
        automaton.addAutomaton(next);
        automaton.combineNodes(currentNode, next.getRoot());
    }

    private void interpretNode(FunctionNode astNode, Automaton automaton, AutomatonNode currentNode){

    }

    private void interpretNode(TerminalNode astNode, Automaton automaton, AutomatonNode currentNode){
        currentNode.addMetaData("isTerminal", astNode.getTerminal());
    }

    private void reset(){
        this.referenceMap = new HashMap<Integer, AutomatonNode>();
        this.processStack = new Stack<ProcessModel>();
    }
}
