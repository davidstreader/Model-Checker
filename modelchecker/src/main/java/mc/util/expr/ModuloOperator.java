package mc.util.expr;

public class ModuloOperator extends BothOperator {

	public ModuloOperator(Expression lhs, Expression rhs){
		super(lhs, rhs);
	}

	public int evaluate(){
		return getLeftHandSide().evaluate() % getRightHandSide().evaluate();
	}
}
