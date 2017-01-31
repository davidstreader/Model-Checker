package mc.compiler.ast;

import mc.util.Location;

import java.util.Set;

/**
 * Created by sheriddavi on 31/01/17.
 */
public class VariableSetNode extends ASTNode {

    private Set<String> variables;

    public VariableSetNode(Set<String> variables, Location location){
        super(location);
        this.variables = variables;
    }

    public Set<String> getVariables(){
        return variables;
    }

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof VariableSetNode){
            VariableSetNode node = (VariableSetNode)obj;
            return variables.equals(node.getVariables());
        }

        return false;
    }
}
