package mc.util.expr.tests;

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
    private ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private Map<String, Integer> variableMap = new HashMap<String, Integer>();

    @Test
    public void simpleNegateTest_1(){
        Expression expression = Expression.constructExpression("!0");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!0' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleNegateTest_2(){
        Expression expression = Expression.constructExpression("!1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!1' to equal 0 but received " + result);
        }
    }

    @Test
    public void simpleNegateTest_3(){
        Expression expression = Expression.constructExpression("!100");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!100' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_1(){
        Expression expression = Expression.constructExpression("!(0)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!(0)' to equal 1 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_2(){
        Expression expression = Expression.constructExpression("!(1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!(1)' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_3(){
        Expression expression = Expression.constructExpression("!(100)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!(100)' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_4(){
        Expression expression = Expression.constructExpression("!(1 - 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!(1 - 1)' to equal 1 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_5(){
        Expression expression = Expression.constructExpression("!(1 || 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!(1 || 1)' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_6(){
        Expression expression = Expression.constructExpression("!(1 && 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!(1 && 1)' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_7(){
        Expression expression = Expression.constructExpression("!(1 && 0)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!(1 && 0)' to equal 1 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_8(){
        Expression expression = Expression.constructExpression("!(0 || 0)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!(0 || 0)' to equal 1 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_9(){
        Expression expression = Expression.constructExpression("!!0");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '!!0' to equal 0 but received " + result);
        }
    }

    @Test
    public void parenNegateTest_10(){
        Expression expression = Expression.constructExpression("!!1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '!!1' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleBitNegationTest_1(){
        Expression expression = Expression.constructExpression("~1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != -2){
            fail("Expecting the expression '~1' to equal -2 but received " + result);
        }
    }

    @Test
    public void simpleBitNegationTest_2(){
        Expression expression = Expression.constructExpression("~100");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != -101){
            fail("Expecting the expression '~100' to equal -101 but received " + result);
        }
    }

    @Test
    public void simpleBitNegationTest_3(){
        Expression expression = Expression.constructExpression("~0");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != -1){
            fail("Expecting the expression '~0' to equal -1 but received " + result);
        }
    }
}
