package mc.processmodels.conversion;


import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by smithjord3 on 23/01/18.
 * Method algorithm for converting an Petrinet to automaton
 */
public class TokenRule {

  public static Automaton tokenRule(Petrinet convertFrom) throws CompilationException {
    Automaton outputAutomaton = new Automaton(convertFrom.getId() + " automata", false);

    Map<Set<PetriNetPlace>, AutomatonNode> nodeMap = new HashMap<>();

    AutomatonNode root = outputAutomaton.addNode();
    root.setStartNode(true);
    outputAutomaton.addRoot(root);

    nodeMap.put(convertFrom.getRoots(), root);

    Stack<Set<PetriNetPlace>> toDo = new Stack<>();
    toDo.push(convertFrom.getRoots());

    Set<Set<PetriNetPlace>> previouslyVisitiedPlaces = new HashSet<>();
    int nodesCreated = 1;
    while (!toDo.isEmpty()) {
      Set<PetriNetPlace> currentMarking = toDo.pop();
      if (previouslyVisitiedPlaces.contains(currentMarking)) {
        continue;
      }

      Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking);

      if (satisfiedPostTransitions.size() == 0) {
        nodeMap.get(currentMarking).setTerminal("STOP");
      }


      for (PetriNetTransition transition : satisfiedPostTransitions) {


        Set<PetriNetPlace> newMarking = new HashSet<>(currentMarking);
        newMarking.removeAll(pre(transition)); // Clear out the places in the current marking which are moving token

        newMarking.addAll(transition.getOutgoing().stream()
            .map(outEdge -> (PetriNetPlace) outEdge.getTo()).collect(Collectors.toList()));


        if (!nodeMap.containsKey(newMarking)) {
          AutomatonNode newNode = outputAutomaton.addNode();
          newNode.setLabelNumber(nodesCreated);
          nodeMap.put(newMarking, newNode);
          toDo.add(newMarking);
          nodesCreated++;
        }
        outputAutomaton.addEdge(transition.getLabel(), nodeMap.get(currentMarking), nodeMap.get(newMarking), null);

      }

      previouslyVisitiedPlaces.add(currentMarking);

    }


    return outputAutomaton;
  }

  private static Set<PetriNetTransition> satisfiedTransitions(Set<PetriNetPlace> currentMarking) {
    Set<PetriNetTransition> potentialTransitions = post(currentMarking);

    return potentialTransitions.stream().filter(transition -> currentMarking.containsAll(pre(transition))).collect(Collectors.toSet());
  }


  private static Set<PetriNetTransition> post(Set<PetriNetPlace> currentMarking) {
    Set<PetriNetTransition> output = new HashSet<>();


    for (PetriNetPlace place : currentMarking) {
      output.addAll(place.getOutgoing().stream().map(outgoingEdge -> (PetriNetTransition) outgoingEdge.getTo()).collect(Collectors.toList()));
    }

    return output;
  }

  private static Set<PetriNetPlace> pre(PetriNetTransition transtionToGetPrePlaces) {
    return transtionToGetPrePlaces.getIncoming().stream().map(incomingEdge -> (PetriNetPlace) incomingEdge.getFrom()).collect(Collectors.toSet());
  }


}
