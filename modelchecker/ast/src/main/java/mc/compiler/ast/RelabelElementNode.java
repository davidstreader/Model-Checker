package mc.compiler.ast;

import mc.util.Location;

/**
 * This is a single label transition name change.
 *
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @see RelabelNode
 * @see ProcessRootNode
 * @see ASTNode
 */
public class RelabelElementNode extends ASTNode {

  /**
   * The new name for the transition after the application of the relabel.
   */
  private String newLabel;

  private IdentifierNode newProcess;
  /**
   * The old name for the transition after the application of the relabel.
   */
  private String oldLabel;

  /**
   * The various processes upon which this applies (use is in indexed processes).
   */
  private RangesNode ranges;

  public RelabelElementNode(String newLabel, String oldLabel, Location location) {
    super(location,"Relabel");
    this.newLabel = newLabel;
    this.oldLabel = oldLabel;
    ranges = null;
  }
  public RelabelElementNode(IdentifierNode newPro, String oldLabel, Location location) {
    super(location,"Relabel");
    System.out.println("RelabelEl IdNode");
    this.oldLabel = oldLabel;
    this.newProcess = newPro;
    ranges = null;
  }
  public boolean hasRanges() {
    return ranges != null;
  }

  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("Relable ");
      if (newLabel == null) sb.append(" newLab==null ");
      else sb.append(" newLab "+ newLabel);
      if (newProcess == null) sb.append("newProcess==null ");
      else sb.append(" newProcess "+ newProcess);
      if (oldLabel == null) sb.append("oldLab==null ");
      else sb.append(" oldLab "+ oldLabel);
      if (ranges == null) sb.append("  ranges=null ");
    else sb.append(" "+ranges.myString());
    return sb.toString();
  }

  public String getNewLabel() {
    return this.newLabel;
  }

  public IdentifierNode getNewProcess() {
    return this.newProcess;
  }

  public String getOldLabel() {
    return this.oldLabel;
  }

  public RangesNode getRanges() {
    return this.ranges;
  }

  public void setNewLabel(String newLabel) {
    this.newLabel = newLabel;
  }

  public void setNewProcess(IdentifierNode newProcess) {
    this.newProcess = newProcess;
  }

  public void setOldLabel(String oldLabel) {
    this.oldLabel = oldLabel;
  }

  public void setRanges(RangesNode ranges) {
    this.ranges = ranges;
  }

  public String toString() {
    return "RelabelElementNode(newLabel=" + this.getNewLabel() + ", newProcess=" + this.getNewProcess() + ", oldLabel=" + this.getOldLabel() + ", ranges=" + this.getRanges() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof RelabelElementNode)) return false;
    final RelabelElementNode other = (RelabelElementNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$newLabel = this.getNewLabel();
    final Object other$newLabel = other.getNewLabel();
    if (this$newLabel == null ? other$newLabel != null : !this$newLabel.equals(other$newLabel)) return false;
    final Object this$newProcess = this.getNewProcess();
    final Object other$newProcess = other.getNewProcess();
    if (this$newProcess == null ? other$newProcess != null : !this$newProcess.equals(other$newProcess)) return false;
    final Object this$oldLabel = this.getOldLabel();
    final Object other$oldLabel = other.getOldLabel();
    if (this$oldLabel == null ? other$oldLabel != null : !this$oldLabel.equals(other$oldLabel)) return false;
    final Object this$ranges = this.getRanges();
    final Object other$ranges = other.getRanges();
    if (this$ranges == null ? other$ranges != null : !this$ranges.equals(other$ranges)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof RelabelElementNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $newLabel = this.getNewLabel();
    result = result * PRIME + ($newLabel == null ? 43 : $newLabel.hashCode());
    final Object $newProcess = this.getNewProcess();
    result = result * PRIME + ($newProcess == null ? 43 : $newProcess.hashCode());
    final Object $oldLabel = this.getOldLabel();
    result = result * PRIME + ($oldLabel == null ? 43 : $oldLabel.hashCode());
    final Object $ranges = this.getRanges();
    result = result * PRIME + ($ranges == null ? 43 : $ranges.hashCode());
    return result;
  }
}
