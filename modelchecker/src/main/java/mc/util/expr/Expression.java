package mc.util.expr;

import mc.exceptions.CompilationException;

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
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Expression)) return false;
        try {
            Expression simple =  ExpressionSimplifier.simplify(new EqualityOperator(this, (Expression) o),Collections.emptyMap());
            if (simple instanceof BooleanOperand && ((BooleanOperand) simple).getValue()) return true;
        } catch (CompilationException ignored) {}
        return false;
    }
    public Expression copy() throws CompilationException {
        return ExpressionSimplifier.copy(this);
    }
}
