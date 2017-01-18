package mc.util.expr;

public abstract class Operator extends Expression {

	// fields
	private Expression lhs;
	private Expression rhs;

	public Operator(Expression lhs, Expression rhs){
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Expression getLeftHandSide(){
		return lhs;
	}

	public Expression getRightHandSide(){
		return rhs;
	}
}
