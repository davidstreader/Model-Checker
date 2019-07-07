
package mc.compiler.ast;

  import lombok.Data;
  import lombok.EqualsAndHashCode;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ImpliesNode;
import mc.compiler.ast.OperationNode;
import mc.util.Location;


/**
 * This represents an conjunction. Used to compute congruance of tests
 *
 * @author David Streader
 * @see ASTNode
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AndNode extends OperationNode {

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
  public AndNode( ASTNode firstOperation,
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
    System.out.println("and Location "+location.toString());
  }
  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("("+firstOperation.myString());
    sb.append(" && ");
    sb.append(secondOperation.myString()+")");
    return sb.toString();
  }
  @Override
  public AndNode instantiate(String from , String to) {
    return new AndNode(firstOperation.instantiate(from,to),secondOperation.instantiate(from,to), getLocation());
  }
}

