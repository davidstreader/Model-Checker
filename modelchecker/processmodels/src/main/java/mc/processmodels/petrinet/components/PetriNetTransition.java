package mc.processmodels.petrinet.components;


import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.processmodels.ProcessModelObject;

@EqualsAndHashCode(callSuper = true, exclude = {"incoming", "outgoing"})
@Data
public class PetriNetTransition extends ProcessModelObject {

  String label;
  Set<PetriNetEdge> incoming = new HashSet<>();
  Set<PetriNetEdge> outgoing = new HashSet<>();

  public PetriNetTransition(String id, String label) {
    super(id, "node");
    this.label = label;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("transition{\n");
    builder.append("\tid:").append(getId());
    builder.append("\tlabel:").append(getLabel());
    builder.append("\n");
    builder.append("\tincoming:{");

    for (PetriNetEdge edge : getIncoming()) {
      builder.append(edge.getId()).append(",");
    }

    builder.append("}\n");

    builder.append("\toutgoing:{");
    for (PetriNetEdge edge : getOutgoing()) {
      builder.append(edge.getId()).append(",");
    }
    builder.append("}\n}");

    return builder.toString();
  }
}
