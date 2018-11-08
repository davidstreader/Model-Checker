package mc.compiler.ast;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * This contains a list of processes with different variables.
 * These are permutations of the same process, and each is not necessarily unique.
 * This is used specifically within indexed processes and {@code forall} statements.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @author Jordan Smith
 * @see RangeNode
 * @see SetNode
 * @see ASTNode
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RangesNode extends ASTNode {

  /**
   * These are the several states within the range.
   */
  private List<IndexExpNode> ranges;

  /**
   * Instantiate a new RangesNode.
   *
   * @param ranges the distinct states within the range. {@link #ranges}
   * @param location the location of the RangesNode within users code {@link ASTNode#location}
   */
  public RangesNode(List<IndexExpNode> ranges, Location location) {
    super(location,"Ranges");
    this.ranges = ranges;
  }
  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    if (ranges != null)
      for(IndexExpNode ien: ranges){
      sb.append(ien.myString()+"; ");
      }
    return sb.toString();
  }
}
