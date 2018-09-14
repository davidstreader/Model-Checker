package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

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
@Data
@EqualsAndHashCode(callSuper = true)
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

  /**
   * @param operation     the type of operation used (e.g. {@code ||}), specifies
   *                      what is invoked later on{@link #operation}
   * @param firstProcess  the first process in the operation {@link #firstProcess}
   * @param secondProcess the second process in the operation {@link #secondProcess}
   * @param location      The location of the operation within the code {@link ASTNode#location}
   */
  public CompositeNode(String operation, ASTNode firstProcess, ASTNode secondProcess, Location location) {
    super(location,"Composite");
    this.operation = operation;
    this.firstProcess = firstProcess;
    this.secondProcess = secondProcess;
  }
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("("+firstProcess.myString());
    sb.append(operation);
    sb.append(secondProcess.myString()+")");
    return sb.toString();
  }
}