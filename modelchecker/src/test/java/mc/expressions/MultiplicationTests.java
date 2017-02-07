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
public class MultiplicationTests {

    // fields
    private ExpressionEvaluator evaluator = new ExpressionEvaluator();
    private Map<String, Integer> variableMap = new HashMap<String, Integer>();

    @Test
    public void simpleMultiplicationTest_1() throws CompilationException {
        Expression expression = Expression.constructExpression("1 * 1");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '1 * 1' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleMultiplicationTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("2 * 3");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 6){
            fail("Expecting the expression '2 * 3' to equal 6 but received " + result);
        }
    }

    @Test
    public void simpleMultiplicationTest_3() throws CompilationException{
        Expression expression = Expression.constructExpression("4 * 5 * 6");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 120){
            fail("Expecting the expression '4 * 5 * 6' to equal 120 but received " + result);
        }
    }

    @Test
    public void parenMultiplicationTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("4 * (5 * 6)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 120){
            fail("Expecting the expression '4 * (5 * 6)' to equal 120 but received " + result);
        }
    }

    @Test
    public void parenMultiplicationTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("(4 * 5) * 6");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 120){
            fail("Expecting the expression '(4 * 5) * 6' to equal 120 but received " + result);
        }
    }

    @Test
    public void parenMultiplicationTest_3() throws CompilationException{
        Expression expression = Expression.constructExpression("(4 * 5 * 6)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 120){
            fail("Expecting the expression '(4 * 5 * 6)' to equal 120 but received " + result);
        }
    }

    @Test
    public void parenMultiplicationTest_4() throws CompilationException{
        Expression expression = Expression.constructExpression("((4 * 5 * 6))");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 120){
            fail("Expecting the expression '((4 * 5 * 6))' to equal 120 but received " + result);
        }
    }

    @Test
    public void simpleDivisionTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("3 / 2");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 1){
            fail("Expecting the expression '3 / 2' to equal 1 but received " + result);
        }
    }

    @Test
    public void simpleMixedTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("4 / 2 * 8 / 4");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 4){
            fail("Expecting the expression '4 / 2 * 8 / 4' to equal 4 but received " + result);
        }
    }

    @Test
    public void simpleMixedTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("4 * 2 / 2 * 2");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 8){
            fail("Expecting the expression '4 * 2 / 2 * 2' to equal 8 but received " + result);
        }
    }

    @Test
    public void simpleMixedTest_3() throws CompilationException{
        Expression expression = Expression.constructExpression("4 + 2 * 8 + 6");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 26){
            fail("Expecting the expression '4 + 2 * 8 + 6' to equal 26 but received " + result);
        }
    }

    @Test
    public void simpleMixedTest_4() throws CompilationException{
        Expression expression = Expression.constructExpression("4 - 2 * 8 - 6");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != -18){
            fail("Expecting the expression '4 - 2 * 8 - 6' to equal -18 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_1() throws CompilationException{
        Expression expression = Expression.constructExpression("(4 * 2) / (2 * 2)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 2){
            fail("Expecting the expression '(4 * 2) / (2 * 2)' to equal 2 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_2() throws CompilationException{
        Expression expression = Expression.constructExpression("(4 + 2) * (8 + 6)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 84){
            fail("Expecting the expression '(4 + 2) * (8 + 6)' to equal 84 but received " + result);
        }
    }

    @Test
    public void parenMixedTest_3() throws CompilationException{
        Expression expression = Expression.constructExpression("(4 - 2) * (8 - 6)");
        int result = evaluator.evaluateExpression(expression, variableMap);

        // check that the correct result was received
        if(result != 4){
            fail("Expecting the expression '(4 - 2) * (8 - 6)' to equal 4 but received " + result);
        }
    }
}
