package mc.ast.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@Data
@EqualsAndHashCode(callSuper = true)
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
    super(location);
    this.action = action;
    this.process = process;
  }
}
