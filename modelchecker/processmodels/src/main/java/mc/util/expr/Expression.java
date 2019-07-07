package mc.util.expr;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.microsoft.z3.*;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.util.Location;

/**
 * A class that is able to simplify expressions using Z3
 *   java class Expr  is a Z3  class
 * Makes use of Google cashing  see https://github.com/google/guava/wiki/CachesExplained
 *
 * to evaluate a Z3 expression using Cashing
 * 1 define a CashLoader with the expression and result
 * 2 call .get om the defined CashLoader that either gets the cashed result of runs the computation
 * 3 in the cash loader the key is of the input type (see below)
 *   3a set up the expression to be "solved" by Z3
 *   3b run the solve on the input
 */
public class Expression {
    /*
      Input  expresion "expr" + evaluation "variables"
      Will need to have typed variables  and Substitute to call the appropriate Z3 substitution
     */
    @Data
    @AllArgsConstructor
    private static class Substitute {
        Context ctx;
        Map<String,Integer> variables;
        Expr expr;
    }
    @Data
    @AllArgsConstructor
    private static class SubstituteReals {
        Context ctx;
        Map<String,Double> variables;
        Expr expr;
    }
    @Data
    @AllArgsConstructor
    private static class And {
        Context ctx;
        Expr expr1;
        Map<String,Integer> variables1;
        Expr expr2;
        Map<String,Integer> variables2;
    }
    @Data
    @AllArgsConstructor
    private static class myExp {
        Context ctx;
        Expr expr;
        Map<String,Integer> variables;

    }
    @Data
    @AllArgsConstructor
    private static class AndAll {
        Context ctx;
        List<myExp> andall;
    }
    /*
    LoadingCache
    equated.get(key, b ) will first use "key" to lookup any cashed result
    else will compute the result by executing "load", cash it and return it

    This computation takes two BoolExpr A, B and solves the conjunction A/\B
     */
    private static LoadingCache<And, Boolean> equated = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.SECONDS)
        .build(
            new CacheLoader<And, Boolean>() {
                public Boolean load(And key) throws InterruptedException, CompilationException {
                    BoolExpr expr = key.ctx.mkAnd((BoolExpr)substituteInts(key.expr1,key.variables1,key.ctx),
                                                  (BoolExpr)substituteInts(key.expr2,key.variables2,key.ctx));
                    return solve(expr,key.ctx);
                }
            });

    private static LoadingCache<AndAll, Boolean> andAll = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterWrite(1, TimeUnit.SECONDS)
      .build(
        new CacheLoader<AndAll, Boolean>() {
            public Boolean load(AndAll key) throws InterruptedException, CompilationException {
                boolean b =
                  solve((BoolExpr)substituteInts(key.andall.get(0).expr,key.andall.get(0).variables,key.ctx),key.ctx);
                if (key.andall.size() == 1) {
                    for (int i = 1; i < key.andall.size() - 1; i++) {
                        boolean nb = solve((BoolExpr) substituteInts(key.andall.get(i).expr, key.andall.get(i).variables, key.ctx), key.ctx);
                        b = b && nb;
                    }
                }
                return b;
            }
        });

/*
   apply the evaluation to the expression (both in Substitute)
   return  true iff "the expression is satisfiable"

   ? if the simplified BoolExpr isConst then true returns 1  and false 0
     if the term is not Const  then the simplified BoolExpr  is only true for some evaluations
   ?
 */
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
                    Solver solver = key.ctx.mkSolver();
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
                    for (Map.Entry<String,Integer> c : subMap.entrySet()) {
                        consts[i] = key.ctx.mkBVConst(c.getKey(),32);
                        replacements[i++] = key.ctx.mkBV(c.getValue(),32);
                    }
                    Expr t = expr.substitute(consts,replacements);
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException("Interrupted!");
                    }
                    return t;
                }
            });

    /*
      Below was HACKED from above turning Integer 2 Double  etc
     */
    private static LoadingCache<SubstituteReals, Expr> substitutionsReals = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.SECONDS)
            .build(
                    new CacheLoader<SubstituteReals, Expr>() {
                        public Expr load(SubstituteReals key) throws InterruptedException {
                            Map<String,Double> subMap = key.variables;
                            Expr expr = key.expr;
                            Expr[] consts = new Expr[subMap.size()];
                            Expr[] replacements = new Expr[subMap.size()];
                            int i =0;
                            for (Map.Entry<String,Double> c : subMap.entrySet()) {
                                consts[i] = key.ctx.mkBVConst(c.getKey(),32);
                                FPSort d_sort = key.ctx.mkFPSort(11, 53);
                                replacements[i++] = key.ctx.mkFP(c.getValue(),d_sort);
                            }
                            Expr t = expr.substitute(consts,replacements);
                            if (Thread.currentThread().isInterrupted()) {
                                throw new InterruptedException("Interrupted!");
                            }
                            return t;
                        }
                    });

    /**
     * Combine two guards together  -b1--a1->-b2--a2-> ==> -b-a->  b== b1/\ b2@a1
     *    Hoare Logic b2@a1 is the precondition of program a1 with post condition b2
     * @param first The first guard
     * @param second The second guard
     * @return A logical and of both guards, with the next variables substituted from the first into the second.
     *         b1/\ b2@a1
     * @throws CompilationException
     */
    public static Guard automataPreConditionHoareLogic(Guard first, Guard second, Context ctx)
      throws CompilationException, InterruptedException {
        //Create a new guard
        Guard ret = new Guard();
        //Start with variables from the second guard
        ret.setVariables(second.getVariables());
        //Replace all the variables from the second guard with ones from the first guard
        ret.getVariables().putAll(first.getVariables());
        ret.setNext(second.getNext());
        // next variables that exist in the first map that have not been edited by the second, add them.
        first.getNext().stream().filter(s -> !second.getNextMap().containsKey(s.split("\\W")[0])).forEach(s -> ret.getNext().add(s));
        //convert the next variables into a series of Z3 expressions.
        HashMap<String,Expr> subMap = new HashMap<>();
        for (String str: first.getNextMap().keySet()) {
            subMap.put(str,constructExpression(first.getNextMap().get(str),null, ctx));
        }
        if (second.getGuard() == null) {
            ret.setGuard(first.getGuard());
        } else {
            if (first.getGuard() == null) {
                ret.setGuard(second.getGuard());
            }else {
                BoolExpr secondGuard = second.getGuard();
                //Substitute every value from the subMap into the second guard.
                secondGuard = substitute(secondGuard,subMap,ctx);
                ret.setGuard(ctx.mkAnd(first.getGuard(), secondGuard));
            }
        }
        //System.out.println(first.myString()+" - "+second.myString()+ " -> "+ret.myString());
        return ret;
    }

    /**
     * On a PetriNet the guards and assignmnets are held on different Guards
     * Use above with Automata
     * Hoare Logic  precondition of   b1-a2->b3- ->
     *    Hoare Logic b? = b1/\ b3@a2 is the precondition of program a1 with post condition b2
     * @param first The first guard
     * @param second The second guard
     * @return A logical and of both guards, with the next variables substituted from the first into the second.
     *         b1/\ b3@a2
     * @throws CompilationException
     */
    public static Guard preConditionHoarLogic(Guard first, Guard second, Guard third,Context ctx)
      throws CompilationException, InterruptedException {
        //Create a new guard
        Guard ret = new Guard();
        //Start with variables from the second guard
        ret.setVariables(second.getVariables());
        //Replace all the variables from the second guard with ones from the first guard
        ret.getVariables().putAll(first.getVariables());
        ret.setNext(second.getNext());
        // next variables that exist in the first map that have not been edited by the second, add them.
        first.getNext().stream().filter(s -> !second.getNextMap().containsKey(s.split("\\W")[0])).forEach(s -> ret.getNext().add(s));
        //convert the next variables into a series of Z3 expressions.
        HashMap<String,Expr> subMap = new HashMap<>();
        for (String str: first.getNextMap().keySet()) {
            subMap.put(str,constructExpression(first.getNextMap().get(str),null, ctx));
        }
        if (third.getGuard() == null) {
            ret.setGuard(first.getGuard());
        } else {
            if (first.getGuard() == null) {
                ret.setGuard(third.getGuard());
            }else {
                BoolExpr thirdGuard = third.getGuard();
                //Substitute every value from the subMap into the second guard.
                thirdGuard = substitute(thirdGuard,subMap,ctx);
                ret.setGuard(ctx.mkAnd(first.getGuard(), thirdGuard));
            }
        }
        System.out.println(first.myString()+" - "+second.myString()+" - "+third.myString()+ " -> "+ret.myString());
        return ret;
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static Expr substituteInts(Expr expr, Map<String, Integer> subMap, Context ctx) {
        return substitutions.get(new Substitute(ctx,subMap, expr));
    }
    @SneakyThrows
    public static Expr substituteReals(Expr expr, Map<String, Double> subMap, Context ctx) {
       return substitutionsReals.get(new SubstituteReals(ctx,subMap, expr));
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T extends Expr> T substitute(T expr, Map<String, Expr> subMap, Context ctx) {
        if (subMap == null) return expr;

        Expr[] consts = new Expr[subMap.size()];
        Expr[] replacements = new Expr[subMap.size()];
        int i =0;
        for (Map.Entry<String,Expr> entry : subMap.entrySet()) {
            if(entry.getValue() == null)
                continue;
            consts[i] = ctx.mkBVConst(entry.getKey(),32);
            replacements[i++] = entry.getValue();
        }
        T t = (T) expr.substitute(consts,replacements);
        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Interrupted!");
        }
        return t;
    }
    @SneakyThrows
    public static boolean equate(Guard guard1, Guard guard2, Context ctx) {
        boolean b =
         equated.get(new And(ctx,guard1.getGuard(),guard1.getVariables(),guard2.getGuard(),guard2.getVariables()));
        System.out.println("equate "+guard1+" - "+guard2+" = "+b);
        return b;
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

    /*
       Called in parse and expander  so could have complexe format!
     */
    public static Expr constructExpression(String s, Location location, Context context) throws InterruptedException, CompilationException {
        return constructExpression(s, Collections.emptyMap(), location, context);
    }
    /**
     *
     * @param expression        The logical expression as a string to construct
     * @param variableMap
     * @param location
     * @param z3Context
     * @return  aZ3 expression
     * @throws InterruptedException
     * @throws CompilationException
     */
    private static Expr constructExpression(String expression, Map<String, String> variableMap,
                                            Location location, Context z3Context)
      throws InterruptedException, CompilationException {
        Pattern regex = Pattern.compile("(\\$v.+\\b)");
        Matcher matcher = regex.matcher(expression);
        while (matcher.find()) {
            if (!variableMap.containsKey(matcher.group(0))) {
                throw new CompilationException(Expression.class,"Unable to find variable: "+matcher.group(0),location);
            }
            expression = expression.replace(matcher.group(0),variableMap.get(matcher.group(0)));
            matcher = regex.matcher(expression);
        }

        /* OK not sure if this is worth the effort BUT.
              constant is reparsed converted into an AST using z#Context?
         */
        // parsing infixed maths to postfixed or AST -- Expr extends AST
        //System.out.println("expression "+expression+ " parsed into sYard");
        ShuntingYardAlgorithm sya = new ShuntingYardAlgorithm(z3Context);
        return sya.convert(expression, location);
    }



    static BitVecExpr mkBV(int i, Context ctx) throws InterruptedException {
        //  FPSort double_sort = ctx.mkFPSort(11, 53); ctx.mkFP()
        return ctx.mkBV(i,32);
    }

    static FPNum mkNum(Double d, Context ctx) throws InterruptedException {
        FPSort d_sort = ctx.mkFPSort(11, 53);
        return ctx.mkFP(d,d_sort);
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
