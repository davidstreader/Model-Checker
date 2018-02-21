package mc.ast.ast;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

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
@Data
@EqualsAndHashCode(callSuper = true)
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
    super(location);
    this.relabels = relabels;
  }
}
