package mc.compiler.ast;

import mc.util.Location;

import java.util.Set;

/**
 * CompositeNode represents any infix operation handled in the code.
 * <p>
 * Uses of CompositeNode includes (by default): Parallel Composition ({@code ||}) and
 * the sequential operator ({@code =>})
 * <p>
 * An example of this may be {@code A = B||C.}
 *
 * @author Jacob Beal  - David Streader
 * @see ASTNode
 * @see FunctionNode
 *
 */
public class CompositeNode extends ASTNode {

  /**
   * The first process used in the infix operation.
   * <p>
   * e.g. {@code B} in {@code A=B||C}
   */
  private ASTNode firstProcess;
  /**
   * The second process used in the infix operation.
   * <p>
   * e.g. {@code C} in {@code A=B||C}
   */
  private ASTNode secondProcess;
  /**
   * The symbol for the operation used in the infix operation.
   * <p>
   * e.g. {@code ||} in {@code A=B||C}
   * <p>
   * By default accepted values should be {@code ||} and {@code =>}
   */
  private String operation;

  private Set<String> flags;

  /**
   * @param operation     the type of operation used (e.g. {@code ||}), specifies
   *                      what is invoked later on{@link #operation}
   * @param firstProcess  the first process in the operation {@link #firstProcess}
   * @param secondProcess the second process in the operation {@link #secondProcess}
   * @param location      The location of the operation within the code {@link ASTNode#location}
   */
  public CompositeNode(String operation, ASTNode firstProcess, ASTNode secondProcess, Location location, Set<String> flags) {
    super(location,"Composite");
    this.operation = operation;
    this.firstProcess = firstProcess;
    this.secondProcess = secondProcess;
    this.flags = flags;
    //System.out.println("CompositeNode "+operation+flags);
  }
  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("("+firstProcess.myString());
    sb.append(operation);
    if (flags.size()>0) sb.append(flags);
    sb.append(secondProcess.myString()+")");
    return sb.toString();
  }

  @Override
  public CompositeNode instantiate(String from , String to) {
    return new CompositeNode(operation,firstProcess.instantiate(from,to),secondProcess.instantiate(from,to), getLocation(),flags);
  }

  public ASTNode getFirstProcess() {
    return this.firstProcess;
  }

  public ASTNode getSecondProcess() {
    return this.secondProcess;
  }

  public String getOperation() {
    return this.operation;
  }

  public Set<String> getFlags() {
    return this.flags;
  }

  public void setFirstProcess(ASTNode firstProcess) {
    this.firstProcess = firstProcess;
  }

  public void setSecondProcess(ASTNode secondProcess) {
    this.secondProcess = secondProcess;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public void setFlags(Set<String> flags) {
    this.flags = flags;
  }

  public String toString() {
    return "CompositeNode(firstProcess=" + this.getFirstProcess() + ", secondProcess=" + this.getSecondProcess() + ", operation=" + this.getOperation() + ", flags=" + this.getFlags() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof CompositeNode)) return false;
    final CompositeNode other = (CompositeNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$firstProcess = this.getFirstProcess();
    final Object other$firstProcess = other.getFirstProcess();
    if (this$firstProcess == null ? other$firstProcess != null : !this$firstProcess.equals(other$firstProcess))
      return false;
    final Object this$secondProcess = this.getSecondProcess();
    final Object other$secondProcess = other.getSecondProcess();
    if (this$secondProcess == null ? other$secondProcess != null : !this$secondProcess.equals(other$secondProcess))
      return false;
    final Object this$operation = this.getOperation();
    final Object other$operation = other.getOperation();
    if (this$operation == null ? other$operation != null : !this$operation.equals(other$operation)) return false;
    final Object this$flags = this.getFlags();
    final Object other$flags = other.getFlags();
    if (this$flags == null ? other$flags != null : !this$flags.equals(other$flags)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof CompositeNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $firstProcess = this.getFirstProcess();
    result = result * PRIME + ($firstProcess == null ? 43 : $firstProcess.hashCode());
    final Object $secondProcess = this.getSecondProcess();
    result = result * PRIME + ($secondProcess == null ? 43 : $secondProcess.hashCode());
    final Object $operation = this.getOperation();
    result = result * PRIME + ($operation == null ? 43 : $operation.hashCode());
    final Object $flags = this.getFlags();
    result = result * PRIME + ($flags == null ? 43 : $flags.hashCode());
    return result;
  }
}