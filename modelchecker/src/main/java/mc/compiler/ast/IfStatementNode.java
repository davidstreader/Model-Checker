package mc.compiler.ast;

import mc.solver.JavaSMTConverter;
import mc.util.Location;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionPrinter;

import java.util.Collections;

public class IfStatementNode extends ASTNode {

	// fields
	private Expression condition;
	private ASTNode trueBranch;
	private ASTNode falseBranch;

	public IfStatementNode(Expression condition, ASTNode trueBranch, Location location){
		super(location);
		this.condition = condition;
		this.trueBranch = trueBranch;
		falseBranch = null;
	}

	public IfStatementNode(Expression condition, ASTNode trueBranch, ASTNode falseBranch, Location location){
		super(location);
		this.condition = condition;
		this.trueBranch = trueBranch;
		this.falseBranch = falseBranch;
	}

	public Expression getCondition(){
		return condition;
	}

	public void setCondition(Expression condition){
		this.condition = condition;
	}

	public ASTNode getTrueBranch(){
		return trueBranch;
	}

	public void setTrueBranch(ASTNode trueBranch){
		this.trueBranch = trueBranch;
	}

	public ASTNode getFalseBranch(){
		return falseBranch;
	}

	public void setFalseBranch(ASTNode falseBranch){
		this.falseBranch = falseBranch;
	}

	public boolean hasFalseBranch(){ return falseBranch != null; }
}
