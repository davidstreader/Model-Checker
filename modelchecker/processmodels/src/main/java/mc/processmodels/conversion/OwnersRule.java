package mc.processmodels.conversion;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import lombok.Builder;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

public class OwnersRule {

  public static Petrinet ownersRule(Automaton a) throws CompilationException {

    Petrinet petri = new Petrinet(a.getId(), false);

    //Create the separate "tracks" for each owner.
    Map<String, PetriNetPlace> ownerToPlaceMap = new HashMap<>();
    a.getOwners().forEach(o -> ownerToPlaceMap.put(o, petri.addPlace()));

    //set these as the root
    ownerToPlaceMap.values().forEach(petri::addRoot);

    Stack<Location> fringe = new Stack<>();
    fringe.push(Location.builder()
        .automatonLocation(a.getRoot())
        .currentMarking(ownerToPlaceMap)
        .build());

    Set<AutomatonNode> visitedAutomataNodes = new HashSet<>();
    Map<AutomatonNode, Set<PetriNetPlace>> locationCorolation = new HashMap<>();
    while (!fringe.isEmpty()) {
      Location loc = fringe.pop();

      visitedAutomataNodes.addAll(loc.automatonLocation);
      loc.automatonLocation.forEach(an ->
          locationCorolation.put(an, new HashSet<>(loc.currentMarking.values())));

      Set<AutomatonEdge> outEdges = loc.automatonLocation.stream()
          .map(AutomatonNode::getOutgoingEdges)
          .flatMap(List::stream)
          .distinct()
          .collect(Collectors.toSet());

      for (AutomatonEdge edge : outEdges) {
        String label = edge.getLabel();
        Set<String> owners = edge.getOwnerLocation();
        PetriNetTransition transition = petri.addTransition(label);
        Set<PetriNetPlace> ownersToTransition = owners.stream()
            .map(loc.currentMarking::get)
            .collect(Collectors.toSet());

        for (PetriNetPlace p : ownersToTransition) {
          petri.addEdge(transition, p, owners);
        }

        if (visitedAutomataNodes.contains(edge.getTo())) {
          for (PetriNetPlace outPlace : locationCorolation.get(edge.getTo())) {
            petri.addEdge(outPlace,transition,owners);
          }
          continue;
        }

        Map<String,PetriNetPlace> outPlaces = new HashMap<>();
        owners.forEach(o -> outPlaces.put(o,petri.addPlace()));


        for (Map.Entry<String, PetriNetPlace> outPlace : outPlaces.entrySet()) {
          petri.addEdge(outPlace.getValue(),transition, Collections.singleton(outPlace.getKey()));
        }

        //new Token
        HashMap<String, PetriNetPlace> newMarking = new HashMap<>(loc.currentMarking);
        newMarking.putAll(outPlaces);

        fringe.push(Location.builder()
            .currentMarking(newMarking)
            .automatonLocation(Collections.singleton(edge.getTo()))
            .build());
      }
    }
    return petri;
  }

  /**
   * This stores locations within the petrinet and its corresponding marking.
   */
  @Builder
  private static class Location {
    Set<AutomatonNode> automatonLocation;
    Map<String, PetriNetPlace> currentMarking;
  }
}
