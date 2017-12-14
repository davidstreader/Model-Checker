package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReferenceNode extends ASTNode {

    private String reference;

    public ReferenceNode(String reference, Location location){
        super(location);
        this.reference = reference;
    }
}
