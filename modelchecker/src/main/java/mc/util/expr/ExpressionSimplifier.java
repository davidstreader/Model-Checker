package mc.util.expr;

import com.microsoft.z3.*;
import com.microsoft.z3.enumerations.Z3_lbool;
import lombok.SneakyThrows;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A class that is able to simplify expressions using Z3
 */
public class ExpressionSimplifier {
    private static ThreadLocal<ExpressionSimplifier> simplifier = new ThreadLocal<>();
    private ExpressionSimplifier(Context o) {
        context = o;
    }
    private boolean isSolvable1(Expression expr, Map<String, Integer> variables) throws CompilationException {
        Expr test = convert(expr, variables).simplify();
        if (test instanceof BoolExpr) {
            BoolExpr testB = (BoolExpr) test;
            if (testB.isConst()) {
                return testB.getBoolValue().toInt()==1;
            }
            Solver solver = context.mkSolver();
            solver.add(testB);
            return solver.check() == Status.SATISFIABLE;
        }
        throw new CompilationException(ExpressionSimplifier.class,
            "Unable to check if equation is satisfied as it was not a boolean expression.");
    }
    @SneakyThrows
    public static boolean isSolvable(Expression expr, Map<String, Integer> variables) {
        init();
        return simplifier.get().isSolvable1(expr, variables);
    }
    /**
     * Simplify an expression
     * @param expr The expression
     * @param variables a list of variables and their values to substitute, if required.
     * @return The simplified expression
     * @throws CompilationException An error converting to z3 occurred.
     */
    public static Expression simplify(Expression expr, Map<String, Integer> variables) throws CompilationException {
        init();
        return simplifier.get().convert(simplifier.get().convert(expr,variables).simplify());
    }

    /**
     * Initialize Z3.
     * @throws CompilationException There was an error Initializing Z3.
     */
    private static void init() throws CompilationException {
        if (simplifier.get() == null) {
            //Initialize Z3, throwing an appropriate error if this fails.
            try {
                HashMap<String, String> cfg = new HashMap<>();
                cfg.put("model", "true");
                Context ctx = new Context(cfg);
                simplifier.set(new ExpressionSimplifier(ctx));
            } catch (UnsatisfiedLinkError | NoClassDefFoundError ex) {
                throw new CompilationException(ExpressionSimplifier.class,"Unable to initialize native code. Reason: "+ex.getMessage());
            }
        }
    }

    /**
     * Combine two guards together
     * @param first The first guard
     * @param second The second guard
     * @return A logical and of both guards, with the next variables substituted from the first into the second.
     * @throws CompilationException
     */
    public static Guard combineGuards(Guard first, Guard second) throws CompilationException {
        init();
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
        HashMap<String,Expression> subMap = new HashMap<>();
        for (String str: first.getNextMap().keySet()) {
            subMap.put(str,Expression.constructExpression(first.getNextMap().get(str)));
        }
        Expression secondGuard = second.getGuard();
        //Substitute every value from the subMap into the second guard.
        secondGuard = simplifier.get().substitute(secondGuard,subMap);
        ret.setGuard(new AndOperator(first.getGuard(),secondGuard));
        return ret;
    }
    //Since we end up using this multiple times from javascript, its much easier to cache it once.
    private Context context;

    /**
     * Convert from a Z3 expression back into an Expression.
     * @param f The Z3 tree
     * @return An Expression equivalent to the Z3 expression.
     */
    private Expression convert(Expr f) throws CompilationException {
        if (f.isBVAdd()) {
            return new AdditionOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVNOT()) {
            return new BitNotOperator(convert(f.getArgs()[0]));
        }
        if (f.isNot()) {
            //Instead of keeping not followed by equal, merge them together.
            if (f.getArgs()[0].isEq()) {
                return new NotEqualOperator(convert(f.getArgs()[0].getArgs()[0]), convert(f.getArgs()[0].getArgs()[1]));
            }
            return new NotOperator(convert(f.getArgs()[0]));
        }
        if (f.isAnd()) {
            return new AndOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isOr()) {
            return new OrOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isXor()) {
            return new ExclOrOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVSGE()) {
            return new GreaterThanEqOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVSGT()) {
            return new GreaterThanOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVSLE()) {
            return new LessThanEqOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVSLT()) {
            return new LessThanOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isEq()) {
            return new EqualityOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVSMod() || f.toString().startsWith("(bvsmod")) {
            return new ModuloOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVMul()) {
            return new MultiplicationOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVSub()) {
            return new SubtractionOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVSDiv()) {
            return new DivisionOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVAdd()) {
            return new AdditionOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVAND()) {
            return new AndOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVOR()) {
            return new OrOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVShiftLeft()) {
            return new LeftShiftOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVShiftRightArithmetic()) {
            return new RightShiftOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isConst()) {
            if (f.isBool()) return new BooleanOperand(f.getBoolValue() == Z3_lbool.Z3_L_TRUE);
            return new VariableOperand(f.getSExpr());
        }
        if (f instanceof BitVecNum) {
            //We need to get the long value here, as the results are unsigned, so ints introduce errors as they overflow before
            //becoming signed again.
            return new IntegerOperand((int) ((BitVecNum) f).getLong());
        }
        throw new CompilationException(ExpressionSimplifier.class,"An unknown Z3 expression was found. "+f.toString());
    }

    /**
     * Convert from a z3 class name to a simple name
     * @param className the z3 class name
     * @return Boolean for boolean expressions, Integer for integral expressions.
     */
    private String getName(String className) {
        if (className.contains("Bool")) return "`Boolean`";
        if (className.contains("BitVec")) return "`Integer`";
        return className;
    }
    /**
     * Convert from an Expression to a Z3 expression
     * @param expr The expression
     * @param variables a list of variablles to substitute if required
     * @return a Z3 expression
     * @throws CompilationException Something went wrong while converting.
     */
    private Expr convert(Expression expr, Map<String, Integer> variables) throws CompilationException {
        if (expr instanceof AdditionOperator) {
            return convert((AdditionOperator) expr, variables);
        } else if (expr instanceof AndOperator) {
            return convert((AndOperator) expr, variables);
        } else if (expr instanceof BitAndOperator) {
            return convert((BitAndOperator) expr, variables);
        } else if (expr instanceof BitOrOperator) {
            return convert((BitOrOperator) expr, variables);
        } else if (expr instanceof DivisionOperator) {
            return convert((DivisionOperator) expr, variables);
        } else if (expr instanceof EqualityOperator) {
            return convert((EqualityOperator) expr, variables);
        } else if (expr instanceof ExclOrOperator) {
            return convert((ExclOrOperator) expr, variables);
        } else if (expr instanceof GreaterThanEqOperator) {
            return convert((GreaterThanEqOperator) expr, variables);
        } else if (expr instanceof GreaterThanOperator) {
            return convert((GreaterThanOperator) expr, variables);
        } else if (expr instanceof IntegerOperand) {
            return convert((IntegerOperand) expr);
        } else if (expr instanceof LeftShiftOperator) {
            return convert((LeftShiftOperator) expr, variables);
        } else if (expr instanceof LessThanEqOperator) {
            return convert((LessThanEqOperator) expr, variables);
        } else if (expr instanceof LessThanOperator) {
            return convert((LessThanOperator) expr, variables);
        } else if (expr instanceof ModuloOperator) {
            return convert((ModuloOperator) expr, variables);
        } else if (expr instanceof MultiplicationOperator) {
            return convert((MultiplicationOperator) expr, variables);
        } else if (expr instanceof NotEqualOperator) {
            return convert((NotEqualOperator) expr, variables);
        } else if (expr instanceof OrOperator) {
            return convert((OrOperator) expr, variables);
        } else if (expr instanceof RightShiftOperator) {
            return convert((RightShiftOperator) expr, variables);
        } else if (expr instanceof SubtractionOperator) {
            return convert((SubtractionOperator) expr, variables);
        } else if (expr instanceof VariableOperand) {
            return convert((VariableOperand) expr, variables);
        } else if (expr instanceof PositiveOperator) {
            return convert((PositiveOperator) expr, variables);
        } else if (expr instanceof NegativeOperator) {
            return convert((NegativeOperator) expr, variables);
        } else if (expr instanceof BitNotOperator) {
            return convert((BitNotOperator) expr, variables);
        } else if (expr instanceof NotOperator) {
            return convert((NotOperator) expr, variables);
        } else if (expr instanceof BooleanOperand) {
            return convert((BooleanOperand) expr);
        }
        //This should never happen.
        throw new CompilationException(ExpressionSimplifier.class,"An unknown expression type was found. "+expr.getClass().getSimpleName());
    }
    //We use BitVectors here instead of Integers so that we have access to bitwise operators.
    private BitVecExpr convert(BitNotOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr right = convert(expr.getRightHandSide(),variables);
        if (right instanceof BitVecExpr)
            return context.mkBVNot((BitVecExpr) right);
        throw new CompilationException(getClass(),"Operator `~` cannot be applied to "+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(NotOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr right = convert(expr.getRightHandSide(),variables);
        if (right instanceof BoolExpr)
            return context.mkNot((BoolExpr) right);
        throw new CompilationException(getClass(),"Operator `!` cannot be applied to "+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(PositiveOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr right = convert(expr.getRightHandSide(),variables);
        if (right instanceof BitVecExpr)
            return (BitVecExpr)right;
        throw new CompilationException(getClass(),"Unary operator `+` cannot be applied to "+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(NegativeOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr right = convert(expr.getRightHandSide(),variables);
        if (right instanceof BitVecExpr)
            return context.mkBVNeg((BitVecExpr) right);
        throw new CompilationException(getClass(),"Unary operator `-` cannot be applied to "+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(AndOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BoolExpr && right instanceof BoolExpr)
            return context.mkAnd((BoolExpr)left, (BoolExpr)right);
        throw new CompilationException(getClass(),"Operator `&&` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(OrOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BoolExpr && right instanceof BoolExpr)
            return context.mkOr((BoolExpr)left, (BoolExpr)right);
        throw new CompilationException(getClass(),"Operator `||` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(ExclOrOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BoolExpr && right instanceof BoolExpr)
            return context.mkXor((BoolExpr)left, (BoolExpr)right);
        throw new CompilationException(getClass(),"Operator `^` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(GreaterThanEqOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVSGE((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `>=` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(GreaterThanOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVSGT((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `>` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(LessThanEqOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVSLE((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `<=` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(LessThanOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVSLT((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `<` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(EqualityOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (Objects.equals(getName(left.getClass().getSimpleName()), getName(right.getClass().getSimpleName())))
            return context.mkEq(left,right);
        throw new CompilationException(getClass(),"Operator `==` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BoolExpr convert(NotEqualOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        return context.mkNot(context.mkEq(left, right));
    }
    private BitVecExpr convert(ModuloOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVSMod((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `%` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(MultiplicationOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVMul((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `*` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(SubtractionOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVSub((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `-` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(DivisionOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVSDiv((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `/` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(AdditionOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVAdd((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `+` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(BitAndOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVAND((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `&` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(BitOrOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVOR((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `|` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(LeftShiftOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVSHL((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `<<` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(RightShiftOperator expr, Map<String, Integer> variables) throws CompilationException {
        Expr left = convert(expr.getLeftHandSide(),variables);
        Expr right = convert(expr.getRightHandSide(),variables);
        if (left instanceof BitVecExpr && right instanceof BitVecExpr)
            return context.mkBVASHR((BitVecExpr)left, (BitVecExpr)right);
        throw new CompilationException(getClass(),"Operator `>>` cannot be applied to "+getName(left.getClass().getSimpleName())+","+getName(right.getClass().getSimpleName()));
    }
    private BitVecExpr convert(VariableOperand expr, Map<String, Integer> variables) {
        if (variables.containsKey(expr.getValue())) {
            return context.mkBV(variables.get(expr.getValue()),32);
        }
        return context.mkBVConst(expr.getValue(),32);
    }
    private BitVecExpr convert(IntegerOperand expr) {
        return context.mkBV(expr.getValue(),32);
    }
    private BoolExpr convert(BooleanOperand expr) {
        return context.mkBool(expr.getValue());
    }

    /**
     * Substitute a variable for a replacement.
     * @param expression the expression to substitute
     * @param subMap The map of variables to substitutions
     * @return the substituted expression.
     */
    private static Expression substitute(BinaryOperator expression, Map<String, Expression> subMap) throws CompilationException {
        expression.setLhs(substitute(expression.getLeftHandSide(), subMap));
        expression.setRhs(substitute(expression.getRightHandSide(), subMap));
        return expression;
    }
    private static Expression substitute(UnaryOperator expression, Map<String, Expression> subMap) throws CompilationException {
        expression.setRhs(substitute(expression.getRightHandSide(), subMap));
        return expression;
    }
    @SneakyThrows
    public static Expression substitute(Expression expression, Map<String, Expression> subMap) {
        if (subMap == null) return expression;
        if (expression instanceof BinaryOperator) {
            return substitute((BinaryOperator) expression, subMap);
        }
        if (expression instanceof UnaryOperator) {
            return substitute((UnaryOperator)expression, subMap);
        }
        if (expression instanceof IntegerOperand) return expression;
        if (expression instanceof VariableOperand) {
            if (subMap.containsKey(((VariableOperand) expression).getValue())) {
                return subMap.get(((VariableOperand) expression).getValue());
            }
            return expression;
        }
        throw new CompilationException(ExpressionSimplifier.class, "An unknown expression type was found when trying to substitute. "+expression.getClass().getSimpleName());
    }
}
