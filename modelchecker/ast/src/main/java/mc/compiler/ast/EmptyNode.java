package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmptyNode extends ASTNode {

    public EmptyNode(){
        super(null);
    }
}
