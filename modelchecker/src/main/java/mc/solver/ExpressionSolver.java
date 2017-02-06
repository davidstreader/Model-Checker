package mc.solver;

import lombok.SneakyThrows;
import mc.compiler.Guard;
import mc.util.expr.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

public class ExpressionSolver {
    private static Logger logger = LoggerFactory.getLogger(ExpressionSolver.class);
    public static Expression simplify(Expression ex) {
        return simplify(ex, Collections.emptyMap());
    }
    @SneakyThrows
    public static Guard combineGuards(Guard hiddenGuard, Guard toGuard) {
        return EvaluationSimplifier.combineGuards(hiddenGuard,toGuard);
    }

    @SneakyThrows
    public static Expression simplify(Expression ex, Map<String, Integer> variables) {
        return EvaluationSimplifier.simplify(ex,variables);
    }
}
