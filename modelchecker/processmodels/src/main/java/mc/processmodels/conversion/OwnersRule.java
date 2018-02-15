package mc.processmodels.conversion;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.util.OwnersRuleColouring;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

public class OwnersRule {

  /**
   * This converts a deterministic automata to a petrinet.
   * <p>
   * TODO: Make this conversion work for NFA
   *
   * @param a The automaton to be converted
   * @return a petrinet representation fo the automaton
   */
  @SneakyThrows({CompilationException.class})
  public static Petrinet ownersRule(Automaton a) {
    a = OwnersRuleColouring.colour(a);
    Petrinet petri = new Petrinet(a.getId(), false);

    //Create the separate "tracks" for each owner.
    Map<String, PetriNetPlace> ownerToPlaceMap = new HashMap<>();
    a.getOwners().forEach(o -> ownerToPlaceMap.put(o, petri.addPlace()));

    //set these as the root
    ownerToPlaceMap.values().forEach(petri::addRoot);

    Stack<Location> fringe = new Stack<>();

    //build the first location
    fringe.push(Location.builder()
        .automatonLocation(a.getRoot())
        .currentMarking(ownerToPlaceMap)
        .build());

    Set<AutomatonNode> visitedAutomataNodes = new HashSet<>();
    Map<AutomatonNode, Set<PetriNetPlace>> locationCorolation = new HashMap<>();

    while (!fringe.isEmpty()) {
      Location loc = fringe.pop();

      //mark the current automaton as visited so it isnt visited again
      visitedAutomataNodes.addAll(loc.automatonLocation);

      //allow loops to work by storing the node->marking map
      loc.automatonLocation.forEach(an ->
          locationCorolation.put(an, new HashSet<>(loc.currentMarking.values())));

      //get all the outgoing edges of the location (this does it for a collection in the case of the
      //root node being Nondeterministic
      Set<AutomatonEdge> outEdges = loc.automatonLocation.stream()
          .map(AutomatonNode::getOutgoingEdges)
          .flatMap(List::stream)
          .distinct()
          .collect(Collectors.toSet());

      edge:
      for (AutomatonEdge edge : outEdges) {
        String label = edge.getLabel();
        Set<String> owners = edge.getOwnerLocation();

        List<Set<PetriNetTransition>> children = owners.stream()
            //get the positions this transition is coming from
            .map(loc.currentMarking::get)
            .filter(Objects::nonNull)
            //get any children of these post positions
            .map(PetriNetPlace::post)
            //ensure that each of the children have a transition with the same label
            .collect(Collectors.toList());

        //this is the rough check whether there is an existing transition of the same name
        boolean transitionExists = !children.isEmpty() && children.stream()
            .allMatch(set ->
                set.stream().map(PetriNetTransition::getLabel).anyMatch(l -> l.equals(label))
            );

        if (transitionExists) {
          //check for a transition in common
          Set<PetriNetTransition> commonTransitions = new HashSet<>(children.get(0));
          for (Set<PetriNetTransition> t : children) {
            commonTransitions.retainAll(t);
          }

          for (PetriNetTransition t : commonTransitions) {
            //if one of the transitions in common is the correct transition
            if (t.getLabel().equals(label)) {
              HashMap<String, PetriNetPlace> newMarking = new HashMap<>(loc.currentMarking);

              t.getOutgoing().forEach(e ->
                  e.getOwners().forEach(o -> newMarking.put(o,(PetriNetPlace)e.getTo()))
              );

              //add the new position to the stack to iterate through
              fringe.push(Location.builder()
                  .currentMarking(newMarking)
                  .automatonLocation(Collections.singleton(edge.getTo()))
                  .build());

              //break out of the loop for this edge
              continue edge;
            }
          }
        }
        PetriNetTransition transition = petri.addTransition(label);

        Set<PetriNetPlace> ownersToTransition = owners.stream()
            .map(loc.currentMarking::get)
            .collect(Collectors.toSet());

        //for each position to transition to, add a link to the transition
        for (PetriNetPlace p : ownersToTransition) {
          petri.addEdge(transition, p, owners);
        }

        //if this automaton visits an existing automaton edge, link back to the positioning
        if (visitedAutomataNodes.contains(edge.getTo())) {
          for (PetriNetPlace outPlace : locationCorolation.get(edge.getTo())) {
            petri.addEdge(outPlace, transition, owners);
          }
          continue;
        }

        //create a new place for each owner
        Map<String, PetriNetPlace> outPlaces = new HashMap<>();
        owners.forEach(o -> outPlaces.put(o, petri.addPlace()));


        //add edges to the new places
        for (Map.Entry<String, PetriNetPlace> outPlace : outPlaces.entrySet()) {
          petri.addEdge(outPlace.getValue(), transition, Collections.singleton(outPlace.getKey()));
        }

        //create a new marking to represent the node that the edge travelled to.
        HashMap<String, PetriNetPlace> newMarking = new HashMap<>(loc.currentMarking);
        //This will override any differences in positions
        newMarking.putAll(outPlaces);

        //add the new position to the stack to iterate through
        fringe.push(Location.builder()
            .currentMarking(newMarking)
            .automatonLocation(Collections.singleton(edge.getTo()))
            .build());
      }
    }
    markStopPlaces(petri);
    return petri;
  }

  private static void markStopPlaces(Petrinet p) {
    p.getPlaces().values().stream()
        .filter(pl -> pl.getOutgoing().size() == 0)
        .filter(((Predicate<PetriNetPlace>) PetriNetPlace::isTerminal).negate())
        .forEach(pl -> pl.setTerminal("STOP"));
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
