package mc.util.expr;

import com.microsoft.z3.*;
import mc.exceptions.CompilationException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by sheriddavi on 19/01/17.
 * Interpretor and Parsing  evaluate expressions in symbolic processes
 */
public class ExpressionEvaluator {

    public boolean isExecutable(Expr expression) throws CompilationException, InterruptedException {
        //If you simplify an expression with no variables it will be evaluated by the solver.
        Expr ex = expression.simplify();
        return ex instanceof BitVecNum;
    }

    public int evaluateIntExpression(Expr ex, Map<String, Integer> variableMap, Context context) throws CompilationException, InterruptedException {
        ex = Expression.substituteInts(ex, variableMap,context).simplify();
        if (ex.isTrue()) return 1;
        if (ex.isFalse()) return 0;
        if (ex instanceof BitVecNum) return evaluate((BitVecNum) ex,context);
         throw new CompilationException(getClass(),"There was an undefined variable in the statement: "+ExpressionPrinter.printExpression(ex));
    }

    public double evaluateRealExpression(Expr ex, Map<String, Double> variableMap, Context context)  throws CompilationException, InterruptedException {
        ex = Expression.substituteReals(ex, variableMap,context).simplify();
        System.out.println("Expression eval "+ex.toString());
        System.out.println(variableMap.entrySet().stream().map(x->x.getKey()+"->"+x.getValue()).collect(Collectors.joining()));
        if (ex.isTrue()) return 1;
        if (ex.isFalse()) return 0;
        if (ex instanceof BitVecNum) return evaluate((BitVecNum) ex,context);
        if (ex instanceof FPNum) return evaluate((FPNum) ex,context);
        throw new CompilationException(getClass(),"There was an undefined variable in the statement: "+ExpressionPrinter.printExpression(ex));
    }


    public static int evaluate(BitVecNum ex, Context context) throws InterruptedException {
        return ((IntNum) context.mkBV2Int(ex,true).simplify()).getInt();
    }
    public static double evaluate(FPNum ex, Context context) throws InterruptedException {
        double d = (double) ex.getSignificandUInt64();
        String s = String.format("%.2f",d);
        System.out.println("ev 1 "+s);
        System.out.println("ev 2 "+ex.getSExpr());
        System.out.println("ev 3 "+ex.getSignificand());
        return ex.getSignificandUInt64();
    }
}
