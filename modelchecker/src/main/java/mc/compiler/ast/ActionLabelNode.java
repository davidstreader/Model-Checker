package mc.compiler.ast;

import mc.util.Location;

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
        if(obj == null){
            return false;
        }
        if(obj instanceof ActionLabelNode){
            ActionLabelNode node = (ActionLabelNode)obj;
            return action.equals(node.getAction());
        }

        return false;
    }

}
