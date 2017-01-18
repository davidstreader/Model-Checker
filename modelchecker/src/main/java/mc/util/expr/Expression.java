package mc.util.expr;

public abstract class Expression {

	public abstract int evaluate();

	public static Expression constructExpression(String expression){
		ShuntingYardAlgorithm sya = new ShuntingYardAlgorithm();
		return sya.convert(expression);
	}
}