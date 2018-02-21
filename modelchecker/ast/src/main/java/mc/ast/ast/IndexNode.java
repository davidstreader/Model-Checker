package mc.ast.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * IndexNode stores the current state of a process through a range, or set operation.
 * <p>
 * This
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 * @see RangesNode
 * @see mc.compiler.Expander
 * @see mc.compiler.Parser
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IndexNode extends ASTNode {

  /**
   * The name of the variable used within this indexing operation.
   */
  private String variable;
  /**
   * The parent node of this index node, through which this is iterated through.
   * <p>
   * This must be either a {@link SetNode}, or {@link RangesNode}.
   *
   * @see SetNode
   * @see RangesNode
   */
  private ASTNode range;
  private ASTNode process;

  /**
   * Initialises a new instance of IndexNode.
   *
   * @param variable the label of the variable used through the iteration {@link #variable}
   * @param range    the range this index is a part of. This must be a {@link SetNode} or
   *                 {@link RangesNode}. {@link #range}
   * @param process  The process which is being iterated through {@link #process}
   * @param location The location which this is within the users code {@link ASTNode#location}
   */
  public IndexNode(String variable, ASTNode range, ASTNode process, Location location) {
    super(location);
    this.variable = variable;
    this.range = range;
    this.process = process;
  }
}
