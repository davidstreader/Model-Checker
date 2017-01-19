package mc.util.expr;

public class LeftShiftOperator extends BothOperator {

	public LeftShiftOperator(Expression lhs, Expression rhs){
		super(lhs, rhs);
	}

	public int evaluate(){
		return getLeftHandSide().evaluate() << getRightHandSide().evaluate();
	}
}
