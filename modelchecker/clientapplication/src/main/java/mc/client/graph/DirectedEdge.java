package mc.client.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.reflections.vfs.Vfs;

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
      sb.append("edge "+ boolGuard+" ");
    sb.append(label);
    if (assignment!=null && assignment.length()>0)
      sb.append(" "+assignment);

    //System.out.println("DirectedEdge "+out);
    return sb.toString();
  }
  private DirectedEdge() {
      boolGuard = "";
      label = "EdgeDummy";
      assignment = "";
      uuid = "EdgeDummy";
  }
  public static DirectedEdge dummy() {
      return new DirectedEdge();
  }
}
