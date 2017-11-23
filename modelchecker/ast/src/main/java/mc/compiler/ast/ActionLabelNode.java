package mc.compiler.ast;

import lombok.ToString;
import mc.util.Location;
@ToString
public class ActionLabelNode extends ASTNode {

	// fields
	private String action;

	public ActionLabelNode(String action, Location location){
		super(location);
		this.action = action;
	}

	public String getAction(){
		return action;
	}

	public void setAction(String action){
		this.action = action;
	}

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj instanceof ActionLabelNode){
            ActionLabelNode node = (ActionLabelNode)obj;
            return action.equals(node.getAction());
        }

        return false;
    }

}
