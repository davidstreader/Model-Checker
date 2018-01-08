package mc.processmodels.petrinet.components;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.processmodels.ProcessModelObject;

@EqualsAndHashCode(callSuper = true)
@Data
public class PetriNetPlace extends ProcessModelObject {

  private String id;
  Set<PetriNetEdge> incoming = new HashSet<>();
  Set<PetriNetEdge> outgoing = new HashSet<>();
  String termination;

  public PetriNetPlace(String id) {
    super(id, "PetriNetPlace");
  }
}
