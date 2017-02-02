package mc.solver;

import com.microsoft.z3.Context;
import mc.compiler.Guard;
import mc.util.expr.Expression;

import java.util.HashMap;

public class ExpressionSolver {
    private static JavaSMTConverter converter;
    static {
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("model", "true");
        Context ctx = new Context(cfg);
        converter = new JavaSMTConverter(ctx);
    }
    public static Expression simplify(Expression ex) {
        return converter.simplify(ex);
    }

    public static Guard combineGuards(Guard hiddenGuard, Guard toGuard) {
        return converter.combineGuards(hiddenGuard,toGuard);
    }

}
