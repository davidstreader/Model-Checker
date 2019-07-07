package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@Data
@EqualsAndHashCode(callSuper = true)
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
}
