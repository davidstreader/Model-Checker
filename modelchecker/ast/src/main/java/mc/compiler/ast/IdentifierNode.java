package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * Stores a reference to a constant, a process or a subprocess.
 * <p>
 * If this is a "LocalReference", or a self reference, this will later be changed in the
 * {@link mc.compiler.ReferenceReplacer}
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
   * Initialises a new instance of IdentifierNode.
   *
   * @param identifier the identifier of the process or constant. {@link #identifier}
   * @param location   the location within the users code, where this node is {@link ASTNode#location}
   */
  public IdentifierNode(String identifier, Location location) {
    super(location);
    this.identifier = identifier;
  }
}
