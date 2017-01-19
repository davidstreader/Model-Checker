package mc.util.expr;

public abstract class BothOperator extends Operator {

	// fields
	private Expression lhs;
	private Expression rhs;

	public BothOperator(Expression lhs, Expression rhs){
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
