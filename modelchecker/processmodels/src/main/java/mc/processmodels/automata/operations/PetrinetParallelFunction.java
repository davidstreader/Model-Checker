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

  private static Set<String> unsynchedActions;
  private static Set<String> synchronisedActions;
  private static Map<Petrinet, Map<PetriNetPlace, PetriNetPlace>> petriPlaceMap;
  private static Map<PetriNetTransition, PetriNetTransition> petriTransMap;
  private static final String tag1 = "*P1";
  private static final String tag2 = "*P2";

  public static Petrinet compose(Petrinet p1, Petrinet p2) throws CompilationException {
    clear();
    p1.rebuildAlphabet(); p2.rebuildAlphabet();
   //System.out.println("\nPETRINETPARALLELFUNCTION");
   //System.out.println("\np1 "+p1.myString());
   //System.out.println("\np2 "+p2.myString());






    setupActions(p1, p2);

    Petrinet composition = new Petrinet(p1.getId() + "||" + p2.getId(), false);
    composition.getOwners().clear();
    composition.getOwners().addAll(p1.getOwners());
    composition.getOwners().addAll(p2.getOwners());

    List<Set<String>> roots = buildRoots(p1,p2);

    petriTransMap.putAll(composition.addPetrinetNoOwner(p1,tag1));
    petriTransMap.putAll(composition.addPetrinetNoOwner(p2,tag2));
    composition.setRoots(roots);
    composition.setStartFromRoot();

    setupSynchronisedActions(p1, p2, composition);

    composition = PetrinetReachability.removeUnreachableStates(composition);

   //System.out.println("\n   PAR end "+composition.myString());
    return composition;
  }

  private static void setupActions(Petrinet p1, Petrinet p2) {
    Set<String> actions1 = p1.getAlphabet().keySet();
    Set<String> actions2 = p2.getAlphabet().keySet();
    //System.out.println("actions1 "+actions1);
    //System.out.println("actions2 "+actions2);
    actions1.forEach(a -> setupAction(a, actions2));
    actions2.forEach(a -> setupAction(a, actions1));
  }

  private static void setupAction(String action, Set<String> otherPetrinetActions) {
    if (action.equals(Constant.HIDDEN) || action.equals(Constant.DEADLOCK)) {
      unsynchedActions.add(action);

      // broadcasting actions are always unsynched
    } else if (action.endsWith("!")) {
      if (containsReceiverOf(action, otherPetrinetActions)) {
        synchronisedActions.add(action);
      }
      if (containsBroadcasterOf(action, otherPetrinetActions)) {
        synchronisedActions.add(action);
      } else {
        unsynchedActions.add(action);
      }
    } else if (action.endsWith("?")) {
      if (!containsBroadcasterOf(action, otherPetrinetActions)) {
        if (containsReceiverOf(action, otherPetrinetActions)) {
          synchronisedActions.add(action);
        }

        unsynchedActions.add(action);
      }
    } else if (otherPetrinetActions.contains(action)) {
      synchronisedActions.add(action);
   //System.out.println("Sync "+ action);
    } else {
      //System.out.println("Unsync "+ action);
      unsynchedActions.add(action);
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

      for (PetriNetTransition t1 : p1Pair) {
        for (PetriNetTransition t2 : p2Pair) {
          if (t1==null) {System.out.println("t1==null");continue;}
          if (t2==null) {System.out.println("t2==null");continue;}
  //System.out.println("t1 "+ t1.myString()+ " , t2 "+t2.myString());
          Set<PetriNetEdge> outgoingEdges = new LinkedHashSet<>();
          outgoingEdges.addAll(t1.getOutgoing());
          outgoingEdges.addAll(t2.getOutgoing());

          Set<PetriNetEdge> incomingEdges = new LinkedHashSet<>();
          incomingEdges.addAll(t1.getIncoming());
          incomingEdges.addAll(t2.getIncoming());

          PetriNetTransition newTrans = comp.addTransition(action);
          for(PetriNetEdge outE : outgoingEdges) {
            comp.addEdge( (PetriNetPlace) outE.getTo(), newTrans);
          }

          for(PetriNetEdge inE : incomingEdges) {
            comp.addEdge(newTrans, (PetriNetPlace) inE.getFrom());
          }
        }
      }

      for (PetriNetTransition oldTrans : Iterables.concat(p1Pair, p2Pair)) {
        if (comp.getTransitions().containsValue(oldTrans))  comp.removeTransition(oldTrans);
      }

    }
  }

  private static boolean containsReceiverOf(String broadcaster, Collection<String> otherPetrinet) {
    for (String reciever : otherPetrinet) {
      if (reciever.endsWith("?")) {
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
      if (receiver.endsWith("!")) {
        String action = receiver.substring(0, receiver.length() - 1);
        if (action.equals(broadcastAction)) {
          return true;
        }
      }
    }

    return false;
  }

  private static void clear() {
    unsynchedActions = new HashSet<>();
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
        addTo.addEdge( transitionMap.get(edge.getTo()), placeMap.get(edge.getFrom()));
      } else {
        //System.out.println("place "+placeMap.get(edge.getTo()).myString());
        addTo.addEdge( placeMap.get(edge.getTo()), transitionMap.get(edge.getFrom()));
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
    out.addAll(m1.stream().map(x->x+tag1).collect(Collectors.toSet()));
    out.addAll(m2.stream().map(x->x+tag2).collect(Collectors.toSet()));
   //System.out.println("Next root "+out);
    return out;
  }
}

