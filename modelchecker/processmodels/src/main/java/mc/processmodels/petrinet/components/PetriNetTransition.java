package mc.processmodels.petrinet.components;


import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.processmodels.ProcessModelObject;

@EqualsAndHashCode(callSuper = true)
@Data
public class PetriNetTransition extends ProcessModelObject {

  String label;
  Set<PetriNetEdge> incoming = new HashSet<>();
  Set<PetriNetEdge> outgoing = new HashSet<>();

  public PetriNetTransition(String id, String label) {
    super(id, "node");
    this.label = label;
  }
}
