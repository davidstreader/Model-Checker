package mc.util.expr;

public class LessThanOperator extends Operator {

	public LessThanOperator(Expression lhs, Expression rhs){
		super(lhs, rhs);
	}

	public int evaluate(){
		int lhs = getLeftHandSide().evaluate();
		int rhs = getRightHandSide().evaluate();
		boolean result = lhs < rhs;

		if(result){
			return 1;
		}

		return 0;
	}

}