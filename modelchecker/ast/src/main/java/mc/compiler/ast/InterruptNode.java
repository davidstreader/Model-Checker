package mc.compiler.ast;

import mc.util.Location;

/**
 * This acts as an interrupt transition node. (Please note this is incomplete)
 * <p>
 * This is gramatically {@code INTERRUPT :: ACTIONLABEL "~>" PROCESS}
 * <p>
 * i.e. this is replacing a {@code ->} with a {@code ~>}
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 * @see ActionLabelNode
 * @deprecated This feature is unimplemented.
 */
public class InterruptNode extends ASTNode {

  /**
   * The action that this interrupt is labelled with.
   */
  private ActionLabelNode action;
  /**
   * The process that follows this InterruptNode.
   */
  private ASTNode process;

  /**
   * Initialises a new InterruptNode.
   *
   * @param action  the action that this InterruptNode represents (including label) {@link #action}
   * @param process the process that follows this interrupt {@link #process}
   * @param location the location this token is within the users code {@link ASTNode#location}
   */
  public InterruptNode(ActionLabelNode action, ASTNode process, Location location) {
    super(location,"Interupt");
    this.action = action;
    this.process = process;
  }

  public ActionLabelNode getAction() {
    return this.action;
  }

  public ASTNode getProcess() {
    return this.process;
  }

  public void setAction(ActionLabelNode action) {
    this.action = action;
  }

  public void setProcess(ASTNode process) {
    this.process = process;
  }

  public String toString() {
    return "InterruptNode(action=" + this.getAction() + ", process=" + this.getProcess() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof InterruptNode)) return false;
    final InterruptNode other = (InterruptNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$action = this.getAction();
    final Object other$action = other.getAction();
    if (this$action == null ? other$action != null : !this$action.equals(other$action)) return false;
    final Object this$process = this.getProcess();
    final Object other$process = other.getProcess();
    if (this$process == null ? other$process != null : !this$process.equals(other$process)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof InterruptNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $action = this.getAction();
    result = result * PRIME + ($action == null ? 43 : $action.hashCode());
    final Object $process = this.getProcess();
    result = result * PRIME + ($process == null ? 43 : $process.hashCode());
    return result;
  }
}
