package mc.client.graph;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by bealjaco on 4/12/17.
 */
@AllArgsConstructor
@Data
public class GraphNode {
    private final String automata;
    private final String nodeId;
    private final String nodeTermination;
}
