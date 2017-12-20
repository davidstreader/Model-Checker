package mc.util.expr;

import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntNum;
import java.util.Map;
import mc.exceptions.CompilationException;

/**
 * Created by sheriddavi on 19/01/17.
 */
public class ExpressionEvaluator {

    public boolean isExecutable(Expr expression) throws CompilationException, InterruptedException {
        //If you simplify an expression with no variables it will be evaluated by the solver.
        Expr ex = expression.simplify();
        return ex instanceof BitVecNum;
    }

    public int evaluateExpression(Expr ex, Map<String, Integer> variableMap, Context context) throws CompilationException, InterruptedException {
        ex = Expression.substituteInts(ex, variableMap,context).simplify();
        if (ex.isTrue()) return 1;
        if (ex.isFalse()) return 0;
        if (ex instanceof BitVecNum) return evaluate((BitVecNum) ex,context);
        throw new CompilationException(getClass(),"There was an undefined variable in the statement: "+ExpressionPrinter.printExpression(ex));
    }
    public static int evaluate(BitVecNum ex, Context context) throws InterruptedException {
        return ((IntNum) context.mkBV2Int(ex,true).simplify()).getInt();
    }
}
