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
import mc.util.Location;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
        Context thread;
        Map<String,Integer> variables;
        Expr expr;
    }
    @Data
    @AllArgsConstructor
    private static class And {
        Context thread;
        Expr expr1;
        Map<String,Integer> variables1;
        Expr expr2;
        Map<String,Integer> variables2;
    }
    private static LoadingCache<And, Boolean> equated = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.SECONDS)
        .build(
            new CacheLoader<And, Boolean>() {
                public Boolean load(And key) throws InterruptedException, CompilationException {
                    BoolExpr expr = key.thread.mkAnd((BoolExpr)substituteInts(key.expr1,key.variables1,key.thread),(BoolExpr)substituteInts(key.expr2,key.variables2,key.thread));
                    return solve(expr,key.thread);
                }
            });
    private static LoadingCache<Substitute, Boolean> solved = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.SECONDS)
        .build(
            new CacheLoader<Substitute, Boolean>() {
                public Boolean load(Substitute key) throws InterruptedException {
                    BoolExpr simpl = (BoolExpr) key.expr.simplify();
                    if (simpl.isConst()) {
                        return simpl.getBoolValue().toInt()==1;
                    }
                    Solver solver = key.thread.mkSolver();
                    solver.add((BoolExpr) key.expr);
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    return solver.check() == Status.SATISFIABLE;
                }
            });
    private static LoadingCache<Substitute, Expr> substitutions = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.SECONDS)
        .build(
            new CacheLoader<Substitute, Expr>() {
                public Expr load(Substitute key) throws InterruptedException {
                    Map<String,Integer> subMap = key.variables;
                    Expr expr = key.expr;
                    Expr[] consts = new Expr[subMap.size()];
                    Expr[] replacements = new Expr[subMap.size()];
                    int i =0;
                    for (String c : subMap.keySet()) {
                        consts[i] = key.thread.mkBVConst(c,32);
                        replacements[i++] = key.thread.mkBV(subMap.get(c),32);
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
    public static Guard combineGuards(Guard first, Guard second, Context ctx) throws CompilationException, InterruptedException {
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
            subMap.put(str,constructExpression(first.getNextMap().get(str),null, ctx));
        }
        BoolExpr secondGuard = second.getGuard();
        //Substitute every value from the subMap into the second guard.
        secondGuard = substitute(secondGuard,subMap,ctx);
        ret.setGuard(ctx.mkAnd(first.getGuard(), secondGuard));
        return ret;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static Expr substituteInts(Expr expr, Map<String, Integer> subMap, Context ctx) {
        return substitutions.get(new Substitute(ctx,subMap, expr));
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T extends Expr> T substitute(T expr, Map<String, Expr> subMap, Context ctx) {
        if (subMap == null) return expr;
        Expr[] consts = new Expr[subMap.size()];
        Expr[] replacements = new Expr[subMap.size()];
        int i =0;
        for (String c : subMap.keySet()) {
            consts[i] = ctx.mkBVConst(c,32);
            replacements[i++] = subMap.get(c);
        }
        T t = (T) expr.substitute(consts,replacements);
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Interrupted!");
        }
        return t;
    }
    @SneakyThrows
    public static boolean equate(Guard guard1, Guard guard2, Context ctx) {
        return equated.get(new And(ctx,guard1.getGuard(),guard1.getVariables(),guard2.getGuard(),guard2.getVariables()));
    }
    @SneakyThrows
    public static boolean isSolvable(BoolExpr ex, Map<String, Integer> variables, Context ctx) {
        return solve((BoolExpr) substituteInts(ex,variables, ctx),ctx);
    }
    public static Context mkCtx() throws InterruptedException {
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("model", "true");
        return new Context(cfg);
    }
    private static boolean solve(BoolExpr expr, Context ctx) throws CompilationException, InterruptedException {
        try {
            return solved.get(new Substitute(ctx,Collections.emptyMap(),expr));
        } catch (ExecutionException e) {
            throw new CompilationException(Expression.class,"Error occurred while solving: "+ExpressionPrinter.printExpression(expr));
        }
    }
    private static Expr constructExpression(String expression, Map<String, String> variableMap, Location location, Context context) throws InterruptedException, CompilationException {
        java.util.regex.Pattern regex = Pattern.compile("(\\$v.+\\b)");
        Matcher matcher = regex.matcher(expression);
        while (matcher.find()) {
            if (!variableMap.containsKey(matcher.group(0))) {
                throw new CompilationException(Expression.class,"Unable to find variable: "+matcher.group(0),location);
            }
            expression = expression.replace(matcher.group(0),variableMap.get(matcher.group(0)));
            matcher = regex.matcher(expression);
        }
        ShuntingYardAlgorithm sya = new ShuntingYardAlgorithm(context);
        return sya.convert(expression, location);
    }
    public static Expr constructExpression(String s, Location location, Context context) throws InterruptedException, CompilationException {
        return constructExpression(s, Collections.emptyMap(), location, context);
    }
    static BitVecExpr mkBV(int i, Context ctx) throws InterruptedException {
        return ctx.mkBV(i,32);
    }
    private static Field m_ctx;
    static {
        try {
            m_ctx = Z3Object.class.getDeclaredField("m_ctx");
            m_ctx.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
    public static Context getContextFrom(Z3Object object) {
        try {
            return (Context) m_ctx.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error creating context!");
    }
}
