package mc.util.expr;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.Expr;
import com.microsoft.z3.IntNum;
import mc.exceptions.CompilationException;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sheriddavi on 19/01/17.
 */
public class ExpressionEvaluator {

    public boolean isExecutable(Expr expression) throws CompilationException, InterruptedException {
        //If you simplify an expression with no variables it will be evaluated by the solver.
        Expr ex = expression.simplify();
        return ex instanceof BitVecNum;
    }

    public int evaluateExpression(Expr ex, Map<String, Integer> variableMap) throws CompilationException, InterruptedException {
        ex = ExpressionSimplifier.substituteInts(ex, variableMap).simplify();
        if (ex.isTrue()) return 1;
        if (ex.isFalse()) return 0;
        if (ex instanceof BitVecNum) return ((IntNum)ExpressionSimplifier.getContext().mkBV2Int(((BitVecNum) ex),true).simplify()).getInt();
        throw new CompilationException(getClass(),"There was an undefined variable in the statement: "+ExpressionPrinter.printExpression(ex));
    }
}
