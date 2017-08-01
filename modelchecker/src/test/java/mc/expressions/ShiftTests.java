package mc.expressions;

import mc.exceptions.CompilationException;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 20/01/17.
 */
public class ShiftTests {

    // fields
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final Map<String, Integer> variableMap = new HashMap<>();

    @Test
    public void simpleLeftShiftTest_1() throws CompilationException, InterruptedException {
        Expression expression = Expression.constructExpression("2 << 3");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 16){
            fail("Expecting the expression '2 << 3' to equal 16 but received " + result);
        }
    }

    @Test
    public void simpleLeftShiftTest_2() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("3 << 2");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 12){
            fail("Expecting the expression '3 << 2' to equal 12 but received " + result);
        }
    }

    @Test
    public void simpleLeftShiftTest_3() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("2 << 3 << 4");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 256){
            fail("Expecting the expression '2 << 3 << 4' to equal 256 but received " + result);
        }
    }

    @Test
    public void parenLeftShiftTest_1() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("2 << (2 + 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 16){
            fail("Expecting the expression '2 << (2 + 1)' to equal 16 but received " + result);
        }
    }

    @Test
    public void parenLeftShiftTest_2() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("(3 - 1) << (2 + 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 16){
            fail("Expecting the expression '(3 - 1) << (2 + 1)' to equal 16 but received " + result);
        }
    }

    @Test
    public void mixedLeftShiftTest_1() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("3 - 1 << 2 + 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 16){
            fail("Expecting the expression '3 - 1 << 2 + 1' to equal 16 but received " + result);
        }
    }

    @Test
    public void simpleRightShiftTest_1() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("512 >> 3");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 64){
            fail("Expecting the expression '512 >> 3' to equal 64 but received " + result);
        }
    }

    @Test
    public void simpleRightShiftTest_2() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("512 >> 2");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 128){
            fail("Expecting the expression '512 >> 2' to equal 128 but received " + result);
        }
    }

    @Test
    public void simpleRightShiftTest_3() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("1024 >> 3 >> 2");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 32){
            fail("Expecting the expression '1024 >> 3 >> 2' to equal 32 but received " + result);
        }
    }

    @Test
    public void parenRightShiftTest_1() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("512 >> (2 + 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 64){
            fail("Expecting the expression '512 >> (2 + 1)' to equal 64 but received " + result);
        }
    }

    @Test
    public void parenRightShiftTest_2() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("(520 - 8) >> (2 + 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 64){
            fail("Expecting the expression '(520 - 8) >> (2 + 1)' to equal 64 but received " + result);
        }
    }

    @Test
    public void mixedRightShiftTest_1() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("520 - 8 >> 2 + 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 64){
            fail("Expecting the expression '520 - 8 >> 2 + 1' to equal 64 but received " + result);
        }
    }
}
