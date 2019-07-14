package mc.client.graph;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by bealjaco on 4/12/17.
 */
@AllArgsConstructor
@Data
public class DirectedEdge {
  public final String boolGuard;
  public final String label;
  public final String assignment;
  public final String uuid;
  public String getAll(){
    StringBuilder sb = new StringBuilder();
    if (boolGuard!=null && boolGuard.length()>0)
      sb.append("edge "+ boolGuard);
    sb.append(" "+label);
    if (assignment!=null && assignment.length()>0)
      sb.append(" "+assignment);

    //System.out.println("DirectedEdge "+out);
    return sb.toString();
  }
}
