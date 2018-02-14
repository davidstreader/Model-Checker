package mc.processmodels.petrinet.operations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.NonNull;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

public class PetrinetSimp {
  /**
   * This reduces any superflouous nodes that are identical into one node.
   * <p>
   * This simplification operates on the assumption that every transition is required.
   *
   * TODO: THIS CURRENTLY DELETES ITEMS IMPROPERLY
   *
   * @param petri the petrinet to simplify
   * @return the simplified petrinet
   */
  public static Petrinet simplify(Petrinet petri) throws CompilationException {
    petri = petri.copy();

    Set<PetriNetTransition> transitions = new HashSet<>(petri.getTransitions().values());

    for (PetriNetTransition transition : transitions) {
      mergeIdenticalNodes(transition.pre(),petri);
      mergeIdenticalNodes(transition.post(),petri);

    }
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
