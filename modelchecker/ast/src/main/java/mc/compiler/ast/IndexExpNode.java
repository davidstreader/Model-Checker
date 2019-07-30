package mc.compiler.ast;

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

  public String getVariable() {
    return this.variable;
  }

  public ASTNode getRange() {
    return this.range;
  }

  public ASTNode getProcess() {
    return this.process;
  }

  public void setVariable(String variable) {
    this.variable = variable;
  }

  public void setRange(ASTNode range) {
    this.range = range;
  }

  public void setProcess(ASTNode process) {
    this.process = process;
  }

  public String toString() {
    return "IndexExpNode(variable=" + this.getVariable() + ", range=" + this.getRange() + ", process=" + this.getProcess() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof IndexExpNode)) return false;
    final IndexExpNode other = (IndexExpNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$variable = this.getVariable();
    final Object other$variable = other.getVariable();
    if (this$variable == null ? other$variable != null : !this$variable.equals(other$variable)) return false;
    final Object this$range = this.getRange();
    final Object other$range = other.getRange();
    if (this$range == null ? other$range != null : !this$range.equals(other$range)) return false;
    final Object this$process = this.getProcess();
    final Object other$process = other.getProcess();
    if (this$process == null ? other$process != null : !this$process.equals(other$process)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof IndexExpNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $variable = this.getVariable();
    result = result * PRIME + ($variable == null ? 43 : $variable.hashCode());
    final Object $range = this.getRange();
    result = result * PRIME + ($range == null ? 43 : $range.hashCode());
    final Object $process = this.getProcess();
    result = result * PRIME + ($process == null ? 43 : $process.hashCode());
    return result;
  }
}
