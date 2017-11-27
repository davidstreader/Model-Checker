package mc.process_models.automata.operations;

import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by sheriddavi on 25/01/17.
 */
public class AutomataReachability {

    /**
     * Finds and removes the unreachable @code{AutomatonNodes} from the specified @code{Automaton}.
     * This results in an @code{Automaton} that contains only reachable @code{AutomatonNodes}. The
     * process is executed on the specified @code{Automaton}, which is then returned.
     *
     * @param automaton
     *      -- the @code{Automaton} to find unreachable @code{AutomatonNodes} for
     * @return
     *      -- the processed @code{Automaton}
     */
    public Automaton removeUnreachableNodes(Automaton automaton){
        Set<String> visited = new HashSet<>();
        Stack<AutomatonNode> fringe = new Stack<>();
        fringe.push(automaton.getRoot());
        // find the reachable nodes within the specified automaton
        while(!fringe.isEmpty()){
            AutomatonNode current = fringe.pop();

            // push the neighbouring nodes from the current node to the fringe
            // if they have not already been visited
            current.getOutgoingEdges().stream()
                    .map(AutomatonEdge::getTo)
                    .filter(node -> !visited.contains(node.getId()))
                    .forEach(fringe::push);

            // mark the current node as being visited
            visited.add(current.getId());
        }

        // remove the nodes that were not reached during the traversal
        automaton.getNodes().stream()
                .filter(node -> !visited.contains(node.getId()))
                .forEach(automaton::removeNode);

        // make sure all terminal nodes are marked as terminal nodes
        automaton.getNodes().stream()
                .filter(node -> node.getOutgoingEdges().size() == 0 && !node.hasMetaData("isTerminal"))
                .forEach(node -> node.addMetaData("isTerminal", "STOP"));

        return automaton;
    }

}
