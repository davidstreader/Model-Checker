package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * The ConstNode represents a numerical literal value within the code.
 *
 * @see ASTNode
 * @see IdentifierNode
 * @author Jacob Beal
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConstNode extends ASTNode {

  /**
   * The numeric value associated with the node.
   */
  private int value;

  /**
   * This initialises an instance of the node.
   *
   * @param value the numerical value of the node {@link #value}
   * @param location the location in the code where this occurs {@link ASTNode#location}
   */
  public ConstNode(int value, Location location) {
    super(location);
    this.value = value;
  }
}
