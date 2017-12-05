package mc.process_models.automata.operations;

import com.microsoft.z3.Context;
import mc.Constant;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import mc.util.expr.Expression;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sheriddavi on 25/01/17.
 */
public class AutomataAbstraction {

    public Automaton performAbstraction(Automaton automaton, boolean isFair, Context context) throws CompilationException, InterruptedException {
        Automaton abstraction = new Automaton(automaton.getId() + ".abs", !Automaton.CONSTRUCT_ROOT);

        // add the nodes from the specified automaton to the abstracted representation
        for (AutomatonNode automatonNode : automaton.getNodes()) {
            addNode(abstraction, automatonNode);
        }

        // only add the observable edges from the specified automaton to the abstracted representation
        for (AutomatonEdge automatonEdge : automaton.getEdges()) {
            if(!automatonEdge.isHidden()){
                addEdge(abstraction, automatonEdge);
            }
        }

        // retrieve the unobservable edges from the specified automaton
        List<AutomatonEdge> hiddenEdges = automaton.getEdges().stream()
            .filter(AutomatonEdge::isHidden)
            .collect(Collectors.toList());

        List<AutomatonNode> toRemove = new ArrayList<>();
        // construct observable edges to replace the unobservable edges
        for(AutomatonEdge hiddenEdge : hiddenEdges){
            AutomatonNode b = constructEdgeOnlyTau(abstraction,hiddenEdge,isFair,context);
            toRemove.add(b);
            constructOutgoingObservableEdges(abstraction, hiddenEdge, isFair,context);
            constructIncomingObservableEdges(abstraction, hiddenEdge, isFair,context);
        }
        toRemove.forEach(s -> {
            if (s != null) {
                abstraction.removeNode(s);
            }
        });
        return abstraction;
    }

    private AutomatonNode constructEdgeOnlyTau(Automaton abstraction, AutomatonEdge hiddenEdge, boolean isFair, Context context) throws CompilationException {
        if (!hiddenEdge.getFrom().getOutgoingEdges().stream().allMatch(AutomatonEdge::isHidden)) {
            return null;
        }
        if (hiddenEdge.getFrom().isStartNode()) return null;
        AutomatonNode to = abstraction.getNode(hiddenEdge.getTo().getId() + ".abs");
        if (hiddenEdge.getTo() == hiddenEdge.getFrom()) {
            return null;
        }
        List<AutomatonEdge> incomingObservableEdges = hiddenEdge.getFrom().getIncomingEdges().stream()
            .filter(edge -> !edge.isHidden())
            .collect(Collectors.toList());
        for (AutomatonEdge edge : incomingObservableEdges) {
            AutomatonNode from = abstraction.getNode(edge.getFrom().getId() + ".abs");
            abstraction.addEdge(edge.getLabel(),from,to, from.getGuard());
        }
        return abstraction.getNode(hiddenEdge.getFrom().getId()+".abs");
    }

    private void constructOutgoingObservableEdges(Automaton abstraction, AutomatonEdge hiddenEdge, boolean isFair, Context context) throws CompilationException, InterruptedException {
        Guard hiddenGuard = hiddenEdge.getGuard();
        List<AutomatonEdge> incomingObservableEdges = hiddenEdge.getFrom().getIncomingEdges().stream()
            .filter(edge -> !edge.isHidden())
            .collect(Collectors.toList());

        List<AutomatonNode> outgoingNodes = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Stack<AutomatonEdge> fringe = new Stack<>();
        fringe.push(hiddenEdge);

        while(!fringe.isEmpty()){
            AutomatonEdge current = fringe.pop();

            // if the tau path returns to the specified hidden edge then there is a tau loop
            if(visited.contains(current.getId()) && current == hiddenEdge){
                // if abstraction is not fair, need to specify that process could deadlock in an infinite tau loop
                if(!isFair) {
                    AutomatonNode deadlockNode = abstraction.addNode();
                    deadlockNode.setTerminal("ERROR");

                    AutomatonNode from = abstraction.getNode(hiddenEdge.getFrom().getId() + ".abs");
                    abstraction.addEdge(Constant.DEADLOCK, from, deadlockNode,null);
                }

                // edge has already been processed
                continue;
            }
            //Check if an edge has already been processed.
            if (visited.contains(current.getId())) {
                continue;
            }
            List<AutomatonEdge> outgoingEdges = current.getTo().getOutgoingEdges();

            outgoingNodes.add(abstraction.getNode(current.getTo().getId() + ".abs"));
            outgoingEdges.stream()
                .filter(AutomatonEdge::isHidden)
                .forEach(fringe::push);

            visited.add(current.getId());
        }

        for(AutomatonEdge edge : incomingObservableEdges){
            AutomatonNode from = abstraction.getNode(edge.getFrom().getId() + ".abs");
            Guard fromGuard = from.getGuard();
            Guard outGuard = null;
            if (fromGuard != null && hiddenGuard == null) {
                outGuard = fromGuard;
            } else if (fromGuard == null && hiddenGuard != null) {
                outGuard = hiddenGuard;
            } else if (fromGuard != null) {
                outGuard = Expression.combineGuards(hiddenGuard,fromGuard,context);
            }
            for (AutomatonNode to : outgoingNodes) {
                if (outGuard != null) {
                    abstraction.addEdge(edge.getLabel(), from, to, outGuard);
                } else {
                    abstraction.addEdge(edge.getLabel(), from, to, null);
                }



            }
        }
    }

    private void constructIncomingObservableEdges(Automaton abstraction, AutomatonEdge hiddenEdge, boolean isFair, Context context) throws CompilationException, InterruptedException {
        Guard hiddenGuard = hiddenEdge.getGuard();
        List<AutomatonEdge> outgoingObservableEdges = hiddenEdge.getTo().getOutgoingEdges().stream()
            .filter(edge -> !edge.isHidden())
            .collect(Collectors.toList());

        List<AutomatonNode> incomingNodes = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Stack<AutomatonEdge> fringe = new Stack<>();
        fringe.push(hiddenEdge);

        while(!fringe.isEmpty()){
            AutomatonEdge current = fringe.pop();

            List<AutomatonEdge> incomingEdges = current.getFrom().getIncomingEdges();

            incomingNodes.add(abstraction.getNode(current.getFrom().getId() + ".abs"));

            incomingEdges.stream()
                .filter(edge -> edge.isHidden() && !visited.contains(edge.getId()))
                .forEach(fringe::push);

            visited.add(current.getId());
        }

        for(AutomatonEdge edge : outgoingObservableEdges){
            AutomatonNode to = abstraction.getNode(edge.getTo().getId() + ".abs");
            Guard toGuard = to.getGuard();
            Guard outGuard = null;
            if (toGuard != null && hiddenGuard == null) {
                outGuard = toGuard;
            } else if (toGuard == null && hiddenGuard != null) {
                outGuard = hiddenGuard;
            } else if (toGuard != null) {
                outGuard = Expression.combineGuards(hiddenGuard,toGuard,context);
            }
            for (AutomatonNode from : incomingNodes) {
                abstraction.addEdge(edge.getLabel(), from, to, outGuard);
            }
        }
    }

    private void addNode(Automaton abstraction, AutomatonNode node) throws CompilationException {
        AutomatonNode newNode = abstraction.addNode(node.getId() + ".abs");

        newNode.copyProperties(node);
        if(newNode.isStartNode())
            abstraction.setRoot(newNode);

    }

    private void addEdge(Automaton abstraction, AutomatonEdge edge) throws CompilationException {
        AutomatonNode from = abstraction.getNode(edge.getFrom().getId() + ".abs");
        AutomatonNode to = abstraction.getNode(edge.getTo().getId() + ".abs");
        abstraction.addEdge(edge.getLabel(), from, to, edge.getGuard());
    }
}
