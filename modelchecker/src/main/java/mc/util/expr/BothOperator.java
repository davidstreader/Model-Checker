package mc.util.expr;

import lombok.Setter;

public abstract class BothOperator extends Operator {

	// fields
    @Setter
	private Expression lhs;
	@Setter
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
