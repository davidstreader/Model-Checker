package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

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
@Data
@EqualsAndHashCode(callSuper = true)
public class ReferenceNode extends ASTNode {

  /**
   * The string label of where this is referring to.
   */
  private String reference;

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
    return sb.toString();
  }
}
