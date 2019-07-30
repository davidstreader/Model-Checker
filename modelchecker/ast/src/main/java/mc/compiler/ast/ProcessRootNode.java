package mc.compiler.ast;

import mc.util.Location;



/**
 * ProcessRootNode is an optional wrapper used iff
 *         labeling processes, renameing events ot hiding events have been applied.
 * (i.e. {@code B} in {@code A=B\{c}})
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ProcessNode
 * @see CompositeNode
 * @see
 */
public class ProcessRootNode extends ASTNode {

  /**
   * The process itself under the root.
   */
  private ASTNode process;

  /**
   * if this process is labelled.
   * e.g. {@code abc} in {@code A=abc:B.}
   */
  private String label;

  /**
   * The relabels that are to be done on this process.
   */
  private RelabelNode relabelSet;
 // private IdentifierNode newProcess;
  /**
   * The hiding that happens on this process.
   */
  private HidingNode hiding;

  /**
   * Instantiate a new instance of ProcessRootNode
   * The ProcessRootNode  is optional but appears under a ProcessNode and
   *       holds the rewriting and hiding data
   *
   * @param process  the process that this node represents. {@link #process}
   * @param label    the label that this process is labelled by. {@link #label}
   * @param relabels the relabels for the process. {@link #relabelSet}
   * @param hiding   instructions on which transitions to replace with tau events {@link #hiding}
   * @param location where in the users code this node is {@link ASTNode#location}
   */
  public ProcessRootNode(ASTNode process, String label, RelabelNode relabels,
                         HidingNode hiding, Location location) {
    super(location,"ProcessesRoot");
    //System.out.println("ProcessRootNode BUILT");
    //Throwable t = new Throwable();  t.printStackTrace();

    this.process = process;
    this.label = label;
    this.relabelSet = relabels;
    this.hiding = hiding;
  }

  /**
   * Whether or not this process is labelled.
   *
   * @return if this process is labelled.
   */
  public boolean hasLabel() {
    return label != null;
  }

  /**
   * Whether or not this process has relabelling.
   *
   * @return if this process has relabelling
   */
  public boolean hasRelabelSet() {
    return relabelSet != null;
  }

  public boolean hasNewProcess() {
    boolean bo = false;
    if (relabelSet != null) bo = relabelSet.getRelabels().stream().
      map(x->x.getNewProcess()!=null).
      reduce(false,(x,y)->x||y);
    return bo;
  }

  /**
   * Whether or not this process has hiding.
   *
   * @return if this process has hiding
   */
  public boolean hasHiding() {
    return hiding != null;
  }

  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("Root"+label+" "+process.myString() );
    if (hiding!=null) sb.append(" h "+hiding.myString() );
    if (relabelSet!=null) sb.append(" r "+relabelSet.myString() );
    return sb.toString();
  }

  public ASTNode getProcess() {
    return this.process;
  }

  public String getLabel() {
    return this.label;
  }

  public RelabelNode getRelabelSet() {
    return this.relabelSet;
  }

  public HidingNode getHiding() {
    return this.hiding;
  }

  public void setProcess(ASTNode process) {
    this.process = process;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setRelabelSet(RelabelNode relabelSet) {
    this.relabelSet = relabelSet;
  }

  public void setHiding(HidingNode hiding) {
    this.hiding = hiding;
  }

  public String toString() {
    return "ProcessRootNode(process=" + this.getProcess() + ", label=" + this.getLabel() + ", relabelSet=" + this.getRelabelSet() + ", hiding=" + this.getHiding() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ProcessRootNode)) return false;
    final ProcessRootNode other = (ProcessRootNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$process = this.getProcess();
    final Object other$process = other.getProcess();
    if (this$process == null ? other$process != null : !this$process.equals(other$process)) return false;
    final Object this$label = this.getLabel();
    final Object other$label = other.getLabel();
    if (this$label == null ? other$label != null : !this$label.equals(other$label)) return false;
    final Object this$relabelSet = this.getRelabelSet();
    final Object other$relabelSet = other.getRelabelSet();
    if (this$relabelSet == null ? other$relabelSet != null : !this$relabelSet.equals(other$relabelSet)) return false;
    final Object this$hiding = this.getHiding();
    final Object other$hiding = other.getHiding();
    if (this$hiding == null ? other$hiding != null : !this$hiding.equals(other$hiding)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ProcessRootNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $process = this.getProcess();
    result = result * PRIME + ($process == null ? 43 : $process.hashCode());
    final Object $label = this.getLabel();
    result = result * PRIME + ($label == null ? 43 : $label.hashCode());
    final Object $relabelSet = this.getRelabelSet();
    result = result * PRIME + ($relabelSet == null ? 43 : $relabelSet.hashCode());
    final Object $hiding = this.getHiding();
    result = result * PRIME + ($hiding == null ? 43 : $hiding.hashCode());
    return result;
  }
}
