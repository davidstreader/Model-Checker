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
}
