package mc.util.expr;

public class RightShiftOperator extends Operator {

	public RightShiftOperator(Expression lhs, Expression rhs){
		super(lhs, rhs);
	}

	public int evaluate(){
		return getLeftHandSide().evaluate() >> getRightHandSide().evaluate();
	}

}
