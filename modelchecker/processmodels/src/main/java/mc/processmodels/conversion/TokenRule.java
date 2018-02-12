package mc.processmodels.conversion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;


/**
 * This holds static methods related to the generation of automata from petrinets.
 * <p>
 * This is to be used such that the conversion between the two can be completed, and for being able
 * to make petrinets and automata interoperable within process definitions.
 *
 * @author Jordan Smith
 * @author David Streader
 * @author Jacob Beal
 * @see <a href="http://doi.org/10.1006/inco.2002.3117">
 * The Box Algebra = Petri Nets + Process Expressions
 * </a>
 * @see Petrinet
 * @see PetriNetPlace
 * @see Automaton
 * @see AutomatonNode
 */
public class TokenRule {



  public static Automaton tokenRule(Petrinet convertFrom){
    return tokenRule(convertFrom, new HashMap<>(), new HashMap<>());
  }
  /**
   * This method statically converts from a Petrinet to an Automaton visualisation of a given
   * process.
   *
   * @param convertFrom the petrinet that is converted from.
   * @param markingToNodeMap the mapping from the marking to an automaton node, used for display
   * @param nodeToMarkingMap the mapping from node to marking
   * @return The automaton that is equivalent to {@code convertFrom} petrinet.
   */
  @SneakyThrows(value = {CompilationException.class})
  public static Automaton tokenRule(Petrinet convertFrom, Map<Set<PetriNetPlace>, AutomatonNode> markingToNodeMap, Map<AutomatonNode, Set<PetriNetPlace> > nodeToMarkingMap) {
    Automaton outputAutomaton = new Automaton(convertFrom.getId() + " automata",
        false);

    AutomatonNode root = outputAutomaton.addNode();
    root.setStartNode(true);
    outputAutomaton.addRoot(root);

    markingToNodeMap.put(convertFrom.getRoots(), root);
    nodeToMarkingMap.put(root, convertFrom.getRoots());

    Stack<Set<PetriNetPlace>> toDo = new Stack<>();
    toDo.push(convertFrom.getRoots());

    Set<Set<PetriNetPlace>> previouslyVisitedPlaces = new HashSet<>();
    int nodesCreated = 1;

    while (!toDo.isEmpty()) {
      Set<PetriNetPlace> currentMarking = toDo.pop();
      if (previouslyVisitedPlaces.contains(currentMarking)) {
        continue;
      }


      Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking);

      if (satisfiedPostTransitions.size() == 0) {
        markingToNodeMap.get(currentMarking).setTerminal("STOP");
      }


      for (PetriNetTransition transition : satisfiedPostTransitions) {
        Set<PetriNetPlace> newMarking = new HashSet<>(currentMarking);


        // Clear out the places in the current marking which are moving token
        newMarking.removeAll(transition.pre());

        newMarking.addAll(transition.getOutgoing().stream()
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .collect(Collectors.toList()));

        if (!markingToNodeMap.containsKey(newMarking)) {
          AutomatonNode newNode = outputAutomaton.addNode();
          newNode.setLabelNumber(nodesCreated++);
          markingToNodeMap.put(newMarking, newNode);
          nodeToMarkingMap.put(newNode, newMarking);
          toDo.add(newMarking);
        }

        outputAutomaton.addEdge(transition.getLabel(), markingToNodeMap.get(currentMarking),
            markingToNodeMap.get(newMarking), null, false);
      }
      previouslyVisitedPlaces.add(currentMarking);
    }
    return outputAutomaton;
  }

  private static Set<PetriNetTransition> satisfiedTransitions(Set<PetriNetPlace> currentMarking) {
    return post(currentMarking).stream()
        .filter(transition -> currentMarking.containsAll(transition.pre()))
        .distinct()
        .collect(Collectors.toSet());
  }


  private static Set<PetriNetTransition> post(Set<PetriNetPlace> currentMarking) {
    return currentMarking.stream()
        .map(PetriNetPlace::post)
        .flatMap(Set::stream)
        .distinct()
        .collect(Collectors.toSet());
  }

}
