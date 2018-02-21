package mc.ast.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * The ReferenceNode refers to a location within the same process
 * (i.e. local processes or self-references)
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see IdentifierNode
 * @see ASTNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReferenceNode extends ASTNode {

  /**
   * The string label of where this is referring to.
   */
  private String reference;

  /**
   * Instantiate a new Reference Node.
   *
   * @param reference the label for where this node is referring to. {@link #reference}
   * @param location  where within the userscode where this node appears. {@link ASTNode#location}
   */
  public ReferenceNode(String reference, Location location) {
    super(location);
    this.reference = reference;
  }
}
