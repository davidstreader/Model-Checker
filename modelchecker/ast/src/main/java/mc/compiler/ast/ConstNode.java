package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConstNode extends ASTNode {

    private int value;

    public ConstNode(int value, Location location){
        super(location);
        this.value = value;
    }
}
