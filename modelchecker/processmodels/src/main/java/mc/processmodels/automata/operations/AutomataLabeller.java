package mc.processmodels.automata.operations;

import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

import java.util.List;

/**
 * Created by sheriddavi on 25/01/17.
 */
public class AutomataLabeller {

  public static Automaton labelAutomaton(Automaton automaton, String label)
    throws CompilationException {
    System.out.println("INPUT "+automaton.myString());
    Automaton labelled = new Automaton(label + ":" + automaton.getId(), !Automaton.CONSTRUCT_ROOT);
    List<AutomatonNode> nodes = automaton.getNodes();
    for (AutomatonNode node : nodes) {
      AutomatonNode newNode = labelled.addNode(label + ":" + node.getId());
      newNode.copyProperties(node);
      if (newNode.isStartNode()) {
        labelled.addRoot(newNode);
      }
    }

    List<AutomatonEdge> edges = automaton.getEdges();
    for (AutomatonEdge edge : edges) {
      AutomatonNode from = labelled.getNode(label + ":" + edge.getFrom().getId());
      AutomatonNode to = labelled.getNode(label + ":" + edge.getTo().getId());
      labelled.addOwnersToEdge(
          labelled.addEdge(label + "." + edge.getLabel(), from, to, edge.getGuard(),false,edge.getOptionalEdge()),
          edge.getEdgeOwners());

    }

    labelled.copyProperties(automaton);

    return labelled;
  }

}
