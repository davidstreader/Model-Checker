package mc.processmodels.petrinet.components;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.processmodels.ProcessModelObject;

@EqualsAndHashCode(callSuper = true, exclude = {"incoming", "outgoing"})
@Data
public class PetriNetPlace extends ProcessModelObject {

  Set<PetriNetEdge> incoming = new HashSet<>();
  Set<PetriNetEdge> outgoing = new HashSet<>();
  boolean start;
  String terminal;

  public PetriNetPlace(String id) {
    super(id, "PetriNetPlace");
  }

  public boolean isTerminal() {
    return terminal != null && terminal.length() > 0;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append("place{\n");
    if (isStart()) {
      builder.append("\tStarting Place");
    }
    if (isTerminal()) {
      builder.append("\tTermination: ").append(getTerminal());
    }
    builder.append("\tid:").append(getId());
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
