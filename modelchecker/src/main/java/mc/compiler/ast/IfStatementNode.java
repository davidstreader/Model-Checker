package mc.compiler.ast;

import mc.util.Location;

public class IfStatementNode extends ASTNode {

	// fields
	private ASTNode condition;
	private ASTNode trueBranch;
	private ASTNode falseBranch;

	public IfStatementNode(ASTNode condition, ASTNode trueBranch, Location location){
		super(location);
		this.condition = condition;
		this.trueBranch = trueBranch;
		falseBranch = null;
	}

	public IfStatementNode(ASTNode condition, ASTNode trueBranch, ASTNode falseBranch, Location location){
		super(location);
		this.condition = condition;
		this.trueBranch = trueBranch;
		this.falseBranch = falseBranch;
	}

	public ASTNode getCondition(){
		return condition;
	}

	public ASTNode getTrueBranch(){
		return trueBranch;
	}

	public ASTNode getFalseBranch(){
		return falseBranch;
	}
}
