package mc.processmodels.conversion;

import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
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
  public static Automaton tokenRule(Petrinet convertFrom, Map<Set<PetriNetPlace>, AutomatonNode> markingToNodeMap,
                                    Map<AutomatonNode, Set<PetriNetPlace> > nodeToMarkingMap) {
    Automaton outputAutomaton = new Automaton(convertFrom.getId() + " automata",
        false);
  //System.out.println("\nTOKEN RULE \n STARTING "+convertFrom.myString());
    convertFrom.validatePNet();

   //System.out.println(convertFrom.getRoots());
    List<Set<PetriNetPlace>> rootsPlaces = new ArrayList<Set<PetriNetPlace>>();

    for(Set<String> rnames: convertFrom.getRoots()) {
     //System.out.println("rnames "+ rnames);
      AutomatonNode root = outputAutomaton.addNode();
      root.setStartNode(true);
      outputAutomaton.addRoot(root);
      Set<PetriNetPlace> rts = rnames.stream().
        map(x-> convertFrom.getPlaces().get(x)).collect(Collectors.toSet());
      rootsPlaces.add(rts);
      markingToNodeMap.put(rts, root);
      nodeToMarkingMap.put(root, rts);
    }

   //System.out.println("rootsPlaces "+ rootsPlaces);
    Stack<Set<PetriNetPlace>> toDo = new Stack<>();
    toDo.addAll(rootsPlaces);

    Set<Set<PetriNetPlace>> previouslyVisitedPlaces = new HashSet<>();
    int nodesCreated = 1;

    while (!toDo.isEmpty()) {
      Set<PetriNetPlace> currentMarking = toDo.pop();
      //System.out.println("Starting "+previouslyVisitedPlaces.size()+
      //   " "+Petrinet.marking2String(currentMarking));
      if (previouslyVisitedPlaces.contains(currentMarking)) {
        //System.out.println("Visted!");
        continue;
      }

      Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking);
      //System.out.println("Processing "+Petrinet.marking2String(currentMarking)+
      //" trans "+satisfiedPostTransitions.size());
      if (satisfiedPostTransitions.size() == 0) {
        boolean stop = true;
        for (PetriNetPlace pl: currentMarking) {
          if (!pl.getTerminal().equals("STOP")) stop = false;
        }
        if (stop)
          markingToNodeMap.get(currentMarking).setTerminal("STOP");
        else
          markingToNodeMap.get(currentMarking).setTerminal("ERROR");
      }

      for (PetriNetTransition transition : satisfiedPostTransitions) {
        //System.out.println("Next tran "+transition.myString());
        Set<PetriNetPlace> newMarking = new HashSet<>(currentMarking);
        // Clear out the places in the current marking which are moving token
        newMarking.removeAll(transition.pre());

        newMarking.addAll(transition.getOutgoing().stream()
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .collect(Collectors.toList()));
        //System.out.println("newMarking "+ Petrinet.marking2String(newMarking));

        if (!markingToNodeMap.containsKey(newMarking)) {
          AutomatonNode newNode = outputAutomaton.addNode();
          newNode.setLabelNumber(nodesCreated++);
          markingToNodeMap.put(newMarking, newNode);
          nodeToMarkingMap.put(newNode, newMarking);
          toDo.add(newMarking);
        }
        Set<String> own =  convertFrom.getTranOwners(transition);
        AutomatonEdge ed =
        outputAutomaton.addEdge(transition.getLabel(), markingToNodeMap.get(currentMarking),
            markingToNodeMap.get(newMarking), null, false);
        ed.setAutomatonLocation(own);
        //System.out.println(" automaton now "+outputAutomaton.myString());
      }
      previouslyVisitedPlaces.add(currentMarking);
      //System.out.println("ENDing "+previouslyVisitedPlaces.size()+"  "+Petrinet.marking2String(currentMarking));
    }
   //System.out.println("Token Out "+outputAutomaton.myString());
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
