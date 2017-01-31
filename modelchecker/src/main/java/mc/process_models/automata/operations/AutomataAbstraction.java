package mc.process_models.automata.operations;

import mc.compiler.Guard;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import mc.solver.JavaSMTConverter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sheriddavi on 25/01/17.
 */
public class AutomataAbstraction {
    private JavaSMTConverter smt = new JavaSMTConverter();
    public Automaton performAbstraction(Automaton automaton, boolean isFair){
        Automaton abstraction = new Automaton(automaton.getId() + ".abs", !Automaton.CONSTRUCT_ROOT);
        automaton.getNodes().forEach(node -> addNode(abstraction, node));
        automaton.getEdges().stream()
                .filter(edge -> !edge.isHidden())
                .forEach(edge -> addEdge(abstraction, edge));

        List<AutomatonEdge> hiddenEdges = automaton.getEdges().stream()
                .filter(edge -> edge.isHidden())
                .collect(Collectors.toList());

        for(AutomatonEdge hiddenEdge : hiddenEdges){
            constructOutgoingObservableEdges(abstraction, hiddenEdge);
            constructIncomingObservableEdges(abstraction, hiddenEdge);
        }

        return abstraction;
    }

    private void constructOutgoingObservableEdges(Automaton abstraction, AutomatonEdge hiddenEdge){
        Guard hiddenGuard = (Guard) hiddenEdge.getMetaData("guard");
        List<AutomatonEdge> incomingObservableEdges = hiddenEdge.getFrom().getIncomingEdges().stream()
                .filter(edge -> !edge.isHidden())
                .collect(Collectors.toList());

        List<AutomatonNode> outgoingNodes = new ArrayList<AutomatonNode>();
        Set<String> visited = new HashSet<String>();
        Stack<AutomatonEdge> fringe = new Stack<AutomatonEdge>();
        fringe.push(hiddenEdge);

        while(!fringe.isEmpty()){
            AutomatonEdge current = fringe.pop();
            List<AutomatonEdge> outgoingEdges = current.getTo().getOutgoingEdges();

            outgoingNodes.add(abstraction.getNode(current.getTo().getId() + ".abs"));

            outgoingEdges.stream()
                    .filter(edge -> edge.isHidden() && !visited.contains(edge.getId()))
                    .forEach(edge -> fringe.push(edge));

            visited.add(current.getId());
        }

        for(AutomatonEdge edge : incomingObservableEdges){
            AutomatonNode from = abstraction.getNode(edge.getFrom().getId() + ".abs");
            Guard fromGuard = (Guard) from.getMetaData("guard");
            Guard outGuard = null;
            if (fromGuard != null && hiddenGuard == null) {
                outGuard = fromGuard;
            } else if (fromGuard == null && hiddenGuard != null) {
                outGuard = hiddenGuard;
            } else if (fromGuard != null) {
                outGuard = smt.combineGuards(hiddenGuard,fromGuard);
            }
            for (AutomatonNode to : outgoingNodes) {
                AutomatonEdge edge1 = abstraction.addEdge(edge.getLabel(), from, to);
                if (outGuard != null) {
                    edge1.addMetaData("guard",outGuard);
                }
            }
        }
    }

    private void constructIncomingObservableEdges(Automaton abstraction, AutomatonEdge hiddenEdge){
        Guard hiddenGuard = (Guard) hiddenEdge.getMetaData("guard");
        List<AutomatonEdge> outgoingObservableEdges = hiddenEdge.getTo().getOutgoingEdges().stream()
                .filter(edge -> !edge.isHidden())
                .collect(Collectors.toList());

        List<AutomatonNode> incomingNodes = new ArrayList<AutomatonNode>();
        Set<String> visited = new HashSet<String>();
        Stack<AutomatonEdge> fringe = new Stack<AutomatonEdge>();
        fringe.push(hiddenEdge);

        while(!fringe.isEmpty()){
            AutomatonEdge current = fringe.pop();
            List<AutomatonEdge> incomingEdges = current.getFrom().getIncomingEdges();

            incomingNodes.add(abstraction.getNode(current.getFrom().getId() + ".abs"));

            incomingEdges.stream()
                    .filter(edge -> edge.isHidden() && !visited.contains(edge.getId()))
                    .forEach(edge -> fringe.push(edge));

            visited.add(current.getId());
        }

        for(AutomatonEdge edge : outgoingObservableEdges){
            AutomatonNode to = abstraction.getNode(edge.getTo().getId() + ".abs");
            Guard toGuard = (Guard) to.getMetaData("guard");
            Guard outGuard = null;
            if (toGuard != null && hiddenGuard == null) {
                outGuard = toGuard;
            } else if (toGuard == null && hiddenGuard != null) {
                outGuard = hiddenGuard;
            } else if (toGuard != null) {
                outGuard = smt.combineGuards(hiddenGuard,toGuard);
            }
            for (AutomatonNode from : incomingNodes) {
                AutomatonEdge edge1 = abstraction.addEdge(edge.getLabel(), from, to);
                if (outGuard != null) {
                    edge1.addMetaData("guard",outGuard);
                }
            }
        }
    }

    private void addNode(Automaton abstraction, AutomatonNode node){
        AutomatonNode newNode = abstraction.addNode(node.getId() + ".abs");
        for(String key : node.getMetaDataKeys()){
            newNode.addMetaData(key, node.getMetaData(key));
            if(key.equals("startNode")){
                abstraction.setRoot(newNode);
            }
        }
    }

    private void addEdge(Automaton abstraction, AutomatonEdge edge){
        AutomatonNode from = abstraction.getNode(edge.getFrom().getId() + ".abs");
        AutomatonNode to = abstraction.getNode(edge.getTo().getId() + ".abs");
        AutomatonEdge newEdge = abstraction.addEdge(edge.getLabel(), from, to);
        for(String key : edge.getMetaDataKeys()){
            newEdge.addMetaData(key, edge.getMetaData(key));
        }
    }
}
