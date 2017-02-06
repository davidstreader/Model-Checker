package mc.solver;

import com.microsoft.z3.*;
import com.microsoft.z3.enumerations.Z3_lbool;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.util.expr.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExpressionSimplifier {
    private static ExpressionSimplifier simplifier;
    private static Logger logger = LoggerFactory.getLogger(ExpressionSimplifier.class);
    static {
        try {
            HashMap<String, String> cfg = new HashMap<>();
            cfg.put("model", "true");
            Context ctx = new Context(cfg);
            simplifier = new ExpressionSimplifier(ctx);
        } catch (UnsatisfiedLinkError ex) {
            logger.error("Unable to load native libraries. Error: "+ex.getLocalizedMessage());
        }
    }
    private ExpressionSimplifier(Context o) {
        context = o;
    }

    public static Expression simplify(Expression expr, Map<String, Integer> variables) throws CompilationException {
        return simplifier.convert(simplifier.convert(expr,variables).simplify());
    }

    public static Guard combineGuards(Guard first, Guard second) throws CompilationException {
        Guard ret = new Guard();
        ret.setVariables(first.getVariables());
        ret.setNext(second.getNext());
        HashMap<String,Expression> subMap = new HashMap<>();
        for (String str: first.getNextMap().keySet()) {
            subMap.put(str,Expression.constructExpression(first.getNextMap().get(str)));
        }
        Expression secondGuard = second.getGuard();

        ret.setGuard(simplify(new AndOperator(first.getGuard(),simplifier.substitute(secondGuard,subMap)), Collections.emptyMap()));
        return ret;
    }
    //Since we end up using this multiple times from javascript, its much easier to cache it once.
    private Context context;
    private Expression convert(Expr f) {
        if (f.isBVAdd()) {
            return new AdditionOperator(convert(f.getArgs()[0]), convert(f.getArgs()[1]));
        }
        if (f.isBVNOT()) {
            return new BitNotOperator(convert(f.getArgs()[0]));
        }
        if (f.isNot()) {
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
        if (f.isBVSMod()) {
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
            //We need to get the long value here, as the results are unsigned, so ints introduce errors when retrieving.
            return new IntegerOperand((int) ((BitVecNum) f).getLong());
        }
        throw new IllegalStateException("Error");
    }

    private String getName(String className) {
        if (className.contains("Bool")) return "`Boolean`";
        if (className.contains("BitVec")) return "`Integer`";
        return className;
    }
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
        } else if (expr instanceof BitNotOperator) {
            return convert((BitNotOperator) expr, variables);
        } else if (expr instanceof NotOperator) {
            return convert((NotOperator) expr, variables);
        } else if (expr instanceof BooleanOperand) {
            return convert((BooleanOperand) expr);
        }
        //This should never happen.
        throw new IllegalStateException("Solver reached an unexpected state");
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
        if (left.getClass() == right.getClass())
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
    private Expression substitute(BothOperator expression, HashMap<String, Expression> subMap) {
        expression.setLhs(substitute(expression.getLeftHandSide(), subMap));
        expression.setRhs(substitute(expression.getRightHandSide(), subMap));
        return expression;
    }
    private Expression substitute(RightOperator expression, HashMap<String, Expression> subMap) {
        expression.setRhs(substitute(expression.getRightHandSide(), subMap));
        return expression;
    }
    private Expression substitute(Expression expression, HashMap<String, Expression> subMap) {
        if (expression instanceof BothOperator) {
            return substitute((BothOperator) expression, subMap);
        }
        if (expression instanceof RightOperator) {
            return substitute((RightOperator)expression, subMap);
        }
        if (expression instanceof IntegerOperand) return expression;
        if (expression instanceof VariableOperand) {
            if (subMap.containsKey(((VariableOperand) expression).getValue())) {
                return subMap.get(((VariableOperand) expression).getValue());
            }
            return expression;
        }
        throw new IllegalStateException("Should not be here.");
    }
}
