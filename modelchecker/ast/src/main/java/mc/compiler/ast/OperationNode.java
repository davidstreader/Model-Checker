package mc.compiler.ast;

import com.google.common.collect.ImmutableSet;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.Constant;
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
 *
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
   * The arguments put into the function curly brace syntax.
   * Currently fixed at Compile Time - this includes the fixed alphabet of listeners
   *
   *
   */
  private ImmutableSet<String> flags;

  /**
   * The first process to be operated on.
   */
  private ASTNode firstProcess;
  /**
   * The second process to be operated on.
   */
  private ASTNode secondProcess;

  private String firstProcessType = Constant.PETRINET;
  private String secondProcessType = Constant.PETRINET;
  private String operationType = Constant.PETRINET;

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
  public OperationNode(String operation, boolean isNegated, ImmutableSet<String> flags, ASTNode firstProcess,
                       ASTNode secondProcess, Location location) {
    super(location,"Operation");
    this.operation = operation;
    this.negated = isNegated;
    this.firstProcess = firstProcess;
    this.secondProcess = secondProcess;
    this.flags = flags;
  }
  public OperationNode(Location location) {
    super(location,"Operation");
    this.operation = "==>";

    this.flags = ImmutableSet.of("*");
  }
  @Override
  public String myString(){
    String opOut;
    if (negated) opOut="!"+operation;
    else opOut=operation;
    StringBuilder sb = new StringBuilder();
    // if (firstProcess instanceof )
    sb.append("("+firstProcess.myString()  + opOut );
    if (flags != null && flags.size()>0) sb.append( flags );
    else sb.append(" ");
    sb.append( secondProcess.myString()+")");

    return sb.toString();
  }

  @Override
  public OperationNode instantiate(String from , String to) {
    return new OperationNode(operation,isNegated(),flags,
      firstProcess.instantiate(from,to),secondProcess.instantiate(from,to), getLocation());
  }
}
