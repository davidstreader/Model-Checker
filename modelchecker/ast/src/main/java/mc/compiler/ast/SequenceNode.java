package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * SequenceNode represents a transition to another node.
 * <p>
 * The syntax of this is: {@code b->}
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see InterruptNode
 * @see ActionLabelNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SequenceNode extends ASTNode {

  /**
   * The label of the transition.
   */
  private ActionLabelNode from;

  /**
   * The next ASTNode (where this transitions to).
   */
  private ASTNode to;

  /**
   * New instance of SequenceNode.
   *
   * @param from     the label of the action. {@link #from}
   * @param to       the node where this transitions to. {@link #to}
   * @param location the location this node is within the users code {@link ASTNode#location}
   */
  public SequenceNode(ActionLabelNode from, ASTNode to, Location location) {
    super(location);
    this.from = from;
    this.to = to;
  }
}
