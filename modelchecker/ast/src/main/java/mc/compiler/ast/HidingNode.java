package mc.compiler.ast;

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
  public String myString(){
    return "Hidding "+type+" "+set.myString();
  }

  public String getType() {
    return this.type;
  }

  public SetNode getSet() {
    return this.set;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setSet(SetNode set) {
    this.set = set;
  }

  public String toString() {
    return "HidingNode(type=" + this.getType() + ", set=" + this.getSet() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof HidingNode)) return false;
    final HidingNode other = (HidingNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$type = this.getType();
    final Object other$type = other.getType();
    if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
    final Object this$set = this.getSet();
    final Object other$set = other.getSet();
    if (this$set == null ? other$set != null : !this$set.equals(other$set)) return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof HidingNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $type = this.getType();
    result = result * PRIME + ($type == null ? 43 : $type.hashCode());
    final Object $set = this.getSet();
    result = result * PRIME + ($set == null ? 43 : $set.hashCode());
    return result;
  }
}
