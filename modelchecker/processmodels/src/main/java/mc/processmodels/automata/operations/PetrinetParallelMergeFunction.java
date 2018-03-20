package mc.processmodels.automata.operations;

import com.google.common.collect.Iterables;
import lombok.SneakyThrows;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.util.*;
import java.util.stream.Collectors;

public class PetrinetParallelMergeFunction {

  private static Set<String> unsynchedActions;
  private static Set<String> synchronisedActions;
  private static Map<Petrinet, Map<PetriNetPlace, PetriNetPlace>> petriPlaceMap;
  private static Map<Petrinet, Map<PetriNetTransition, PetriNetTransition>> petriTransMap;

  public static Petrinet compose(Petrinet p1, Petrinet p2) {
    clear();

   // System.out.println("p1 "+p1.myString());
   // System.out.println("p2 "+p2.myString());
    for(String eId : p1.getEdges().keySet()) {
      Set<String> owners = p1.getEdges().get(eId).getOwners();
      if(owners.contains(Petrinet.DEFAULT_OWNER)) {
        owners = Collections.singleton(p1.getId());
      }
    //  System.out.println("eId "+eId);
    //  System.out.println(p1.getOwners());
      //p1.getEdges().get(eId).getOwners().add(p1.getId());
      p1.getEdges().get(eId).setOwners(owners);
    }
    for(String eId : p2.getEdges().keySet()) {
      Set<String> owners = p2.getEdges().get(eId).getOwners();
      if(owners.contains(Petrinet.DEFAULT_OWNER)) {
        owners= Collections.singleton(p2.getId());;
      }
      p2.getEdges().get(eId).setOwners(owners);
    }


    setupActions(p1, p2);

    Petrinet composition = new Petrinet(p1.getId() + "||" + p2.getId(), false);
    composition.getOwners().clear();
    composition.getOwners().addAll(p1.getOwners());
    composition.getOwners().addAll(p2.getOwners());

    addPetrinet(composition,p1).forEach(composition::addRoot);
    addPetrinet(composition,p2).forEach(composition::addRoot);

    //setupSynchronisedActions(p1, p2, composition);


    System.out.println("MERGE "+ composition.myString());

    return composition;
  }

  private static void setupActions(Petrinet p1, Petrinet p2) {
    Set<String> actions1 = p1.getAlphabet().keySet();
    Set<String> actions2 = p2.getAlphabet().keySet();
    actions1.forEach(a -> setupAction(a, actions2));
    actions2.forEach(a -> setupAction(a, actions1));
  }

  private static void setupAction(String action, Set<String> otherPetrinetActions) {
    unsynchedActions.add(action);
  }







  private static void clear() {
    unsynchedActions = new HashSet<>();
    synchronisedActions = new HashSet<>();
    petriPlaceMap = new HashMap<>();
    petriTransMap = new HashMap<>();
  }

  @SneakyThrows(value = {CompilationException.class})
  public static Set<PetriNetPlace> addPetrinet(Petrinet addTo, Petrinet petriToAdd) {
    addTo.validatePNet();
    petriToAdd.validatePNet();
   // System.out.println("IN AddTo "+addTo.myString());
   // System.out.println("IN ToAdd "+petriToAdd.myString());
    Set<PetriNetPlace> roots = addTo.getRoots();
    Map<PetriNetPlace, PetriNetPlace> placeMap = new HashMap<>();
    Map<PetriNetTransition, PetriNetTransition> transitionMap = new HashMap<>();

    for (PetriNetPlace place : petriToAdd.getPlaces().values()) {
      PetriNetPlace newPlace = addTo.addPlace();
      newPlace.copyProperties(place);

      if (place.isStart()) {
        newPlace.setStart(true);
        roots.add(newPlace);
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
        addTo.addEdge( transitionMap.get(edge.getTo()), placeMap.get(edge.getFrom()), edge.getOwners());
      } else {
        //System.out.println("place "+placeMap.get(edge.getTo()).myString());
        addTo.addEdge( placeMap.get(edge.getTo()), transitionMap.get(edge.getFrom()), edge.getOwners());
      }
    }
    //System.out.println("one2");
     addTo.setRoots(roots);
    petriTransMap.put(petriToAdd, transitionMap);
    petriPlaceMap.put(petriToAdd, placeMap);

    addTo.validatePNet();
  //  System.out.println("OUT AddedTo "+addTo.myString());
    return roots;
  }
}

