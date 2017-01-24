package mc.util.expr;

import java.io.Serializable;

public abstract class Expression implements Serializable {

	public abstract int evaluate();

	public static Expression constructExpression(String expression){
		ShuntingYardAlgorithm sya = new ShuntingYardAlgorithm();
		return sya.convert(expression);
	}

}
