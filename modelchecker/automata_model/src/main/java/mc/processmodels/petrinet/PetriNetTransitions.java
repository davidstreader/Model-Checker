package mc.processmodels.petrinet;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import mc.processmodels.ProcessModelObject;

public class PetriNetTransitions extends ProcessModelObject {

  private Map<String, PetriNetEdge> incomingEdges;
  private Map<String, PetriNetEdge> outgoingEdges;

  @Getter
  @Setter
  private String label;

  @Getter
  @Setter
  private boolean startNode;

  @Getter
  @Setter
  private String terminal;

  @Getter
  @Setter
  private int colour;

  @Getter
  @Setter
  private Set<String> references;

  @Getter
  @Setter
  private int labelNumber;

  public PetriNetTransitions(String id) {
    super(id, "node");
    this.label = null;
    incomingEdges = new HashMap<>();
    outgoingEdges = new HashMap<>();
  }
}
