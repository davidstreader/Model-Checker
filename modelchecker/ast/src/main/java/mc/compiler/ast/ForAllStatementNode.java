package mc.compiler.ast;

import mc.util.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * a "ForAll" statement, this is a programmatic way of parallel composing multiple processes using
 * event indexing.
 * <p>
 * e.g. {@code ForAllTest = forall [i:1..2] (do[i] -> STOP).}
 * This expands to {@code ForAllTest = (do[1] STOP) || (do[2] -> STOP).}
 * <p>
 * The grammar for this is: {@code FORALL :: "forall" RANGE PROCESS.}
 *
 * A second use is controlling the variable expansion in equations
 * forall{X}(P(X,Y,Z) ) ==> Q(Y,Z)
 *
 * @author Jacob Beal
 * @see CompositeNode
 * @see ASTNode
 *
 */
public class ForAllStatementNode extends ASTNode {
   private     List<LocalProcessNode> localProcesses;
   // used to pass any localprocesses up the parse chain to the ProcessNode
  /**
   * The ranges upon which the ForAll statement will apply.
   */
  private RangesNode ranges;
  /**
   *
   */
  private List<String> variables = new ArrayList<>();  // bound variables
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
    super(location,"Forall");
    this.ranges = ranges;
    this.process = process;
  }
  public ForAllStatementNode(List<String> variables,  Location location) {
    super(location,"Forall");
    this.variables = variables;
    System.out.println("Built forall "+variables);
  }

  public String myString(){
   StringBuilder sb = new StringBuilder();
    if (process !=null) {
      sb.append(process.myString()+" ");
    }
    if (variables!=null) sb.append("variables "+variables);
    if (ranges!=null) sb.append("ranges "+ranges.myString());
    return "forall "+sb.toString();
  }

  public List<LocalProcessNode> getLocalProcesses() {
    return this.localProcesses;
  }

  public RangesNode getRanges() {
    return this.ranges;
  }

  public List<String> getVariables() {
    return this.variables;
  }

  public ASTNode getProcess() {
    return this.process;
  }

  public void setLocalProcesses(List<LocalProcessNode> localProcesses) {
    this.localProcesses = localProcesses;
  }

  public void setRanges(RangesNode ranges) {
    this.ranges = ranges;
  }

  public void setVariables(List<String> variables) {
    this.variables = variables;
  }

  public void setProcess(ASTNode process) {
    this.process = process;
  }

  public String toString() {
    return "ForAllStatementNode(localProcesses=" + this.getLocalProcesses() + ", ranges=" + this.getRanges() + ", variables=" + this.getVariables() + ", process=" + this.getProcess() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ForAllStatementNode)) return false;
    final ForAllStatementNode other = (ForAllStatementNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$localProcesses = this.getLocalProcesses();
    final Object other$localProcesses = other.getLocalProcesses();
    if (this$localProcesses == null ? other$localProcesses != null : !this$localProcesses.equals(other$localProcesses))
      return false;
    final Object this$ranges = this.getRanges();
    final Object other$ranges = other.getRanges();
    if (this$ranges == null ? other$ranges != null : !this$ranges.equals(other$ranges)) return false;
    final Object this$variables = this.getVariables();
    final Object other$variables = other.getVariables();
    if (this$variables == null ? other$variables != null : !this$variables.equals(other$variables)) return false;
    final Object this$process = this.getProcess();
    final Object other$process = other.getProcess();
    if (this$process == null ? other$process != null : !this$process.equals(other$process)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ForAllStatementNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $localProcesses = this.getLocalProcesses();
    result = result * PRIME + ($localProcesses == null ? 43 : $localProcesses.hashCode());
    final Object $ranges = this.getRanges();
    result = result * PRIME + ($ranges == null ? 43 : $ranges.hashCode());
    final Object $variables = this.getVariables();
    result = result * PRIME + ($variables == null ? 43 : $variables.hashCode());
    final Object $process = this.getProcess();
    result = result * PRIME + ($process == null ? 43 : $process.hashCode());
    return result;
  }
}
