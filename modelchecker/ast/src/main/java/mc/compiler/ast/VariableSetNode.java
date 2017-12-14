package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
public class VariableSetNode extends ASTNode {

    private final Set<String> variables;

    public VariableSetNode(Set<String> variables, Location location){
        super(location);
        this.variables = variables;
    }
}
