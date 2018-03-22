package mc.processmodels.petrinet.operations;

import java.util.*;
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
      //System.out.println("Visited "+Petrinet.marking2String(currentMarking));
      visitedPlaces.addAll(currentMarking);

      if (previouslyVisitedPlaces.contains(currentMarking)) {
        continue;
      }

      //System.out.println("MARKING: "+Petrinet.marking2String(currentMarking));
      //System.out.println("Post "+Petrinet.trans2String(post(currentMarking)));
      Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking);

      for (PetriNetTransition transition : satisfiedPostTransitions) {
        System.out.println(transition.myString()+"  Marking "
                          +Petrinet.marking2String(currentMarking));
        visitedTransitions.add(transition);

        Set<PetriNetPlace> newMarking = new HashSet<>(currentMarking);

        // Clear out the places in the current marking which are moving token
        newMarking.removeAll(transition.pre());

        newMarking.addAll(transition.getOutgoing().stream()
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .collect(Collectors.toList()));
        System.out.println("New Marking "+Petrinet.marking2String(newMarking));
        if (!previouslyVisitedPlaces.contains(newMarking)) {
          toDo.add(newMarking);
        }
      }
      previouslyVisitedPlaces.add(currentMarking);
    }
    Set<PetriNetPlace> placesToRemove = new HashSet<>(petri.getPlaces().values());
    placesToRemove.removeAll(visitedPlaces);
    //System.out.println("All Vis "+Petrinet.marking2String(visitedPlaces));
    //System.out.println("All Rem "+Petrinet.marking2String(placesToRemove));
    Set<PetriNetTransition> transitionsToRemove = new HashSet<> (petri.getTransitions().values());
    transitionsToRemove.removeAll(visitedTransitions);

    for (PetriNetPlace p : placesToRemove) {
      petri.removePlace(p);
    }

    System.out.println("Trans to Go "+ transitionsToRemove.stream().map(x->x.getId())
                            .collect(Collectors.toSet()));
    for (PetriNetTransition t : transitionsToRemove) {
      petri.removeTransition(t);
    }
/*
  If two places have  incoming and outgoing edges to the same Transitions then they can be merged
  This means that the owners must be merged.
 */
    Set<PetriNetPlace> togo = new HashSet<>();
    for (PetriNetPlace pl: petri.getPlaces().values()) {
      for (PetriNetPlace pl1: peer(pl)){

        if (  !pl.getId().equals(pl1.getId()) &&
             pl.post().equals(pl1.post()) && pl.pre().equals(pl1.pre())) {
          System.out.println("Merge "+pl.getId()+" with "+pl1.getId());
          if (!togo.contains(pl) && !togo.contains(pl1) ) {
            mergePlaces(pl,pl1);
            togo.add(pl1);
          }

        }
      }
    }
    for(PetriNetPlace p:togo) {
      petri.removePlace(p);
    }

   /*
  If two transitions have  incoming and outgoing edges to the same Places then they can be merged
  This means that the owners must be merged.
 */
    Set<PetriNetTransition> togoT = new HashSet<>();
    for (PetriNetTransition pl: petri.getTransitions().values()) {
      for (PetriNetTransition pl1: peerT(pl)){

        if (  !pl.getId().equals(pl1.getId()) &&
          pl.post().equals(pl1.post()) && pl.pre().equals(pl1.pre())
             && pl.getLabel().equals(pl1.getLabel())) {
          System.out.println("Merge "+pl.getId()+" with "+pl1.getId());
          if (!togoT.contains(pl) && !togoT.contains(pl1) ) {
            mergeTrans(pl,pl1);
            togoT.add(pl1);
          }

        }
      }
    }
    for(PetriNetTransition p:togoT) {
      petri.removeTransition(p);
    }
    System.out.println("REACH  end "+ petri.myString()+"REACH END");
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
  private static Set<PetriNetTransition> pre(Set<PetriNetPlace> currentMarking) {
    return currentMarking.stream()
      .map(PetriNetPlace::pre)
      .flatMap(Set::stream)
      .distinct()
      .collect(Collectors.toSet());
  }
  private static Set<PetriNetPlace> peer(PetriNetPlace current) {

    Set<PetriNetPlace> union = new HashSet<>(current.post().stream()
      .map(PetriNetTransition::pre).flatMap(Set::stream)
      .distinct().collect(Collectors.toSet()));

    union.addAll(current.pre().stream()
      .map(PetriNetTransition::post).flatMap(Set::stream)
      .distinct().collect(Collectors.toSet()));
    return union;
  }
  private static Set<PetriNetTransition> peerT(PetriNetTransition current) {
    return current.post().stream()
      .map(PetriNetPlace::pre).flatMap(Set::stream)
      .distinct().collect(Collectors.toSet());
  }
  private static void mergePlaces(PetriNetPlace p1,  PetriNetPlace p2){
    Map<PetriNetEdge,Set<String>> work = new HashMap<PetriNetEdge,Set<String>>();
    Set<String> union = new HashSet<String>();
    for (PetriNetEdge ed1: p1.getOutgoing()) {
      for (PetriNetEdge ed2 : p2.getOutgoing()) {
        System.out.println(ed1.myString()+" -- "+ ed2.myString());
        if (!ed1.getId().equals(ed2.getId()) &&  ed1.getTo().equals(ed2.getTo())) {
         union.addAll(ed1.getOwners());
         union.addAll(ed2.getOwners());
         work.put(ed1,union);
        }
      }
    }
    for(PetriNetEdge ed1: p1.getOutgoing()){
      if (work.get(ed1) != null ) ed1.setOwners(work.get(ed1));
      else System.out.println("WHAT work.get("+ed1.getId()+") = null");
    }
    work.clear();
    for (PetriNetEdge ed1: p1.getIncoming()) {
      for (PetriNetEdge ed2: p2.getIncoming()){
        if (!ed1.getId().equals(ed2.getId()) && ed1.getTo().equals(ed2.getTo())){
          System.out.println(ed1.myString()+" -- "+ ed2.myString());
          union.addAll(ed1.getOwners());
          union.addAll(ed2.getOwners());
          work.put(ed1,union);
        }
      }
    }
    for(PetriNetEdge ed1: p1.getIncoming()){
      if (work.get(ed1) != null ) ed1.setOwners(work.get(ed1));
      else System.out.println("WHAT work.get("+ed1.getId()+") = null");
    }
  }

  //Doing the work
  private static void mergeTrans(PetriNetTransition p1,  PetriNetTransition p2){
    Map<PetriNetEdge,Set<String>> work = new HashMap<PetriNetEdge,Set<String>>();
    Set<String> union = new HashSet<String>();
    for (PetriNetEdge ed1: p1.getOutgoing()) {
      for (PetriNetEdge ed2 : p2.getOutgoing()) {
 //System.out.println(ed1.myString()+" -- "+ ed2.myString());
        if (!ed1.getId().equals(ed2.getId()) &&  ed1.getTo().equals(ed2.getTo())) {
          union.addAll(ed1.getOwners());
          union.addAll(ed2.getOwners());
          work.put(ed1,union);
        }
      }
    }
    for(PetriNetEdge ed1: p1.getOutgoing()){
      if (work.get(ed1) != null ) ed1.setOwners(work.get(ed1));
      else System.out.println("WHAT work.get("+ed1.getId()+") = null");
    }
    work.clear();
    for (PetriNetEdge ed1: p1.getIncoming()) {
      for (PetriNetEdge ed2: p2.getIncoming()){
        if (!ed1.getId().equals(ed2.getId()) && ed1.getTo().equals(ed2.getTo())){
  //System.out.println(ed1.myString()+" -- "+ ed2.myString());
          union.addAll(ed1.getOwners());
          union.addAll(ed2.getOwners());
          work.put(ed1,union);
        }
      }
    }
    for(PetriNetEdge ed1: p1.getIncoming()){
      if (work.get(ed1) != null ) ed1.setOwners(work.get(ed1));
      else System.out.println("WHAT work.get("+ed1.getId()+") = null");
    }

  }
}
