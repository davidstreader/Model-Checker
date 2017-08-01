package mc.process_models.automata.operations;

import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sheriddavi on 13/02/17.
 */
public class AutomataPruning {

    public Automaton performPruning(Automaton automaton) throws CompilationException {
        // find the hidden edges within the automaton
        List<AutomatonEdge> hiddenEdges = automaton.getEdges().stream()
                .filter(AutomatonEdge::isHidden)
                .collect(Collectors.toList());

        // if there are no hidden edges then there is nothing to prune
        if(hiddenEdges.isEmpty()){
            return automaton;
        }

        for (AutomatonEdge hiddenEdge : hiddenEdges) {
            AutomatonNode from = hiddenEdge.getFrom();
            AutomatonNode to = hiddenEdge.getTo();

            List<AutomatonEdge> incomingHiddenEdges = from.getIncomingEdges().stream()
                .filter(AutomatonEdge::isHidden)
                .collect(Collectors.toList());

            // if there are incoming hidden edges then we cannot prune the current edge
            if (!incomingHiddenEdges.isEmpty()) {
                continue;
            }

            List<AutomatonEdge> outgoingHiddenEdges = to.getOutgoingEdges().stream()
                .filter(AutomatonEdge::isHidden)
                .collect(Collectors.toList());

            // if there are outgoing hidden edges then we cannot prune the current edge
            if (!outgoingHiddenEdges.isEmpty()) {
                continue;
            }

            // since there are no incoming or outgoing hidden edges we can merge the two nodes
            automaton.combineNodes(from, to);
            automaton.removeEdge(hiddenEdge);
        }

        return automaton;
    }

}
