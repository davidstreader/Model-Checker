package mc.util.expr;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.microsoft.z3.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that is able to simplify expressions using Z3
 */
public class Expression {
    @Data
    @AllArgsConstructor
    private static class Substitute {
        Map<String,Integer> variables;
        Expr expr;
    }
    private static LoadingCache<Substitute, Expr> substitutions = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build(
            new CacheLoader<Substitute, Expr>() {
                public Expr load(Substitute key) throws InterruptedException {
                    Map<String,Integer> subMap = key.variables;
                    Expr expr = key.expr;
                    Expr[] consts = new Expr[subMap.size()];
                    Expr[] replacements = new Expr[subMap.size()];
                    int i =0;
                    for (String c : subMap.keySet()) {
                        consts[i] = getContext().mkBVConst(c,32);
                        replacements[i++] = getContext().mkBV(subMap.get(c),32);
                    }
                    Expr t = expr.substitute(consts,replacements);
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Interrupted!");
                    }
                    return t;
                }
            });
    /**
     * Combine two guards together
     * @param first The first guard
     * @param second The second guard
     * @return A logical and of both guards, with the next variables substituted from the first into the second.
     * @throws CompilationException
     */
    public static Guard combineGuards(Guard first, Guard second) throws CompilationException, InterruptedException {
        //Create a new guard
        Guard ret = new Guard();
        //Start with variables from the second guard
        ret.setVariables(second.getVariables());
        //Replace all the variables from the second guard with ones from the first guard
        ret.getVariables().putAll(first.getVariables());
        ret.setNext(second.getNext());
        //If there are next variables that exist in the first map that have not been edited by the second, add them.
        for (String s: first.getNext()) {
            if (!second.getNextMap().containsKey(s.split("\\W")[0]))
                ret.getNext().add(s);
        }
        //convert the next variables into a series of expressions.
        HashMap<String,Expr> subMap = new HashMap<>();
        for (String str: first.getNextMap().keySet()) {
            subMap.put(str,constructExpression(first.getNextMap().get(str)));
        }
        BoolExpr secondGuard = second.getGuard();
        //Substitute every value from the subMap into the second guard.
        secondGuard = substitute(secondGuard,subMap);
        ret.setGuard(getContext().mkAnd(first.getGuard(), secondGuard));
        return ret;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static Expr substituteInts(Expr expr, Map<String, Integer> subMap) {
        Expr[] consts = new Expr[subMap.size()];
        Expr[] replacements = new Expr[subMap.size()];
        int i =0;
        for (String c : subMap.keySet()) {
            consts[i] = getContext().mkBVConst(c,32);
            replacements[i++] = getContext().mkBV(subMap.get(c),32);
        }
        Expr t = expr.substitute(consts,replacements);
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Interrupted!");
        }
        return t;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T extends Expr> T substitute(T expr, Map<String, Expr> subMap) {
        if (subMap == null) return expr;
        Expr[] consts = new Expr[subMap.size()];
        Expr[] replacements = new Expr[subMap.size()];
        int i =0;
        for (String c : subMap.keySet()) {
            consts[i] = getContext().mkBVConst(c,32);
            replacements[i++] = subMap.get(c);
        }
        T t = (T) expr.substitute(consts,replacements);
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Interrupted!");
        }
        return t;
    }
    @SneakyThrows
    public static boolean equate(Guard guard1, Guard guard2) {
            BoolExpr expr = getContext().mkAnd(guard1.getResolved(),guard2.getResolved());
            return solve(expr);
    }
    @SneakyThrows
    public static boolean isSolvable(BoolExpr ex, Map<String, Integer> variables) {
        return solve((BoolExpr) substituteInts(ex,variables));
    }
    private static Context mkCtx() throws InterruptedException {
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("model", "true");
        return new Context(cfg);
    }
    private static boolean solve(BoolExpr expr) throws CompilationException, InterruptedException {
        BoolExpr simpl = (BoolExpr) expr.simplify();
        if (simpl.isConst()) {
            return simpl.getBoolValue().toInt()==1;
        }
        Solver solver = getContext().mkSolver();
        solver.add((BoolExpr) expr);
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }
        return solver.check() == Status.SATISFIABLE;
    }
    private static Map<Thread,Context> context = new HashMap<>();
    public static Context getContext() throws InterruptedException {
        if (context.get(Thread.currentThread()) == null) {
            context.put(Thread.currentThread(),mkCtx());
        }
        return context.get(Thread.currentThread());
    }
    public static Expr constructExpression(String expression, Map<String,String> variableMap) throws InterruptedException, CompilationException {
        java.util.regex.Pattern regex = Pattern.compile("(\\$v.+\\b)");
        Matcher matcher = regex.matcher(expression);
        while (matcher.find()) {
            expression = expression.replace(matcher.group(0),variableMap.get(matcher.group(0)));
            matcher = regex.matcher(expression);
        }
        ShuntingYardAlgorithm sya = new ShuntingYardAlgorithm();
        return sya.convert(expression);
    }
    public static Expr constructExpression(String s) throws InterruptedException, CompilationException {
        return constructExpression(s, Collections.emptyMap());
    }
    public static BitVecExpr mkBV(int i) throws InterruptedException {
        return getContext().mkBV(i,32);
    }
    public static void closeContext(Thread compileThread) {
        try {
            if (context.containsKey(compileThread)) {
                context.remove(compileThread).close();
            }
        } catch (Z3Exception ignored) {

        }
    }
}
