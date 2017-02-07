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
public class BooleanTests {

    // fields
    private ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private Map<String, Integer> variableMap = new HashMap<String, Integer>();

    @Test
    public void simpleComparatorTest_1() throws CompilationException {
        Expression expression = Expression.constructExpression("3 < 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '3 < 1' to equal 0 but received " + result);
        }
    }

    @Test
    public void simpleComparatorTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("3 <= 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '3 <= 1' to equal 0 but received " + result);
        }
    }

    @Test
    public void simpleComparatorTest_3() throws CompilationException{
        Expression expression = Expression.constructExpression("5 < 6");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '5 < 6' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleComparatorTest_4() throws CompilationException{
        Expression expression = Expression.constructExpression("5 <= 5");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '5 <= 5' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleComparatorTest_5() throws CompilationException{
        Expression expression = Expression.constructExpression("6 > 9");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '6 > 9' to equal 0 but received " + result);
        }
    }

    @Test
    public void simpleComparatorTest_6() throws CompilationException{
        Expression expression = Expression.constructExpression("7 > 6");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '7 > 6' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleComparatorTest_7() throws CompilationException{
        Expression expression = Expression.constructExpression("7 >= 7");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '7 >= 7' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleEquivalenceTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("1 != 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '1 != 1' to equal 0 but received " + result);
        }
    }

    @Test
    public void simpleEquivalenceTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("2 == 2");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '2 == 2' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleBooleanTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("10 != 10 || 10 == 10");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '10 != 10 || 10 == 10' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleBooleanTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("10 != 10 && 10 == 10");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 0){
            fail("Expecting the expression '10 != 10 && 10 == 10' to equal 0 but received " + result);
        }
    }
}
