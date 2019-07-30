package mc.compiler.ast;

import mc.util.Location;

import java.util.List;

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

  public List<IndexExpNode> getRanges() {
    return this.ranges;
  }

  public void setRanges(List<IndexExpNode> ranges) {
    this.ranges = ranges;
  }

  public String toString() {
    return "RangesNode(ranges=" + this.getRanges() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof RangesNode)) return false;
    final RangesNode other = (RangesNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$ranges = this.getRanges();
    final Object other$ranges = other.getRanges();
    if (this$ranges == null ? other$ranges != null : !this$ranges.equals(other$ranges)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof RangesNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $ranges = this.getRanges();
    result = result * PRIME + ($ranges == null ? 43 : $ranges.hashCode());
    return result;
  }
}
