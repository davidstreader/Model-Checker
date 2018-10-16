package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;


/**
 * Stores a reference to a constant, a process or a subprocess.
 * <p>
 * If this is a "LocalReference", or a self reference, this will later be changed in the
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @see ReferenceNode
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IdentifierNode extends ASTNode {

  /**
   * The name of the process or constant.
   * Could be "C[$i][j+1][4]"  and may be parsed in Guard.java
   */
  private String identifier;

  /**
   * All identifers must be globally unique and domain specific
   */
  private String domain = "*";

  /**
   * Initialises a new instance of IdentifierNode.
   *
   * @param identifier the identifier of the process or constant. {@link #identifier}
   * @param location   the location within the users code, where this node is {@link ASTNode#location}
   */
  public IdentifierNode(String identifier, String domain, Location location) {
    super(location, "Identifier");
    this.identifier = identifier;
    this.domain = domain;
  }

  public String myString() {
    if (identifier.equals("*"))
      return identifier;
    else
      return identifier + ":" + domain;
  }

  public String getVarDom() {
    return identifier + ":" + domain;
  }

  public IdentifierNode instantiate(String from, String to) {
    //System.out.println("Instant Id "+this.identifier+" ^ " + this.domain+ " from "+from+" to "+to);
    if (from.contains(":")) {
      String parts[] = from.split(":");
      String fromVar = parts[0];
      String toparts[] = to.split(":");
      String toVar = parts[0];
      String toDom = parts[1];
      //System.out.println("fromVar "+fromVar+"  =? "+ this.getIdentifier());
      if (this.getIdentifier().equals(fromVar)) {
        IdentifierNode n = new IdentifierNode(to, toDom, getLocation());
        //System.out.println("Instant Id "+n.myString()+" from "+from+" to "+to);
        return n;
      }
      else {
        //System.out.println("fromVar "+fromVar+"  != "+ this.getIdentifier());
        return this;
      }
    } else {
      System.out.println("ERROR instainting variable with NO domain! " + from);
      return this;
    }
  }
}
