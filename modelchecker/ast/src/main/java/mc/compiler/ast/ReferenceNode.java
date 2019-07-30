package mc.compiler.ast;

import mc.util.Location;

import java.util.ArrayList;
import java.util.List;
/**
 * The Expander  builds a Process node for each Global
 *  and places Localy defined processes in a list
 * The ReferenceNode refers to a location within the same process
 * (i.e. local processes or self-references)
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see IdentifierNode
 * @see ASTNode
 */
public class ReferenceNode extends ASTNode {

  /**
   * The string label of where this is referring to.
   */
  private String reference;

  private List<String> symbolicBits = new ArrayList<>();

  /**
   * Instantiate a new Reference Node.
   *
   * @param reference the label for where this node is referring to. {@link #reference}
   * @param location  where within the userscode where this node appears. {@link ASTNode#location}
   */
  public ReferenceNode(String reference, Location location) {
    super(location,"Reference");
    this.reference = reference;
  }
  @Override
  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("Ref_"+reference);
    sb.append(" sbits "+symbolicBits);
    return sb.toString();
  }

  public String getReference() {
    return this.reference;
  }

  public List<String> getSymbolicBits() {
    return this.symbolicBits;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public void setSymbolicBits(List<String> symbolicBits) {
    this.symbolicBits = symbolicBits;
  }

  public String toString() {
    return "ReferenceNode(reference=" + this.getReference() + ", symbolicBits=" + this.getSymbolicBits() + ")";
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ReferenceNode)) return false;
    final ReferenceNode other = (ReferenceNode) o;
    if (!other.canEqual((Object) this)) return false;
    if (!super.equals(o)) return false;
    final Object this$reference = this.getReference();
    final Object other$reference = other.getReference();
    if (this$reference == null ? other$reference != null : !this$reference.equals(other$reference)) return false;
    final Object this$symbolicBits = this.getSymbolicBits();
    final Object other$symbolicBits = other.getSymbolicBits();
    if (this$symbolicBits == null ? other$symbolicBits != null : !this$symbolicBits.equals(other$symbolicBits))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof ReferenceNode;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = super.hashCode();
    final Object $reference = this.getReference();
    result = result * PRIME + ($reference == null ? 43 : $reference.hashCode());
    final Object $symbolicBits = this.getSymbolicBits();
    result = result * PRIME + ($symbolicBits == null ? 43 : $symbolicBits.hashCode());
    return result;
  }
}
