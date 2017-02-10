package mc.util.expr;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Expression implements Serializable {

    public static Expression constructExpression(String expression, Map<String,String> variableMap){
        Pattern regex = Pattern.compile("(\\$v.+\\b)");
        Matcher matcher = regex.matcher(expression);
        while (matcher.find()) {
            expression = expression.replace(matcher.group(0),variableMap.get(matcher.group(0)));
            matcher = regex.matcher(expression);
        }
        ShuntingYardAlgorithm sya = new ShuntingYardAlgorithm();
        return sya.convert(expression);
    }

    public static Expression constructExpression(String s) {
        return constructExpression(s, Collections.emptyMap());
    }
    @Override
    public String toString() {
        return new ExpressionPrinter().printExpression(this);
    }
}
