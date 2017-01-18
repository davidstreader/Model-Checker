package mc.compiler.ast;

import mc.util.Location;

public class InterruptNode extends ASTNode {

	// fields
	private ActionLabelNode action;
	private ASTNode process;

	public InterruptNode(ActionLabelNode action, ASTNode process, Location location){
		super(location);
		this.action = action;
		this.process = process;
	}

	public ActionLabelNode getAction(){
		return action;
	}

	public void setAction(ActionLabelNode action){
		this.action = action;
	}

	public ASTNode getProcess(){
		return process;
	}

	public void setProcess(ASTNode process){
		this.process = process;
	}
}
