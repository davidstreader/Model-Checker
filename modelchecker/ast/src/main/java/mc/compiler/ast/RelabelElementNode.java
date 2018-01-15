package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
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
@Data
@EqualsAndHashCode(callSuper = true)
public class RelabelElementNode extends ASTNode {

  /**
   * The new name for the transition after the application of the relabel.
   */
  private String newLabel;

  /**
   * The old name for the transition after the application of the relabel.
   */
  private String oldLabel;

  /**
   * The various processes upon which this applies (use is in indexed processes).
   */
  private RangesNode ranges;

  public RelabelElementNode(String newLabel, String oldLabel, Location location) {
    super(location);
    this.newLabel = newLabel;
    this.oldLabel = oldLabel;
    ranges = null;
  }

  public boolean hasRanges() {
    return ranges != null;
  }
}
