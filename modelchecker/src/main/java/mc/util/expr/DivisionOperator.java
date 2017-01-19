package mc.util.expr;

public class DivisionOperator extends BothOperator {

	public DivisionOperator(Expression lhs, Expression rhs){
		super(lhs, rhs);
	}

	public int evaluate(){
		return getLeftHandSide().evaluate() / getRightHandSide().evaluate();
	}

}
