package mc.compiler.ast;

/**
 * Created by sheriddavi on 19/01/17.
 */
public class EmptyNode extends ASTNode {

    public EmptyNode(){
        super(null);
    }

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }

        return obj instanceof EmptyNode;
    }
}
