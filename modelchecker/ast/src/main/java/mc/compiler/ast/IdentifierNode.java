package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class IdentifierNode extends ASTNode {

  private String identifier;

  public IdentifierNode(String identifier, Location location) {
    super(location);
    this.identifier = identifier;
  }
}
