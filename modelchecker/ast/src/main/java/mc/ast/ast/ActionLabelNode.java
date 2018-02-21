package mc.ast.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * This Node describes an action (or transition)
 * <p>
 * e.g. {@code a} in {@code A = a->STOP.}
 * <p>
 * This node is only used within other nodes, and is rarely accessed outside of the AST itself
 *
 * @author Jacob Beal
 * @see mc.compiler.Expander
 * @see InterruptNode
 * @see SequenceNode
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ActionLabelNode extends ASTNode {

  private String action;

  /**
   * Constructor for the action label.
   *
   * @param action   the text of the label for the action {@link #action}
   * @param location the location within the code where this action appears {@link ASTNode#location}
   */
  public ActionLabelNode(String action, Location location) {
    super(location);
    this.action = action;
  }
}
