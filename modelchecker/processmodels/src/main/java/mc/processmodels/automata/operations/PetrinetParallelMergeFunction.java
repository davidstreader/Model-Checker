package mc.processmodels.automata.operations;

import com.google.common.collect.Iterables;
import lombok.SneakyThrows;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.operations.PetrinetReachability;

import java.util.*;
import java.util.stream.Collectors;

public class PetrinetParallelMergeFunction {

  private static Set<String> unsynchedActions;
  private static Set<String> synchronisedActions;
  private static Map<Petrinet, Map<PetriNetPlace, PetriNetPlace>> petriPlaceMap;
  private static Map<Petrinet, Map<PetriNetTransition, PetriNetTransition>> petriTransMap;
  private static final String tag1 = "*P1";
  private static final String tag2 = "*P2";

  public static Petrinet compose(Petrinet p1, Petrinet p2)
    throws CompilationException {
    clear();
    System.out.println("\nPETRINETPARALLEL MERGE");
    System.out.println("|| NET1 "+p1.myString()+ "\n");
    System.out.println("|| NET2 "+p2.myString()+ "\n");
    for(String eId : p1.getEdges().keySet()) {
      Set<String> owners = p1.getEdges().get(eId).getOwners();
      if(owners.contains(Petrinet.DEFAULT_OWNER)) {
        owners = Collections.singleton(p1.getId());
      }
      p1.getEdges().get(eId).setOwners(owners);
      System.out.println(p1.getEdges().get(eId).myString());
    }
    //below should not be needed as this should not occur!
    if (p1.getOwners().contains(Petrinet.DEFAULT_OWNER))
      p1.setOwners(p1.getEdges().values().stream().
        flatMap(x->x.getOwners().stream()).collect(Collectors.toSet()));

    System.out.println("p1.getOwners()"+ p1.getOwners());
    for(String eId : p2.getEdges().keySet()) {
      Set<String> owners = p2.getEdges().get(eId).getOwners();
      if(owners.contains(Petrinet.DEFAULT_OWNER)) {
        owners= Collections.singleton(p2.getId());;
      }
      p2.getEdges().get(eId).setOwners(owners);
      System.out.println(p2.getEdges().get(eId).myString());
    }
    Set<String> out2 = new HashSet<>();
    if (p2.getOwners().contains(Petrinet.DEFAULT_OWNER)) {
      for (PetriNetEdge ed : p2.getEdges().values()) {
        out2.addAll(ed.getOwners());
      }
    }
    p2.setOwners(out2.stream().distinct().collect(Collectors.toSet()));

    System.out.println("p2.getOwners()"+ p2.getOwners());

    setupActions(p1, p2);

    Petrinet composition = new Petrinet(p1.getId() + "||" + p2.getId(), false);
    composition.getOwners().clear();
    composition.getOwners().addAll(p1.getOwners());
    composition.getOwners().addAll(p2.getOwners());

    List<Set<String>> roots = buildRoots(p1,p2);
    composition.addPetrinetNoOwner(p1,tag1);
    composition.addPetrinetNoOwner(p2,tag2);
    composition.setRoots(roots);
    composition.setStartFromRoot();



    composition = PetrinetReachability.removeUnreachableStates(composition);
    System.out.println("END of PAR "+composition.myString()+ "\n");
    return composition;
  }

  /**
   *
   * @param net1
   * @param net2
   * @return the multiRoot for parallel composition of the nets
   */
  private static List<Set<String>> buildRoots(Petrinet net1,Petrinet net2) {
    System.out.println("Building Roots");
    List<Set<String>> out = new ArrayList<>();
    for(Set<String> m1: net1.getRoots()) {
      for(Set<String> m2: net2.getRoots()) {
        out.add(buildMark(m1,m2));
      }
    }
    System.out.println("New Roots "+out);
    return out;
  }

  private static Set<String> buildMark(Set<String> m1, Set<String> m2){
    Set<String> out = new HashSet<>();
    out.addAll(m1.stream().map(x->x+tag1).collect(Collectors.toSet()));
    out.addAll(m2.stream().map(x->x+tag2).collect(Collectors.toSet()));
    System.out.println("Next root "+out);
    return out;
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
    }
  }





  private static void clear() {
    unsynchedActions = new HashSet<>();
    synchronisedActions = new HashSet<>();
    petriPlaceMap = new HashMap<>();
    petriTransMap = new HashMap<>();
  }


}

