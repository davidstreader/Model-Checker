package mc.client.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode(exclude = {"nodeTermination"})
@Data
public class GraphNode {
  private final String automata;
  private final String nodeId;
  private  NodeStates nodeTermination;
  private final NodeType type;
  private final String label;
}
