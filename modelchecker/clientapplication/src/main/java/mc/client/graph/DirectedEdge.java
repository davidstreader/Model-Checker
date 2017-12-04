package mc.client.graph;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by bealjaco on 4/12/17.
 */
@AllArgsConstructor
@Data
public class DirectedEdge {
    public final String label;
    public final String uuid;
}
