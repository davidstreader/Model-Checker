package mc.process_models.automata.operations;

import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.jar.Pack200;
import java.util.stream.Collectors;

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
        Set<String> visited = new HashSet<String>();
        Stack<AutomatonNode> fringe = new Stack<AutomatonNode>();
        fringe.push(automaton.getRoot());

        // find the reachable nodes within the specified automaton
        while(!fringe.isEmpty()){
            AutomatonNode current = fringe.pop();

            // push the neighbouring nodes from the current node to the fringe
            // if they have not already been visited
            current.getOutgoingEdges().stream()
                    .map(edge -> edge.getTo())
                    .filter(node -> !visited.contains(node.getId()))
                    .forEach(node -> fringe.push(node));

            // mark the current node as being visited
            visited.add(current.getId());
        }

        // remove the nodes that were not reached during the traversal
        automaton.getNodes().stream()
                .filter(node -> !visited.contains(node.getId()))
                .forEach(node -> automaton.removeNode(node));

        return automaton;
    }

}
