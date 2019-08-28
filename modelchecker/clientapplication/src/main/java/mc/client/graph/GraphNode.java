package mc.client.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.processmodels.ProcessModelObject;

@AllArgsConstructor
@EqualsAndHashCode(exclude = {"nodeColor","oldColor"})
@Data
public class GraphNode {
  private final String processModelId; // The process that this graphnode came from. e.g A = t->STOP. the stop node would have have A as its processModelId
  private final String nodeId;  //
  private  NodeStates nodeColor; // The type of node is represented as a color. Blue for stop, etc
  private  NodeStates originalColor;
    private final NodeType type;
  private final String label;
  private final ProcessModelObject representedFeature; // The automaton node, or petrinet place that this represents.
  public String toString() {
      return type+" "+ processModelId+" "+ nodeId+" "+ label;
  }

  public GraphNode copy(){
    return new GraphNode(processModelId,nodeId, nodeColor, originalColor,type, label, representedFeature );
  }
  private GraphNode(String pid , String nid) {
      processModelId = pid;
      nodeId = nid;
      label = "dummy";
      type = null;
      representedFeature = null;
  }
  public static GraphNode dummy(){
      return new GraphNode("Dummy", "Dummy");
  }
}

