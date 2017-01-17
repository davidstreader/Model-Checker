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

}
