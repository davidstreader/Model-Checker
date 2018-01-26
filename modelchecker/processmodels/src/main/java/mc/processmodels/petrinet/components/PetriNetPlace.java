package mc.processmodels.petrinet.components;

import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.processmodels.ProcessModelObject;

@EqualsAndHashCode(callSuper = true, exclude = {"incoming", "outgoing"})
@Data
public class PetriNetPlace extends ProcessModelObject {

  private Set<PetriNetEdge> incoming = new HashSet<>();
  private Set<PetriNetEdge> outgoing = new HashSet<>();
  private boolean start;
  private String terminal;
  private Set<String> references;

  public PetriNetPlace(String id) {
    super(id, "PetriNetPlace");
  }

  public boolean isTerminal() {
    return terminal != null && terminal.length() > 0;
  }

  public void copyProperties(PetriNetPlace toCopy) {
    start = toCopy.start;
    terminal = toCopy.terminal;
  }

  public void intersectionOf(PetriNetPlace place1, PetriNetPlace place2) {
    if (place1.isStart() && place2.isStart()) {
      start = true;
    }
    if (place1.isTerminal() && place2.isTerminal()) {
      terminal = "STOP";
    }

    if ("ERROR".equalsIgnoreCase(place1.getTerminal()) || "ERROR".equalsIgnoreCase(place2.getTerminal())) {
      terminal = "ERROR";
    }
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
