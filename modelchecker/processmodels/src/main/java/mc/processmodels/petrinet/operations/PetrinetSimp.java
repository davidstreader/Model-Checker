package mc.processmodels.petrinet.operations;

import java.util.*;

import lombok.Builder;
import lombok.NonNull;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.utils.PetriColouring;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import mc.processmodels.petrinet.utils.PetriColouring.PetriColourComponent;
public class PetrinetSimp {

  public static Petrinet colSimp(Petrinet p) throws CompilationException {
    Petrinet petri = p.copy();
    PetriColouring pc = new PetriColouring();
    Map<String, Integer> ic = pc.initColour(p);
    System.out.println("petri doSimp START "+petri.getId());
    pc.doColour(petri, ic);
    Multimap<Integer,PetriNetPlace> col2Place = ArrayListMultimap.create();
    //Set<PetriNetPlace> pls = new HashSet<>(petri.getPlaces().values());
    for (PetriNetPlace pl : petri.getPlaces().values()) {
      col2Place.put(pl.getColour(),pl);
    }
    //System.out.println(c2PtoString(col2Place));
    String base = null;
    Map<String,String> prodNames =  null;
    for(Integer c : col2Place.keySet()){
      Collection<PetriNetPlace> cPl = col2Place.get(c);
      if (cPl.size()<= 1) continue;
      //System.out.println("Col = "+ c);
      Iterator<PetriNetPlace> itColPl =  cPl.iterator();
      PetriNetPlace basePnP = itColPl.next();
      while(itColPl.hasNext()){
        PetriNetPlace  g = itColPl.next();
        //System.out.println("Gluing "+ basePnP.getId()+ " - "+g.getId());
        prodNames =
           petri.gluePlaces(Collections.singleton(g), Collections.singleton(basePnP));
        base = prodNames.values().iterator().next();
        basePnP = petri.getPlace(base);
      }

    }
    petri.setRootFromStart();
    //System.out.println("Colour Simp End "+petri.getId());
    petri = PetrinetReachability.removeUnreachableStates(petri);
    return petri;
  }

   public static String c2PtoString(Multimap<Integer,PetriNetPlace> c2p){
    StringBuilder sb = new StringBuilder();
    sb.append("c2P\n");
    for( Integer c: c2p.keySet()) {
      sb.append(c+" -> ");
      sb.append(c2p.get(c).stream().map(x->x.getId()).reduce("",(x,y)->x+y+" ")+"\n");
    }
    return sb.toString();
   }
  /**
   * This reduces any superflouous nodes that are identical into one node.
   * <p>
   * This simplification operates on the assumption that every transition is required.
   *
   * TODO: THIS CURRENTLY DELETES ITEMS IMPROPERLY
   *
   * @param p the petrinet to simplify
   * @return the simplified petrinet
   */
  public static Petrinet simplify(Petrinet p) throws CompilationException {
    Petrinet petri = p.copy();

    Set<PetriNetTransition> transitions = new HashSet<>(petri.getTransitions().values());
    for (PetriNetTransition transition : petri.getTransitions().values()) {
      mergeIdenticalNodes(transition.pre(),petri);
      mergeIdenticalNodes(transition.post(),petri);

    }
    //petri = PetrinetReachability.removeUnreachableStates(petri);
    return petri;
  }

  private static void mergeIdenticalNodes(Set<PetriNetPlace> placesToCompare, Petrinet petri)
      throws CompilationException {
    if (placesToCompare.size() == 1) {
      return;
    }

    Set<Tuple<PetriNetPlace>> pairs = getAllPairCombinations(placesToCompare);
    Set<PetriNetPlace> removed = new HashSet<>();

    for (Tuple<PetriNetPlace> pair : pairs) {
      if (removed.stream().anyMatch(pair::contains)) {
        continue;
      }

      if (pair.first.pre().equals(pair.second.pre())
          && pair.second.post().equals(pair.second.post())) {

        removed.add(pair.second);
        petri.removePlace(pair.second);
      }
    }
  }

  private static <E> Set<Tuple<E>> getAllPairCombinations(Set<E> toPair) {
    List<E> orderedToPair = new ArrayList<>(toPair);
    Set<Tuple<E>> pairs = new HashSet<>();

    for (int i = 0; i < orderedToPair.size(); i++) {
      for (int j = i + 1; j < orderedToPair.size(); j++) {
        if (i == j) {
          continue;
        }
        pairs.add(Tuple.<E>builder()
            .first(orderedToPair.get(i))
            .second(orderedToPair.get(j))
            .build());
      }
    }
    return pairs;
  }

  @Builder
  private static class Tuple<E> {
    @NonNull
    public final E first;
    @NonNull
    public final E second;

    public boolean contains(E item) {
      return (first != null && first.equals(item)) || (second != null && second.equals(item));
    }
  }
}
