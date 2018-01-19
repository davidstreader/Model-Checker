package mc.client.graph;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GraphNode {
  private final String automata;
  private final String nodeId;
  private final NodeStates nodeTermination;
  private final NodeType type;
  private final String label;
}
