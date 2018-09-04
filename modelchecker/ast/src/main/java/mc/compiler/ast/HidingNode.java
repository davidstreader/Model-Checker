package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * HidingNode stores the alphabet of the labels of the transitions to hide.
 * <p>
 * HidingNode appears the following gramatically {@code PROCESS("\"|"@")"{"(LABEL "," )* LABEL"}"}
 *
 * <p>
 * e.g. {@code A = (b->STOP)\{b}}
 *
 * @author Sanjay Govind
 * @author David Sheridan
 * @author Jacob Beal
 * @see SetNode
 * @see ProcessRootNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HidingNode extends ASTNode {

  /**
   * The type of hiding, the valid values of this are {@code includes} and {@code excludes}.
   */
  private String type;
  /**
   * The set of the alphabet being hidden.
   */
  private SetNode set;

  /**
   * Instantiates a new instance of HidingNode.
   *
   * @param type     the mode of hiding used,
   *                 valid inputs are {@code includes} and {@code excludes} {@link #type}
   * @param set      a set node containing the values of the labels to hide {@link #set}
   * @param location the location within the users code where this node is {@link ASTNode#location}
   */
  public HidingNode(String type, SetNode set, Location location) {
    super(location,"Hiding");
    this.type = type;
    this.set = set;
  }
}
