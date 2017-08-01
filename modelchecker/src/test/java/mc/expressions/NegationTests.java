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
public class NegationTests {

    // fields
    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private final Map<String, Integer> variableMap = new HashMap<>();

    @Test
    public void simpleNegateTest_1() throws CompilationException, InterruptedException {
        Expression expression = Expression.constructExpression("!$false");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!$false' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleNegateTest_2() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("!$true");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!$true' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_1() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("!($false)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!($false)' to equal 1 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_2() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("!($true)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!($true)' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_5() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("!($true || $true)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!($true || $true)' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_6() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("!($true && $true)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!($true && $true)' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_7() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("!($true && $false)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!($true && $false)' to equal 1 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_8() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("!($false || $false)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!($false || $false)' to equal 1 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_9() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("!!$false");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!!$false' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_10() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("!!$true");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!!$true' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleBitNegationTest_1() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("~1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != -2){
            fail("Expecting the expression '~1' to equal -2 but received " + result);
        }
    }

    @Test
    public void simpleBitNegationTest_2() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("~100");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != -101){
            fail("Expecting the expression '~100' to equal -101 but received " + result);
        }
    }

    @Test
    public void simpleBitNegationTest_3() throws CompilationException, InterruptedException{
        Expression expression = Expression.constructExpression("~0");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != -1){
            fail("Expecting the expression '~0' to equal -1 but received " + result);
        }
    }
}
