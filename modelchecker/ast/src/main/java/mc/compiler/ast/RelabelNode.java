package mc.compiler.ast;

import mc.util.Location;

import java.util.List;

/**
 * This contains the relabel for a given root process.
 * This is only used with {@code ProcessRootNode}.
 * <p>
 * Gramatically this is:
 * {@code RELABELS :: "/{" (RELABEL ",")* RELABEL "}"}
 * <p>
 * {@code RELABEL :: OLDTRANSITION "/" NEWTRANSITION}
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @author Jordan Smith
 * @see ProcessRootNode
 * @see RelabelElementNode
 */
public class RelabelNode extends ASTNode {

  /**
   * All of the relabels that should take place.
   */
  private List<RelabelElementNode> relabels;

  /**
   * Instantiate a new RelabelNode.
   *
   * @param relabels a list of the relabels to take place. {@link #relabels}
   * @param location the location of the node within the users code {@link ASTNode#location}
   */
  public RelabelNode(List<RelabelElementNode> relabels, Location location) {
    super(location,"RelabelNode");
    this.relabels = relabels;
  }

  public List<RelabelElementNode> getRelabels() {
    return this.relabels;
  }

  public void setRelabels(List<RelabelElementNode> relabels) {
    this.relabels = relabels;
  }

  public String toString() {
    return "RelabelNode(relabels=" + this.getRelabels() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof RelabelNode)) return false;
    final RelabelNode other = (RelabelNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$relabels = this.getRelabels();
    final Object other$relabels = other.getRelabels();
    if (this$relabels == null ? other$relabels != null : !this$relabels.equals(other$relabels)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof RelabelNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $relabels = this.getRelabels();
    result = result * PRIME + ($relabels == null ? 43 : $relabels.hashCode());
    return result;
  }
}
