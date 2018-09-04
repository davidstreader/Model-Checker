package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * ChoiceNode represents a choice in the syntax of the LTS. When used, this appears as two (or more)
 * possible actions happening from the same state.
 * <p>
 * Syntax: {@code Choice :: Process "|" Process}
 *
 * @author Jacob Beal
 * @see ASTNode
 * @see FunctionNode
 * @see CompositeNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceNode extends ASTNode {

  /**
   * The first process used in the choice.
   * <p>
   * e.g. {@code B} in {@code A=B|C}
   */
  private ASTNode firstProcess;

  /**
   * The second process used in the choice.
   * <p>
   * e.g. {@code C} in {@code A=B|C}
   */
  private ASTNode secondProcess;

  /**
   * Instantiate ChoiceNode.
   *
   * @param firstProcess  the first process branch for the 'choice' {@link #firstProcess}
   * @param secondProcess the second processbranch for the 'choice' {@link #secondProcess}
   * @param location      the location of the choice within the users code {@link ASTNode#location}
   */
  public ChoiceNode(ASTNode firstProcess, ASTNode secondProcess, Location location) {
    super(location,"Choice");
    this.firstProcess = firstProcess;
    this.secondProcess = secondProcess;
  }
}
