package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * IndexNode stores the current state of a process through a range, or set operation.
 * <p>
 * This is evaluated in Expander NOT Interpreter
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ASTNode
 * @see RangesNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IndexExpNode extends ASTNode {

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
  public IndexExpNode(String variable, ASTNode range, ASTNode process, Location location) {
    super(location,"Index");
    this.variable = variable;
    this.range = range;
    this.process = process;
  }
  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("IEN "+variable+" process ");
    if (process!=null) sb.append(process.myString() );
    //sb.append(" range ");
    if (range!=null) sb.append( range.toString());  // not sure what type this ASTNode is?
     sb.append("end of IEN");
    return sb.toString();
  }
}
