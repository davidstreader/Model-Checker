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
    String out;
    if (boolGuard!=null && boolGuard.length()>0)
      out = boolGuard+" "+label;
    else if (assignment!=null && assignment.length()>0)
      out = assignment+" "+label;
    else
      out = label;
    //System.out.println("DirectedEdge "+out);
    return out;
  }
}
