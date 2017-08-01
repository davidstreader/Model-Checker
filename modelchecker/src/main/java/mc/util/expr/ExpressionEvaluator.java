package mc.util.expr;

import mc.exceptions.CompilationException;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sheriddavi on 19/01/17.
 */
public class ExpressionEvaluator {

    public boolean isExecutable(Expression expression) throws CompilationException, InterruptedException {
        //If you simplify an expression with no variables it will be evaluated by the solver.
        Expression ex = ExpressionSimplifier.simplify(expression, Collections.emptyMap());
        return ex instanceof BooleanOperand || ex instanceof IntegerOperand ;
    }

    public int evaluateExpression(Expression ex, Map<String, Integer> variableMap) throws CompilationException, InterruptedException {
        ex = ExpressionSimplifier.simplify(ex, variableMap);
        if (ex instanceof BooleanOperand) return ((BooleanOperand) ex).getValue()?1:0;
        if (ex instanceof IntegerOperand) return ((IntegerOperand) ex).getValue();
        throw new CompilationException(getClass(),"There was an undefined variable in that statement.");
    }
}
