package mc.processmodels.automata.operations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import mc.Constant;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

/**
 * Created by sheriddavi on 25/01/17.
 */
public class AutomataReachability {

  /**
   * Finds and removes the unreachable @code{AutomatonNodes} from the specified @code{Automaton}.
   * This results in an @code{Automaton} that contains only reachable @code{AutomatonNodes}. The
   * process is executed on the specified @code{Automaton}, which is then returned.
   *
   * @param automaton -- the @code{Automaton} to find unreachable @code{AutomatonNodes} for
   * @return -- the processed @code{Automaton}
   */
  public static Automaton removeUnreachableNodes(Automaton automaton) throws CompilationException{

    //automaton = automaton.copy();  THIS was usless and now relabels the nodes
    Set<String> visited = new HashSet<>();
    Stack<AutomatonNode> fringe = new Stack<>();
    automaton.getRoot().forEach(fringe::push);


    // find the reachable nodes within the specified automaton
    while (!fringe.isEmpty()) {
      AutomatonNode current = fringe.pop();
     //System.out.println("fringe "+current.getId());

      // push the neighbouring nodes from the current node to the fringe
      // if they have not already been visited
      // Also remove any edges that point to objects not in this automaton
      List<AutomatonEdge> edgesToRemove = new ArrayList<>();
      for (AutomatonEdge e : current.getOutgoingEdges()) {
        // if (e.getLabel().equals(Constant.DEADLOCK)) continue;
        if (!automaton.getNodes().contains(e.getTo())) {
          edgesToRemove.add(e);
          continue;
        }

        if (!visited.contains(e.getTo().getId())) {
          fringe.push(e.getTo());
        }
      }
      automaton.getEdges().removeAll(edgesToRemove);
      current.getOutgoingEdges().removeAll(edgesToRemove);


      // mark the current node as being visited
      visited.add(current.getId());
    }

    // remove the nodes that were not reached during the traversal
    automaton.getNodes().stream()
        .filter(node -> !visited.contains(node.getId()))
        .forEach(automaton::removeNode);


    // terminal nodes not marked with "STOP" are marked "ERROR"
    automaton.getNodes().stream()
        .filter(node -> node.getOutgoingEdges().size() == 0 && !node.isTerminal())
        .forEach(node -> node.setErrorNode(true));


    return automaton;
  }

}
