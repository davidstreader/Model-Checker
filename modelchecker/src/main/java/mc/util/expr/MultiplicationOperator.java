package mc.util.expr;

public class MultiplicationOperator extends BothOperator {

	public MultiplicationOperator(Expression lhs, Expression rhs){
		super(lhs, rhs);
	}

	public int evaluate(){
		return getLeftHandSide().evaluate() * getRightHandSide().evaluate();
	}
}
