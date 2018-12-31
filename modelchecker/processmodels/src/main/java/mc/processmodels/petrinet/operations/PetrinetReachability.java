package mc.processmodels.petrinet.operations;

import java.util.*;
import java.util.stream.Collectors;

import mc.Constant;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.util.MyAssert;

public final class PetrinetReachability {

  public static Petrinet removeUnreachableStates(Petrinet pet) throws CompilationException {
    return removeUnreachableStates(pet,true);
  }
/*
  Needs to be updated for Broadcast optional edges!
  remove Unreachable and all END called from Parallel Comp with merge==false
 */
  public static Petrinet removeUnreachableStates(Petrinet pet, boolean merge) throws CompilationException {
   //System.out.println("\n UNREACH " + petri.myString());

    Petrinet petri = pet.copy();
    System.out.println("removeUnreach CHECK END " +petri.getEnds());
    Stack<Set<PetriNetPlace>> toDo = new Stack<>();

    for (Set<String> rt : petri.getRoots()) {
      Set<PetriNetPlace> rtP = new HashSet<>();
      for (String name : rt) {
       //System.out.println("Key Set "+petri.getPlaces().keySet());
       //System.out.println("key "+name);
        PetriNetPlace pl = petri.getPlaces().get(name);
        rtP.add(pl);
      }
//System.out.println("pushing root size " + rtP.size() +" "+
    //   rtP.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
      toDo.push(rtP);
    }


    //Stack<Set<PetriNetPlace>> toDo = new Stack<>();

    Set<Set<PetriNetPlace>> previouslyVisitedMarking = new HashSet<>();
    Set<PetriNetPlace> visitedPlaces = new HashSet<>();
    Set<PetriNetTransition> visitedTransitions = new HashSet<>();

    while (!toDo.isEmpty()) {
      Set<PetriNetPlace> currentMarking = toDo.pop();
     //System.out.println("Visited " + currentMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
      visitedPlaces.addAll(currentMarking);

      if (previouslyVisitedMarking.contains(currentMarking)) {
        continue;
      }
/*if (currentMarking==null) System.out.println("currentMarking == null");
      else
      System.out.println("MARKING: "+Petrinet.marking2String(currentMarking)); */
      //System.out.println("Post "+Petrinet.trans2String(post(currentMarking)));
      Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking); //**

      for (PetriNetTransition transition : satisfiedPostTransitions) {
        //System.out.println(transition.myString()+"  Marking "+Petrinet.marking2String(currentMarking));
        visitedTransitions.add(transition);
       /*System.out.println(Petrinet.marking2String(currentMarking)+" - " +
                           Petrinet.marking2String(transition.pre())+" + "+
                           Petrinet.marking2String(transition.post())); */
        // new =  current - pre + post
        if (transition.getLabel().equals(Constant.DEADLOCK)) continue;
        Set<PetriNetPlace> newMarking = new HashSet<>(currentMarking);
        newMarking.removeAll(transition.pre());
        newMarking.addAll(transition.post());
       //System.out.println("New Marking " + newMarking.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));
        if (!previouslyVisitedMarking.contains(newMarking)) {
          toDo.add(newMarking);
        }
      }
      previouslyVisitedMarking.add(currentMarking);
    }

    Set<PetriNetPlace> placesToRemove = new HashSet<>(petri.getPlaces().values());
    placesToRemove.removeAll(visitedPlaces);
    //System.out.println("All Vis "+Petrinet.marking2String(visitedPlaces));
    //System.out.println("All Rem "+Petrinet.marking2String(placesToRemove));
    Set<PetriNetTransition> transitionsToRemove = new HashSet<>(petri.getTransitions().values());
    transitionsToRemove.removeAll(visitedTransitions);
    System.out.println("removeAll CHECK END " +petri.getEnds());
    for (PetriNetPlace p : placesToRemove) {
      System.out.println("removeing "+p.myString());
      petri.removePlace(p, merge);  // parallel comp will remove all End
    }

    //System.out.println("Trans to Go "+ transitionsToRemove.stream().map(x->x.getId()) .collect(Collectors.toSet()));
    for (PetriNetTransition t : transitionsToRemove) {
      petri.removeTransition(t);
    }
    System.out.println("removeUnreach CHECK END " +petri.getEnds());
    // PROBLEM with optional edges
    if (merge) mergePlaces(petri);  //paralle comp will not merge Places
    System.out.println("removeUnreach CHECK END " +petri.getEnds());
    MyAssert.myAssert(petri.validatePNet("PetriNet reachability "+petri.getId()+ " valid ="), "Net reachability Failure");

    return petri;
  }

  private static Petrinet mergePlaces(Petrinet petri) throws CompilationException {
    /*
  If two places have  incoming and outgoing edges to the same Transitions
     and no edge is optional!
  then they can be merged
  This means that the owners must be merged.
 */
    Set<PetriNetPlace> togo = new HashSet<>();
    for (PetriNetPlace pl : petri.getPlaces().values()) {
      for (PetriNetPlace pl1 : peerNonBlocking(pl)) {   //Do NOT merge optional places

        if (!pl.getId().equals(pl1.getId()) &&
          pl.post().equals(pl1.post()) && pl.pre().equals(pl1.pre())) {
          //System.out.println("Merge "+pl.getId()+" with "+pl1.getId());
          if (!togo.contains(pl) && !togo.contains(pl1)) {
            togo.add(pl1);
            pl.getOwners().addAll(pl1.getOwners());
          }

        }
      }
    }
    for (PetriNetPlace p : togo) {
      //System.out.println("togo "+p.getId());
      petri.removePlace(p,false);
    }

   /*
  If two transitions have  incoming and outgoing edges to the same Places then they can be merged

 */
    Set<PetriNetTransition> togoT = new HashSet<>();
    for (PetriNetTransition pl : petri.getTransitions().values()) {
      for (PetriNetTransition pl1 : peerT(pl)) {

        if (!pl.getId().equals(pl1.getId()) &&
          pl.post().equals(pl1.post()) &&
          pl.pre().equals(pl1.pre()) &&
          pl.getOwners().equals(pl1.getOwners()) &&
          pl.getLabel().equals(pl1.getLabel())) {
          //System.out.println("Merge "+pl.getId()+" with "+pl1.getId());
          if (!togoT.contains(pl) && !togoT.contains(pl1)) {
            //System.out.println("togoT "+pl1.getId()+" "+pl1.getLabel());
            togoT.add(pl1);
          }

        }
      }
    }
    for (PetriNetTransition p : togoT) {
      petri.removeTransition(p);
    }
  return petri;
  }
  private static Set<PetriNetTransition> satisfiedTransitions(Set<PetriNetPlace> currentMarking) {
    Set<PetriNetTransition> out = new HashSet<>();
    for(PetriNetTransition tr: post(currentMarking)){
      if (currentMarking.containsAll(tr.pre())) out.add(tr);
    }
    return out;
 /*   return post(currentMarking).stream() //**
      .filter(transition -> currentMarking.containsAll(transition.pre()))
      .distinct()
      .collect(Collectors.toSet());*/
  }


  private static Set<PetriNetTransition> post(Set<PetriNetPlace> currentMarking) {
    return currentMarking.stream()
      .map(PetriNetPlace::post)
      .flatMap(Set::stream)
      .distinct()
      .collect(Collectors.toSet()); //**
  }

  private static Set<PetriNetTransition> pre(Set<PetriNetPlace> currentMarking) {
    return currentMarking.stream()
      .map(PetriNetPlace::pre)
      .flatMap(Set::stream)
      .distinct()
      .collect(Collectors.toSet());
  }
/*
  Used in the check for place merging
 */
  private static Set<PetriNetPlace> peerNonBlocking(PetriNetPlace current) {

    Set<PetriNetPlace> union = new HashSet<>(current.postNotOpt().stream()
      .map(PetriNetTransition::preNonBlocking).flatMap(Set::stream)
      .distinct().collect(Collectors.toSet()));
    //System.out.println("peer "+Petrinet.marking2String(union));
    union.addAll(current.pre().stream()
      .map(PetriNetTransition::postNotOptional).flatMap(Set::stream)
      .distinct().collect(Collectors.toSet()));
    //System.out.println("peer "+Petrinet.marking2String(union));
    return union;
  }

  private static Set<PetriNetTransition> peerT(PetriNetTransition current) {
    return current.post().stream()
      .map(PetriNetPlace::pre).flatMap(Set::stream)
      .distinct().collect(Collectors.toSet());
  }




}
