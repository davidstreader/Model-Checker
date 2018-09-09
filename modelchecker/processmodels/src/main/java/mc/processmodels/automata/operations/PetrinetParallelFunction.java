package mc.processmodels.automata.operations;

import com.google.common.collect.Iterables;

import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.operations.PetrinetReachability;

public class PetrinetParallelFunction  {

  //private static Set<String> unsynchedActions;
  private static Set<String> synchronisedActions;
  private static Map<Petrinet, Map<PetriNetPlace, PetriNetPlace>> petriPlaceMap;
  private static Map<PetriNetTransition, PetriNetTransition> petriTransMap;
  //private static final String tag1 = ""; //"*P1";
  //private static final String tag2 = ""; //"*P2";

  public static Petrinet compose(Petrinet pi1, Petrinet pi2) throws CompilationException {
    clear();
    Petrinet p1 = pi1.reId("1");
    Petrinet p2 = pi2.reId("2");
    p1.rebuildAlphabet(); p2.rebuildAlphabet();

   //System.out.println("     PETRINET PARALLELFUNCTION"+"  "+p1.getId()+" || "+p2.getId());

   //builds synchronisedActions set
    setupActions(p1, p2);
    //System.out.println("  synchronisedActions "+synchronisedActions);
    Petrinet composition = new Petrinet(p1.getId()  + p2.getId(), false);
    composition.getOwners().clear();
    composition.getOwners().addAll(p1.getOwners());
    composition.getOwners().addAll(p2.getOwners());

    List<Set<String>> roots = buildRoots(p1,p2);

    petriTransMap.putAll(composition.addPetrinetNoOwner(p1,""));  //Tag not needed as reId dose this
    //System.out.println("par "+ composition.myString());
    petriTransMap.putAll(composition.addPetrinetNoOwner(p2,"")); //adds unsynchronised transitions
    composition.setRoots(roots);
    composition.setEnds(buildEnds(p1.getEnds(),p2.getEnds()));
    //System.out.println("half "+composition.myString()+"\nhalf END");
    composition.setStartFromRoot();
    composition.setEndFromNet();
    //System.out.println("half "+composition.myString()+"\nhalf END");
    //System.out.println("  SoFar unsynced \n"+ composition.myString());
    //System.out.println("  synchronisedActions "+synchronisedActions);
    setupSynchronisedActions(p1, p2, composition);
    //System.out.println("  synced  \n "+ composition.myString());
    //composition = PetrinetReachability.removeUnreachableStates(composition);
     composition.reId("");
     assert composition.validatePNet():"parallel comp post condition ";
   //System.out.println("\n   PAR end "+composition.myString());
    return composition;
  }
  private static List<Set<String>> buildEnds(List<Set<String>> p1, List<Set<String>> p2){
    List<Set<String>> out = new ArrayList<>();
    for(Set<String> e1: p1){
      for(Set<String> e2: p2){
        Set<String> o = new HashSet<>() ;
        o.addAll(e1);
        o.addAll(e2);
        o = o.stream().sorted().collect(Collectors.toSet());
        out.add( o );
      }
    }
    return out;
  }

  /*
  Adds actions to synchronisedActions.
   */
  private static void setupActions(Petrinet p1, Petrinet p2) {
    Set<String> actions1 = p1.getAlphabet().keySet();
    Set<String> actions2 = p2.getAlphabet().keySet();
    //System.out.println("actions1 "+actions1);
    //System.out.println("actions2 "+actions2);
    actions1.forEach(a -> setupAction(a, actions2));
    actions2.forEach(a -> setupAction(a, actions1));
  }

  private static void setupAction(String action, Set<String> otherPetrinetActions) {
     if (action.endsWith(Constant.BROADCASTSoutput)) {
      if (containsReceiverOf(action, otherPetrinetActions)) {
        synchronisedActions.add(action);
      } else if (containsBroadcasterOf(action, otherPetrinetActions)) {
        synchronisedActions.add(action);
      }
    } else if (action.endsWith(Constant.BROADCASTSinput)) {
      if (containsReceiverOf(action, otherPetrinetActions)) {
        synchronisedActions.add(action);
      } else if (containsBroadcasterOf(action, otherPetrinetActions)) {
        synchronisedActions.add(action);
      }
    } else if (action.endsWith(Constant.ACTIVE)) {
       String passiveAction = action.substring(0, action.length() - 1);

       if (otherPetrinetActions.contains(passiveAction)) {
         synchronisedActions.add(action);
         //synchronisedActions.add(passiveAction);
       }
     }else if (otherPetrinetActions.contains(action)) {
      synchronisedActions.add(action);
   //System.out.println("Sync "+ action);
    }
  }

  @SneakyThrows(value = {CompilationException.class})
  private static void setupSynchronisedActions(Petrinet p1, Petrinet p2, Petrinet comp) {

    for (String action : synchronisedActions) {
      //System.out.println("Sync action "+action);
      Set<PetriNetTransition> p1Pair = p1.getAlphabet().get(action).stream()
              .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
      Set<PetriNetTransition> p2Pair = p2.getAlphabet().get(action).stream()
              .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());

      replaceActions(p1Pair, p2Pair, comp, action, false); //handshake on label equality

      if (action.endsWith(Constant.BROADCASTSoutput)) {
        String sync = action.substring(0, action.length() - 1)+Constant.BROADCASTSinput;

        Set<PetriNetTransition> p1P = p1.getAlphabet().get(action).stream()
                .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
        Set<PetriNetTransition> p2P = p2.getAlphabet().get(sync).stream()
                .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
        replaceActions(p1P, p2P, comp, action, true);
         p1P = p1.getAlphabet().get(sync).stream()
                .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
         p2P = p2.getAlphabet().get(action).stream()
                .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
        replaceActions(p1P, p2P, comp, action, true);
      }
      else if (action.endsWith(Constant.ACTIVE)) {
        String sync = action.substring(0, action.length() - 1);

        Set<PetriNetTransition> p1P = p1.getAlphabet().get(action).stream()
          .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
        Set<PetriNetTransition> p2P = p2.getAlphabet().get(sync).stream()
          .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
        replaceActions(p1P, p2P, comp, action, true);
        p1P = p1.getAlphabet().get(sync).stream()
          .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
        p2P = p2.getAlphabet().get(action).stream()
          .map(t -> petriTransMap.get(t)).collect(Collectors.toSet());
        replaceActions(p1P, p2P, comp, action, true);
      }
      //System.out.println("Sync END "+comp.myString());
    }
  }

/*
   Replace each pair of synchronising transitions with their combined transition
 */
  private static void replaceActions(Set<PetriNetTransition> p1_ , Set<PetriNetTransition> p2_ ,
                                     Petrinet comp, String action,boolean optional)
  throws  CompilationException {
    //optional is only true if one tran is b! and the other b?
    //only synced transitions passed into the method
    if (p1_.size()==0 || p2_.size() ==0) return; // Must not contiue as deleat transitions at end
    //System.out.println("Replace actions "+ p1_.size()+" "+p2_.size());
    //System.out.println("p1 "+p1_.stream().map(x->x.myString()).reduce("",(x,y)->x+y+" "));
    //System.out.println("p2 "+p2_.stream().map(x->x.myString()).reduce("",(x,y)->x+y+" "));
    for (PetriNetTransition t1 : p1_) {
        for (PetriNetTransition t2 : p2_) {
          if (t1==null) {System.out.println("t1==null");continue;}
          if (t2==null) {System.out.println("t2==null");continue;}
   //System.out.println("  t1 "+ t1.myString()+ " , t2 "+t2.myString());
          Set<PetriNetEdge> outgoingEdges = new LinkedHashSet<>();
          outgoingEdges.addAll(t1.getOutgoing());
          outgoingEdges.addAll(t2.getOutgoing());

          Set<PetriNetEdge> incomingEdges = new LinkedHashSet<>();
          incomingEdges.addAll(t1.getIncoming());
          incomingEdges.addAll(t2.getIncoming());

          PetriNetTransition newTrans = comp.addTransition(action);
          newTrans.clearOwners();
          newTrans.addOwners(t1.getOwners());
          newTrans.addOwners(t2.getOwners());
   //System.out.println("size "+incomingEdges.size()+" "+incomingEdges.size());
          //Set broadcast listening b? edges to optional
          for(PetriNetEdge outE : outgoingEdges) { // outgoing from transition
            //System.out.println("out "+outE.myString());
            PetriNetEdge ed =  comp.addEdge( (PetriNetPlace) outE.getTo(), newTrans,outE.getOptional());
           if (((PetriNetTransition) outE.getFrom()).getLabel().endsWith(Constant.BROADCASTSinput) && optional) {
              ed.setOptional(true);
            }
            //System.out.println("    adding "+ed.myString());
          }
          //System.out.println("  newTran "+newTrans.myString());
          for(PetriNetEdge inE : incomingEdges) { // incoming to transition
            //System.out.println("in  "+inE.myString());
            PetriNetEdge ed = comp.addEdge(newTrans, (PetriNetPlace) inE.getFrom(),inE.getOptional());
            if (((PetriNetTransition) inE.getTo()).getLabel().endsWith(Constant.BROADCASTSinput)&& optional) {
              ed.setOptional(true);
            }
            //System.out.println("    adding "+ed.myString());
          }
          //System.out.println("  newTrans "+newTrans.myString());

        }
      }
    for (PetriNetTransition oldTrans : Iterables.concat(p1_, p2_)) {
      if (comp.getTransitions().containsValue(oldTrans))  {
        //System.out.println("removing "+oldTrans.myString());
        comp.removeTransition(oldTrans);}
    }


  }

  private static boolean containsReceiverOf(String broadcaster, Collection<String> otherPetrinet) {
    for (String reciever : otherPetrinet) {
      if (reciever.endsWith(Constant.BROADCASTSinput)) {
        if (reciever.substring(0, reciever.length() - 1).equals(broadcaster.substring(0, broadcaster.length() - 1))) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean containsBroadcasterOf(String broadcaster, Set<String> otherPetrinet) {
    String broadcastAction = broadcaster.substring(0, broadcaster.length() - 1);
    for (String receiver : otherPetrinet) {
      if (receiver.endsWith(Constant.BROADCASTSoutput)) {
        String action = receiver.substring(0, receiver.length() - 1);
        if (action.equals(broadcastAction)) {
          return true;
        }
      }
    }

    return false;
  }

  private static void clear() {
    //unsynchedActions = new HashSet<>();
    synchronisedActions = new HashSet<>();
    petriPlaceMap = new HashMap<>();
    petriTransMap = new HashMap<>();
  }

  @SneakyThrows(value = {CompilationException.class})
  public static List<Set<String>> addPetrinet(Petrinet addTo, Petrinet petriToAdd) {
    //addTo.validatePNet();
    //petriToAdd.validatePNet();
   //System.out.println("IN AddTo "+addTo.myString());
   //System.out.println("IN ToAdd "+petriToAdd.myString());
    List<Set<String>> roots = addTo.getRoots();
   //System.out.println("roots "+roots);
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();

    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {
      PetriNetPlace newPlace = addTo.addPlace();
      newPlace.copyProperties(place);

      if (place.isStart()) {
        newPlace.setStart(true);
      }

      placeMap.put(place, newPlace);
    }
    for (PetriNetTransition transition : petriToAdd.getTransitions().values()) {
      PetriNetTransition newTransition = addTo.addTransition(transition.getLabel());
      transitionMap.put(transition, newTransition);
    }

    for (PetriNetEdge edge : petriToAdd.getEdges().values()) {
      //System.out.println(edge.myString());
      if (edge.getFrom() instanceof PetriNetPlace) {
        //System.out.println("tran "+transitionMap.get(edge.getTo()).myString());
        PetriNetEdge e = addTo.addEdge( transitionMap.get(edge.getTo()), placeMap.get(edge.getFrom()),edge.getOptional());
        e.setOptional(edge.getOptional());

      } else {
        //System.out.println("place "+placeMap.get(edge.getTo()).myString());
        PetriNetEdge e = addTo.addEdge( placeMap.get(edge.getTo()), transitionMap.get(edge.getFrom()),edge.getOptional());
        e.setOptional(edge.getOptional());
      }
    }
   //System.out.println("toAdd roots"+petriToAdd.getRoots());
    roots.addAll(petriToAdd.getRoots());
   //System.out.println("roots"+petriToAdd.getRoots());
    addTo.setRoots(roots);
    petriTransMap.putAll(transitionMap);
    petriPlaceMap.put(petriToAdd, placeMap);

    //addTo.validatePNet();
  //System.out.println("OUT AddedTo "+addTo.myString());
    return roots;
  }

  /**
   *
   * @param net1
   * @param net2
   * @return the multiRoot for parallel composition of the nets
   */
  private static List<Set<String>> buildRoots(Petrinet net1,Petrinet net2) {
   //System.out.println("Building Roots");
    List<Set<String>> out = new ArrayList<>();
    for(Set<String> m1: net1.getRoots()) {
      for(Set<String> m2: net2.getRoots()) {
        out.add(buildMark(m1,m2));
      }
    }
  //System.out.println("New buildRoots "+out);
    return out;
  }

  private static Set<String> buildMark(Set<String> m1, Set<String> m2){
    Set<String> out = new HashSet<>();
    out.addAll(m1);
    out.addAll(m2);
    out = out.stream().sorted().collect(Collectors.toSet());
   //System.out.println("Next root "+out);
    return out;
  }
}

