package mc.compiler.ast;

/**
 * Created by sheriddavi on 19/01/17.
 */
public class EmptyNode extends ASTNode {

    public EmptyNode(){
        super(null);
    }

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }

        return obj instanceof EmptyNode;
    }
}
