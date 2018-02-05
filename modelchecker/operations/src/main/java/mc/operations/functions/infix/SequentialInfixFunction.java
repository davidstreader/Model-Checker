package mc.operations.functions.infix;

import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.AutomataReachability;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;

public class SequentialInfixFunction implements IProcessInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "sequential";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "=>";
  }

  /**
   * Execute the function.
   *
   * @param id         the id of the resulting automaton
   * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
   * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2) {
    Automaton sequence = new Automaton(id, !Automaton.CONSTRUCT_ROOT);
    Multimap<String,String> setOfOwners = AutomatonEdge.createIntersection(automaton1.getOwners(),
        automaton2.getOwners());

    //store a map to the nodes so id can be ignored
    Map<String, AutomatonNode> automata1nodes = new HashMap<>();
    Map<String, AutomatonNode> automata2nodes = new HashMap<>();

    //copy node1 nodes across
    AutomataReachability.removeUnreachableNodes(automaton1).getNodes().forEach(node -> {
      try {
        AutomatonNode newNode = sequence.addNode();
        newNode.copyProperties(node);
        automata1nodes.put(node.getId(), newNode);

        if (newNode.isStartNode()) {
          sequence.addRoot(newNode);
        }

      } catch (CompilationException e) {
        e.printStackTrace();
      }
    });

    copyAutomataEdges(sequence, automaton1, automata1nodes,setOfOwners);

    //get the stop nodes such that they can be replaced
    Collection<AutomatonNode> stopNodes = sequence.getNodes().stream()
        .filter(n -> "STOP".equals(n.getTerminal()))
        .collect(Collectors.toList());


    //if there are no stop nodes, we cannot glue them together
    if (stopNodes.isEmpty()) {
      return sequence;
    }

    AutomataReachability.removeUnreachableNodes(automaton2).getNodes().forEach(node -> {
      AutomatonNode newNode = sequence.addNode();
      newNode.copyProperties(node);

      automata2nodes.put(node.getId(), newNode);

      if (newNode.isStartNode()) {
        newNode.setStartNode(false);
        // for every stop node of automata1, get the edges that go into it
        // replace it with the start node of automata2
        for (AutomatonNode stopNode : stopNodes) {
          for (AutomatonEdge edge : stopNode.getIncomingEdges()) {
            AutomatonNode origin = edge.getFrom();
            try {
              sequence.addEdge(edge.getLabel(), origin, newNode,
                  edge.getGuard() == null ? null : edge.getGuard().copy(), true);
            } catch (CompilationException e) {
              e.printStackTrace();
            }
          }
        }
      }
    });
    stopNodes.stream().map(AutomatonNode::getIncomingEdges)
        .flatMap(List::stream)
        .forEach(sequence::removeEdge);
    stopNodes.forEach(sequence::removeNode);

    copyAutomataEdges(sequence, automaton2, automata2nodes,setOfOwners);

    return sequence;
  }

  /**
   * Execute the function.
   *
   * @param id        the id of the resulting petrinet
   * @param petrinet1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param petrinet2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting petrinet of the operation
   */
  @Override
  public Petrinet compose(String id, Petrinet petrinet1, Petrinet petrinet2)
      throws CompilationException {

    //create an empty petrinet
    Petrinet composition = new Petrinet(id, false);

    //add the first petrinet to the sequential composition
    //add the start nodes of petrinet1 as start nodes for the composition petrinet
    composition.addPetrinet(petrinet1).forEach(p -> {
      p.setStart(true);
      composition.addRoot(p);
    });

    //get the STOP states (not ERROR) places
    Set<PetriNetPlace> stopNodes = composition.getPlaces().values().stream()
        .filter(PetriNetPlace::isTerminal)
        .filter(p -> p.getTerminal().equalsIgnoreCase("STOP"))
        .collect(Collectors.toSet());

    //add the second petrinet
    Set<PetriNetPlace> startOfP2 = composition.addPetrinet(petrinet2);
    //merge the end of petri1 with the start of petri2
    composition.gluePlaces(stopNodes, startOfP2);

    return composition;
  }

  /**
   * Copies the edges from one automata to another.
   *
   * @param writeAutomaton the automata that will have the edges copied to it
   * @param readAutomaton  the automata that will have the edges copied from it
   * @param nodeMap        the mapping of the ids to AutomatonNodes
   */
  private void copyAutomataEdges(Automaton writeAutomaton, Automaton readAutomaton,
                                 Map<String, AutomatonNode> nodeMap,
                                 Multimap<String,String> edgeOwnersMap) {
    readAutomaton.getEdges().forEach(e -> {
      try {
        AutomatonNode fromNode = nodeMap.get(e.getFrom().getId());
        AutomatonNode toNode = nodeMap.get(e.getTo().getId());
        writeAutomaton.addOwnersToEdge(
            writeAutomaton.addEdge(e.getLabel(), fromNode, toNode, e.getGuard(),
                false), getEdgeOwnersFromProduct(e.getOwnerLocation(),edgeOwnersMap));
      } catch (CompilationException e1) {
        e1.printStackTrace();
      }
    });
  }

  private Set<String> getEdgeOwnersFromProduct(Set<String> edgeOwners,
                                               Multimap<String,String> productSpace) {
    return edgeOwners.stream().map(productSpace::get)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

}
