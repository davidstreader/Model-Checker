package mc.compiler.ast;

import mc.util.Location;

/**
 * This Node describes an action (or transition)
 * <p>
 * e.g. {@code a} in {@code A = a->STOP.}
 * <p>
 * This node is only used within other nodes, and is rarely accessed outside of the AST itself
 *
 * @author Jacob Beal
 * @see InterruptNode
 * @see SequenceNode
 */
public class ActionLabelNode extends ASTNode {

  private String action;

  /**
   * Constructor for the action label.
   *
   * @param action   the text of the label for the action {@link #action}
   * @param location the location within the code where this action appears {@link ASTNode#location}
   */
  public ActionLabelNode(String action, Location location) {
    super(location,"Label");
    this.action = action;
  }
  public String myString() {
    return action;
  }

  public String getAction() {
    return this.action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String toString() {
    return "ActionLabelNode(action=" + this.getAction() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ActionLabelNode)) return false;
    final ActionLabelNode other = (ActionLabelNode) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$action = this.getAction();
    final Object other$action = other.getAction();
    if (this$action == null ? other$action != null : !this$action.equals(other$action)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ActionLabelNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $action = this.getAction();
    result = result * PRIME + ($action == null ? 43 : $action.hashCode());
    return result;
  }
}
