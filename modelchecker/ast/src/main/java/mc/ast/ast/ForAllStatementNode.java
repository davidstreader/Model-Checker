package mc.ast.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * a "ForAll" statement, this is a programmatic way of parallel composing multiple processes using
 * event indexing.
 * <p>
 * e.g. {@code ForAllTest = forall [i:1..2] (do[i] -> STOP).}
 * This expands to {@code ForAllTest = (do[1] STOP) || (do[2] -> STOP).}
 * <p>
 * The grammar for this is: {@code FORALL :: "forall" RANGE PROCESS.}
 *
 * @author Jacob Beal
 * @see CompositeNode
 * @see ASTNode
 * @see mc.compiler.Expander
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ForAllStatementNode extends ASTNode {

  /**
   * The ranges upon which the ForAll statement will apply.
   */
  private RangesNode ranges;
  /**
   * The process that shall be iterated through to compose.
   */
  private ASTNode process;

  /**
   * Instantiate a new instance of ForAllNode.
   *
   * @param ranges   The range upon which this node shall apply {@link #ranges}
   * @param process  The process that shall be used to create the composition {@link #process}
   * @param location The location within the users code where this
   *                 node is located {@link ASTNode#location}
   */
  public ForAllStatementNode(RangesNode ranges, ASTNode process, Location location) {
    super(location);
    this.ranges = ranges;
    this.process = process;
  }
}
