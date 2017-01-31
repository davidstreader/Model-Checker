package mc.compiler.ast;

import mc.util.Location;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class ReferenceNode extends ASTNode {

    // fields
    private int reference;

    public ReferenceNode(int reference, Location location){
        super(location);
        this.reference = reference;
    }

    public int getReference(){
        return reference;
    }

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof ReferenceNode){
            ReferenceNode node = (ReferenceNode)obj;
            return reference == node.getReference();
        }

        return false;
    }
}
