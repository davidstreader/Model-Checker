package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * A dummy node, for testing purposes only.
 *
 * @see ASTNode
 * @author Sanjay Govind
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EmptyNode extends ASTNode {

  /**
   * Initialise the Node.
   */
  public EmptyNode() {
    super(null);
  }
}
