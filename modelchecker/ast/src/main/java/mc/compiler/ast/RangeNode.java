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
 * @see RangesNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RangeNode extends ASTNode {

  /**
   * 
   */
  private int start;
  private int end;

  public RangeNode(int start, int end, Location location) {
    super(location);
    this.start = start;
    this.end = end;
  }
}
