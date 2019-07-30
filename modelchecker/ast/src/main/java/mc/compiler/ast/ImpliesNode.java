package mc.compiler.ast;

import mc.util.Location;


/**
 * This represents an implication. Used to compute Galois connectons
 *
 * @author David Streader
 * @see ASTNode
 *
 */
public class ImpliesNode extends OperationNode {

  /**
   * The symbol used in the operation.
   * <p>
   * e.g. {@code ~}
   */

  private ASTNode firstOperation;
  /**
   * The second process to be operated on.
   */
  private ASTNode secondOperation;
  public ASTNode getFirstOperation() { return firstOperation;}
  public ASTNode getSecondOperation() { return secondOperation;}

  /**
   * first implies second
   *
   * @param firstOperation     the first Operation
   * @param secondOperation    the second Operation
   * @param location         the location within the users code where this takes
   *                         place {@link ASTNode#location}
   */
  public ImpliesNode( ASTNode firstOperation,
                       ASTNode secondOperation, Location location) {
   // super(location,"Implies");
    super(location);
    if(firstOperation==null|| secondOperation==null) {
      Throwable t = new Throwable(); t.printStackTrace();
      System.out.println("*******************\n\n");
    }
    this.firstOperation = firstOperation;
    this.secondOperation = secondOperation;
    //this.setFlags(Collections.singletonList("*"));
    System.out.println("implies Location "+location.toString());
  }
  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("("+firstOperation.myString());
    sb.append(" ==> ");
    sb.append(secondOperation.myString()+")");
    return sb.toString();
  }
  @Override
  public ImpliesNode instantiate(String from , String to) {
    return new ImpliesNode(firstOperation.instantiate(from,to),secondOperation.instantiate(from,to), getLocation());
  }

  public void setFirstOperation(ASTNode firstOperation) {
    this.firstOperation = firstOperation;
  }

  public void setSecondOperation(ASTNode secondOperation) {
    this.secondOperation = secondOperation;
  }

  public String toString() {
    return "ImpliesNode(firstOperation=" + this.getFirstOperation() + ", secondOperation=" + this.getSecondOperation() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ImpliesNode)) return false;
    final ImpliesNode other = (ImpliesNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$firstOperation = this.getFirstOperation();
    final Object other$firstOperation = other.getFirstOperation();
    if (this$firstOperation == null ? other$firstOperation != null : !this$firstOperation.equals(other$firstOperation))
      return false;
    final Object this$secondOperation = this.getSecondOperation();
    final Object other$secondOperation = other.getSecondOperation();
    if (this$secondOperation == null ? other$secondOperation != null : !this$secondOperation.equals(other$secondOperation))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ImpliesNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $firstOperation = this.getFirstOperation();
    result = result * PRIME + ($firstOperation == null ? 43 : $firstOperation.hashCode());
    final Object $secondOperation = this.getSecondOperation();
    result = result * PRIME + ($secondOperation == null ? 43 : $secondOperation.hashCode());
    return result;
  }
}

