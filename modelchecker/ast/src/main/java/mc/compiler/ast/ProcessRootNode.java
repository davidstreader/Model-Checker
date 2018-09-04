package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;



/**
 * ProcessRootNode contains a composite process within an existing process.
 * (i.e. {@code B} in {@code A=B\{c}})
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ProcessNode
 * @see CompositeNode
 * @see
 */
@Data
@EqualsAndHashCode(callSuper = true)
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
}
