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
public class HideNode extends ASTNode {

  /**
   * The type of hiding, the valid values of this are {@code includes} and {@code excludes}.
   */
  private String type;
  /**
   * The set of the alphabet being hidden.
   */
  private SetNode set;
  private boolean obs = false;
  public void setObs(boolean b) {obs = b;}
  public boolean getObs() {return obs;}
  /**
   * Instantiates a new instance of HideNode.
   *
   * @param type     the mode of hiding used,
   *                 valid inputs are {@code includes} and {@code excludes} {@link #type}
   * @param set      a set node containing the values of the labels to hide {@link #set}
   * @param location the location within the users code where
   */
  public HideNode(String type, SetNode set, Location location) {
          super(location,"Hide");
    this.type = type;
    this.set = set;
  }
    public HideNode HideNode(String type, SetNode set, Location location, boolean o) {
        HideNode out =  new HideNode(type,set,location);
        out.setObs(o);
        return out;
    }
  public String myString(){
    return "Hide "+type+" "+set.myString();
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
    return "HideNode(type=" + this.getType() + ", set=" + this.getSet() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof HideNode)) return false;
    final HideNode other = (HideNode) o;
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
    return other instanceof HideNode;
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
