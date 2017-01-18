package mc.compiler.ast;

import mc.util.Location;

public class SequenceNode extends ASTNode {

	// fields
	private ActionLabelNode from;
	private ASTNode to;

	public SequenceNode(ActionLabelNode from, ASTNode to, Location location){
		super(location);
		this.from = from;
		this.to = to;
	}

	public ActionLabelNode getFrom(){
		return from;
	}

	public void setFrom(ActionLabelNode from){
		this.from = from;
	}

	public ASTNode getTo(){
		return to;
	}

	public void setTo(ASTNode to){
		this.to = to;
	}

}
