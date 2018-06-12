package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;


/**
 * This represents an infix operation, for within the operations.
 * <p>
 * By default, this contains {@code bismulation (~)} and {@code traceEquivalent (#)}
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 * @see mc.plugins.IOperationInfixFunction
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OperationNode extends ASTNode {

  /**
   * The symbol used in the operation.
   * <p>
   * e.g. {@code ~}
   */
  private String operation;
  /**
   * Whether or not the result should be inverted (i.e. a {@code !} in the operation)
   */
  private boolean negated;
  /**
   * The first process to be operated on.
   */
  private ASTNode firstProcess;
  /**
   * The second process to be operated on.
   */
  private ASTNode secondProcess;

  private String firstProcessType = "petrinet";
  private String secondProcessType = "petrinet";
  private String operationType = "petrinet";
  /**
   * Instantitate a new Operation Node.
   *
   * @param operation        the symbolic representation of the operation {@link #operation}
   * @param isNegated        whether or not the operation result should be inversed {@link #negated}
   * @param firstProcess     the first process to be operated on {@link #firstProcess}
   * @param secondProcess    the second process to be operated on {@link #secondProcess}
   * @param location         the location within the users code where this takes
   *                         place {@link ASTNode#location}
   */
  public OperationNode(String operation, boolean isNegated, ASTNode firstProcess,
                       ASTNode secondProcess, Location location) {
    super(location);
    this.operation = operation;
    this.negated = isNegated;
    this.firstProcess = firstProcess;
    this.secondProcess = secondProcess;
  }
}
