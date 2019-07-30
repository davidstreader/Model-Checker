package mc.compiler.ast;

import com.google.common.collect.ImmutableSet;
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

  public String getOperation() {
    return this.operation;
  }

  public boolean isNegated() {
    return this.negated;
  }

  public ImmutableSet<String> getFlags() {
    return this.flags;
  }

  public ASTNode getFirstProcess() {
    return this.firstProcess;
  }

  public ASTNode getSecondProcess() {
    return this.secondProcess;
  }

  public String getFirstProcessType() {
    return this.firstProcessType;
  }

  public String getSecondProcessType() {
    return this.secondProcessType;
  }

  public String getOperationType() {
    return this.operationType;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public void setNegated(boolean negated) {
    this.negated = negated;
  }

  public void setFlags(ImmutableSet<String> flags) {
    this.flags = flags;
  }

  public void setFirstProcess(ASTNode firstProcess) {
    this.firstProcess = firstProcess;
  }

  public void setSecondProcess(ASTNode secondProcess) {
    this.secondProcess = secondProcess;
  }

  public void setFirstProcessType(String firstProcessType) {
    this.firstProcessType = firstProcessType;
  }

  public void setSecondProcessType(String secondProcessType) {
    this.secondProcessType = secondProcessType;
  }

  public void setOperationType(String operationType) {
    this.operationType = operationType;
  }

  public String toString() {
    return "OperationNode(operation=" + this.getOperation() + ", negated=" + this.isNegated() + ", flags=" + this.getFlags() + ", firstProcess=" + this.getFirstProcess() + ", secondProcess=" + this.getSecondProcess() + ", firstProcessType=" + this.getFirstProcessType() + ", secondProcessType=" + this.getSecondProcessType() + ", operationType=" + this.getOperationType() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof OperationNode)) return false;
    final OperationNode other = (OperationNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$operation = this.getOperation();
    final Object other$operation = other.getOperation();
    if (this$operation == null ? other$operation != null : !this$operation.equals(other$operation)) return false;
    if (this.isNegated() != other.isNegated()) return false;
    final Object this$flags = this.getFlags();
    final Object other$flags = other.getFlags();
    if (this$flags == null ? other$flags != null : !this$flags.equals(other$flags)) return false;
    final Object this$firstProcess = this.getFirstProcess();
    final Object other$firstProcess = other.getFirstProcess();
    if (this$firstProcess == null ? other$firstProcess != null : !this$firstProcess.equals(other$firstProcess))
      return false;
    final Object this$secondProcess = this.getSecondProcess();
    final Object other$secondProcess = other.getSecondProcess();
    if (this$secondProcess == null ? other$secondProcess != null : !this$secondProcess.equals(other$secondProcess))
      return false;
    final Object this$firstProcessType = this.getFirstProcessType();
    final Object other$firstProcessType = other.getFirstProcessType();
    if (this$firstProcessType == null ? other$firstProcessType != null : !this$firstProcessType.equals(other$firstProcessType))
      return false;
    final Object this$secondProcessType = this.getSecondProcessType();
    final Object other$secondProcessType = other.getSecondProcessType();
    if (this$secondProcessType == null ? other$secondProcessType != null : !this$secondProcessType.equals(other$secondProcessType))
      return false;
    final Object this$operationType = this.getOperationType();
    final Object other$operationType = other.getOperationType();
    if (this$operationType == null ? other$operationType != null : !this$operationType.equals(other$operationType))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof OperationNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $operation = this.getOperation();
    result = result * PRIME + ($operation == null ? 43 : $operation.hashCode());
    result = result * PRIME + (this.isNegated() ? 79 : 97);
    final Object $flags = this.getFlags();
    result = result * PRIME + ($flags == null ? 43 : $flags.hashCode());
    final Object $firstProcess = this.getFirstProcess();
    result = result * PRIME + ($firstProcess == null ? 43 : $firstProcess.hashCode());
    final Object $secondProcess = this.getSecondProcess();
    result = result * PRIME + ($secondProcess == null ? 43 : $secondProcess.hashCode());
    final Object $firstProcessType = this.getFirstProcessType();
    result = result * PRIME + ($firstProcessType == null ? 43 : $firstProcessType.hashCode());
    final Object $secondProcessType = this.getSecondProcessType();
    result = result * PRIME + ($secondProcessType == null ? 43 : $secondProcessType.hashCode());
    final Object $operationType = this.getOperationType();
    result = result * PRIME + ($operationType == null ? 43 : $operationType.hashCode());
    return result;
  }
}
