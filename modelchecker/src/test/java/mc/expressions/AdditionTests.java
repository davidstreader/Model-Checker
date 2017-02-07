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
public class AdditionTests {

    // fields
    private ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private Map<String, Integer> variableMap = new HashMap<String, Integer>();

    @Test
    public void singleOperandTest() throws CompilationException {
        Expression expression = Expression.constructExpression("1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '1' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleAdditionTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("1 + 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 2){
            fail("Expecting the expression '1 + 1' to equal 2 but received " + result);
        }
    }

    @Test
    public void simpleAdditionTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("3 + 2 + 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 6){
            fail("Expecting the expression '3 + 2 + 1' to equal 6 but received " + result);
        }
    }

    @Test
    public void parenAdditionTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("3 + (2 + 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 6){
            fail("Expecting the expression '3 + (2 + 1)' to equal 6 but received " + result);
        }
    }

    @Test
    public void parenAdditionTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("(3 + 2) + 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 6){
            fail("Expecting the expression '(3 + 2) + 1' to equal 6 but received " + result);
        }
    }

    @Test
    public void parenAdditionTest_3() throws CompilationException{
        Expression expression = Expression.constructExpression("(3 + 2 + 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 6){
            fail("Expecting the expression '(3 + 2 + 1)' to equal 6 but received " + result);
        }
    }

    @Test
    public void parenAdditionTest_4() throws CompilationException{
        Expression expression = Expression.constructExpression("((3 + 2 + 1))");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 6){
            fail("Expecting the expression '((3 + 2 + 1))' to equal 6 but received " + result);
        }
    }

    @Test
    public void simpleSubtractionTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("3 - 2");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '3 - 2' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleSubtrctionTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("5 - 2 - 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 2){
            fail("Expecting the expression '5 - 2 - 1' to equal 2 but received " + result);
        }
    }

    @Test
    public void parenSubtractionTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("5 - (2 - 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 4){
            fail("Expecting the expression '5 - (2 - 1)' to equal 4 but received " + result);
        }
    }

    @Test
    public void parenSubtractionTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("(5 - 2) - 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 2){
            fail("Expecting the expression '(5 - 2) - 1' to equal 2 but received " + result);
        }
    }

    @Test
    public void parenSubtractionTest_3() throws CompilationException{
        Expression expression = Expression.constructExpression("(5 - 2 - 1)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 2){
            fail("Expecting the expression '(5 - 2 - 1)' to equal 2 but received " + result);
        }
    }

    @Test
    public void parenSubtractionTest_4() throws CompilationException{
        Expression expression = Expression.constructExpression("((5 - 2 - 1))");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 2){
            fail("Expecting the expression '((5 - 2 - 1))' to equal 2 but received " + result);
        }
    }

    @Test
    public void simpleMixedTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("10 + 2 - 7");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 5){
            fail("Expecting the expression '10 + 2 - 7' to equal 5 but received " + result);
        }
    }

    @Test
    public void simpleMixedTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("10 - 2 + 7");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 15){
            fail("Expecting the expression '10 - 2 + 7' to equal 15 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("(10 + 2) - 7");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 5){
            fail("Expecting the expression '(10 + 2) - 7' to equal 5 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("(10 - 2) + 7");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 15){
            fail("Expecting the expression '(10 - 2) + 7' to equal 15 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_3() throws CompilationException{
        Expression expression = Expression.constructExpression("10 + (2 - 7)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 5){
            fail("Expecting the expression '10 + (2 - 7)' to equal 5 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_4() throws CompilationException{
        Expression expression = Expression.constructExpression("10 - (2 + 7)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '10 - (2 + 7)' to equal 1 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_5() throws CompilationException{
        Expression expression = Expression.constructExpression("(10 + 2 - 7)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 5){
            fail("Expecting the expression '(10 + 2 - 7)' to equal 5 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_6() throws CompilationException{
        Expression expression = Expression.constructExpression("(10 - 2 + 7)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 15){
            fail("Expecting the expression '(10 - 2 + 7)' to equal 15 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_7() throws CompilationException{
        Expression expression = Expression.constructExpression("(10 + 5) - (3 + 2)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 10){
            fail("Expecting the expression '(10 + 5) - (3 + 2)' to equal 10 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_8() throws CompilationException{
        Expression expression = Expression.constructExpression("(10 + (5 - 3) + 2)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 14){
            fail("Expecting the expression '(10 + (5 - 3) + 2)' to equal 14 but received " + result);
        }
    }
}
