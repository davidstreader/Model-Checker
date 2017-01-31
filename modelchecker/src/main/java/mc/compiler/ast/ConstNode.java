package mc.compiler.ast;

import mc.util.Location;

/**
 * Created by sheriddavi on 30/01/17.
 */
public class ConstNode extends ASTNode {

    private int value;

    public ConstNode(int value, Location location){
        super(location);
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof ConstNode){
            ConstNode node = (ConstNode)obj;
            return value == node.getValue();
        }

        return false;
    }
}
