package mc.util.expr;

import mc.webserver.LogMessage;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Expression implements Serializable {

	public abstract int evaluate();

	public static Expression constructExpression(String expression, Map<String,String> variableMap){
    Pattern regex = Pattern.compile("(\\$v\\w)");
    Matcher matcher = regex.matcher(expression);
    while (matcher.find()) {
      expression = expression.replace(matcher.group(0),variableMap.get(matcher.group(0)));
      matcher = regex.matcher(expression);
    }
		ShuntingYardAlgorithm sya = new ShuntingYardAlgorithm();
		return sya.convert(expression);
	}

}
