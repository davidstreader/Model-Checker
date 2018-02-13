package mc.processmodels.petrinet.operations;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

public final class PetrinetReachability {
  public static Petrinet removeUnreachableStates(Petrinet petri) throws CompilationException {
    petri = petri.copy();
    Stack<Set<PetriNetPlace>> toDo = new Stack<>();
    toDo.push(petri.getRoots());

    Set<Set<PetriNetPlace>> previouslyVisitedPlaces = new HashSet<>();
    Set<PetriNetPlace> visitedPlaces = new HashSet<>();
    Set<PetriNetTransition> visitedTransitions = new HashSet<>();

    while (!toDo.isEmpty()) {
      Set<PetriNetPlace> currentMarking = toDo.pop();

      visitedPlaces.addAll(currentMarking);

      if (previouslyVisitedPlaces.contains(currentMarking)) {
        continue;
      }


      Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking);

      for (PetriNetTransition transition : satisfiedPostTransitions) {
        visitedTransitions.add(transition);

        Set<PetriNetPlace> newMarking = new HashSet<>(currentMarking);


        // Clear out the places in the current marking which are moving token
        newMarking.removeAll(transition.pre());

        newMarking.addAll(transition.getOutgoing().stream()
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .collect(Collectors.toList()));

        if (!visitedPlaces.contains(newMarking)) {
          toDo.add(newMarking);
        }
      }
      previouslyVisitedPlaces.add(currentMarking);
    }
    Set<PetriNetPlace> placesToRemove = new HashSet<>(petri.getPlaces().values());
    placesToRemove.removeAll(visitedPlaces);
    Set<PetriNetTransition> transitionsToRemove = new HashSet<>(petri.getTransitions().values());
    transitionsToRemove.removeAll(visitedTransitions);

    for (PetriNetPlace p : placesToRemove) {
      petri.removePlace(p);
    }
    for (PetriNetTransition t : transitionsToRemove) {
      petri.removeTransititon(t);
    }
    return petri;
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
