package mc.processmodels.petrinet.utils;

import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

/**
 * This is a small helper class to label automata using the
 * {@code label:PROCESS} style syntax.
 *
 * @author Jacob Beal
 * @see mc.processmodels.automata.operations.AutomataLabeller
 * @see Petrinet
 */
public final class PetrinetLabeller {

  /**
   * This labels the petrinet, relabelling events and ids.
   *
   * @param petri the petrinet to be labelled
   * @param label the prefix for the labelling
   * @return a labelled petrinet
   */
  @SneakyThrows(value = {CompilationException.class})
  public static Petrinet labelPetrinet(Petrinet petri, String label) {
    Petrinet labelled = new Petrinet(label + ":" + petri.getId(), false);

    petri.getPlaces().values().forEach(p -> {
      PetriNetPlace newPlace = labelled.addPlace(label + ":" + p.getId());
      newPlace.copyProperties(p);
      if (p.isStart()) {
        labelled.addRoot(newPlace);
      }
    });

    petri.getTransitions().values().forEach(t -> labelled.addTransition(label + ":" + t.getId(),
        label + ":" + t.getLabel()));


    for (PetriNetEdge edge : petri.getEdges().values()) {
      if (edge.getTo() instanceof PetriNetTransition) {
        PetriNetTransition to = labelled.getTransitions().get(label + ":" + edge.getTo().getId());
        PetriNetPlace from = labelled.getPlaces().get(label + ":" + edge.getFrom().getId());
        labelled.addEdge(to,from, edge.getOwners());
      } else {
        PetriNetPlace to = labelled.getPlaces().get(label + ":" + edge.getTo().getId());
        PetriNetTransition from = labelled.getTransitions().get(label + ":" + edge.getFrom().getId());
        labelled.addEdge(to,from, edge.getOwners());
      }
    }

    return labelled;
  }
}
