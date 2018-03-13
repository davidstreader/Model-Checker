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

    System.out.println("Reachability "+petri.myString());
    petri = petri.copy();
    Stack<Set<PetriNetPlace>> toDo = new Stack<>();
    toDo.push(petri.getRoots());

    Set<Set<PetriNetPlace>> previouslyVisitedPlaces = new HashSet<>();
    Set<PetriNetPlace> visitedPlaces = new HashSet<>();
    Set<PetriNetTransition> visitedTransitions = new HashSet<>();

    while (!toDo.isEmpty()) {
      Set<PetriNetPlace> currentMarking = toDo.pop();
      System.out.println("Visited "+Petrinet.marking2String(currentMarking));
      visitedPlaces.addAll(currentMarking);

      if (previouslyVisitedPlaces.contains(currentMarking)) {
        continue;
      }

      System.out.println("MARKING: "+Petrinet.marking2String(currentMarking));
      System.out.println("Post "+Petrinet.trans2String(post(currentMarking)));
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

        if (!previouslyVisitedPlaces.contains(newMarking)) {
          toDo.add(newMarking);
        }
      }
      previouslyVisitedPlaces.add(currentMarking);
    }
    Set<PetriNetPlace> placesToRemove = new HashSet<>(petri.getPlaces().values());
    placesToRemove.removeAll(visitedPlaces);
    System.out.println("All Vis "+Petrinet.marking2String(visitedPlaces));
    System.out.println("All Rem "+Petrinet.marking2String(placesToRemove));
    Set<PetriNetTransition> transitionsToRemove = new HashSet<> (petri.getTransitions().values());
    transitionsToRemove.removeAll(visitedTransitions);

    for (PetriNetPlace p : placesToRemove) {
      petri.removePlace(p);
    }
    for (PetriNetTransition t : transitionsToRemove) {
      petri.removeTransition(t);
    }
  
    System.out.println("REACH  end "+ petri.myString()+"REACH END \n");
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
