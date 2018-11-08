package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * The ConstNode represents a numerical literal value within the code.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 * @see IdentifierNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ConstParseOnlyNode extends ASTNode {

  /**
   * The numeric value associated with the node.
   */
  private int value;

  /**
   * This initialises an instance of the node.
   *
   * @param value    the numerical value of the node {@link #value}
   * @param location the location in the code where this occurs {@link ASTNode#location}
   */
  public ConstParseOnlyNode(int value, Location location) {
    super(location,"ConstParse");
    this.value = value;
  }
  public String myString(){
    return " "+value;
  }
}
