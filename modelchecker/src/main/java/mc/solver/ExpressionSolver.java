package mc.solver;

import com.microsoft.z3.Context;
import mc.Main;
import mc.compiler.Guard;
import mc.util.expr.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExpressionSolver {

    private static Logger logger = LoggerFactory.getLogger(ExpressionSolver.class);
    private static JavaSMTConverter converter;
    static {
        try {
            HashMap<String, String> cfg = new HashMap<>();
            cfg.put("model", "true");
            Context ctx = new Context(cfg);
            converter = new JavaSMTConverter(ctx);
        } catch (UnsatisfiedLinkError ex) {
            logger.error("Unable to load native libraries. Error: "+ex.getLocalizedMessage());
        }
    }
    public static Expression simplify(Expression ex) {
        return converter.simplify(ex, Collections.emptyMap());
    }

    public static Guard combineGuards(Guard hiddenGuard, Guard toGuard) {
        return converter.combineGuards(hiddenGuard,toGuard);
    }

    public static Expression simplify(Expression ex, Map<String, Integer> variables) {
        return converter.simplify(ex,variables);
    }
}
