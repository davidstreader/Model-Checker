package mc.compiler.ast;

import mc.util.Location;

/**
 * This contains a continuous range between two indexes.
 * <p>
 * RANGE :: "[" INT ".." INT "]"
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 * @see RangesNode
 */
public class RangeNode extends ASTNode {

  /**
   * The starting number in the range.
   */
  private int start;
  /**
   * The ending number in the range.
   */
  private int end;

  /**
   * Instantiate a new RangeNode.
   *
   * @param start    the starting number in the range {@link #start}
   * @param end      the ending number in the range {@link #end}
   * @param location the location of the RangeNode within the users code {@link ASTNode#location}
   */
  public RangeNode(int start, int end, Location location) {
    super(location,"Range");
    this.start = start;
    this.end = end;
  }

  public int getStart() {
    return this.start;
  }

  public int getEnd() {
    return this.end;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public void setEnd(int end) {
    this.end = end;
  }

  public String toString() {
    return "RangeNode(start=" + this.getStart() + ", end=" + this.getEnd() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof RangeNode)) return false;
    final RangeNode other = (RangeNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    if (this.getStart() != other.getStart()) return false;
    if (this.getEnd() != other.getEnd()) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof RangeNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    result = result * PRIME + this.getStart();
    result = result * PRIME + this.getEnd();
    return result;
  }
}
