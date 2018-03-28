package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A dummy node, for testing purposes only.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmptyTestOnlyNode extends ASTNode {

  /**
   * Initialise the Node.
   */
  public EmptyTestOnlyNode() {
    super(null);
  }
}
