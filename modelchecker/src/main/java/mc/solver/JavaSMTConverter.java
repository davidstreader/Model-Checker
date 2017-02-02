package mc.solver;

import com.microsoft.z3.*;
import com.microsoft.z3.enumerations.Z3_lbool;
import mc.compiler.Guard;
import mc.util.expr.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class JavaSMTConverter {
    public JavaSMTConverter(Context o) {
        context = o;
    }
    //Since we end up using this multiple times from javascript, its much easier to cache it once.
    private Context context;
    public Expression convert(Expr f) {
        if (f.isBVAdd()) {
            return new AdditionOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVNOT()) {
            return new BitNotOperator(convert(f.getArgs()[0]));
        }
        if (f.isNot()) {
            if (f.getArgs()[0].isEq()) {
                return new NotEqualOperator(convert(f.getArgs()[0].getArgs()[0]),convert(f.getArgs()[0].getArgs()[1]));
            }
            return new NotOperator(convert(f.getArgs()[0]));
        }
        if (f.isAnd()) {
            return new AndOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isOr()) {
            return new OrOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isXor()) {
            return new ExclOrOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVSGE()) {
            return new GreaterThanEqOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVSGT()) {
            return new GreaterThanOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVSLE()) {
            return new LessThanEqOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVSLT()) {
            return new LessThanOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isEq()) {
            return new EqualityOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVSMod()) {
            return new ModuloOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVMul()) {
            return new MultiplicationOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVSub()) {
            return new SubtractionOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVSDiv()) {
            return new DivisionOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVAdd()) {
            return new AdditionOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVAND()) {
            return new AndOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVOR()) {
            return new OrOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVShiftLeft()) {
            return new LeftShiftOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isBVShiftRightArithmetic()) {
            return new RightShiftOperator(convert(f.getArgs()[0]),convert(f.getArgs()[1]));
        }
        if (f.isConst()) {
            if (f.isBool()) return new BooleanOperand(f.getBoolValue()==Z3_lbool.Z3_L_TRUE);
            return new VariableOperand(f.getSExpr());
        }
        if (f instanceof BitVecNum) {
            return new IntegerOperand(((BitVecNum) f).getInt());
        }
        throw new IllegalStateException("Error");
    }
    public Expression simplify(Expression expr, Map<String, Integer> variables) {
        return convert(convert(expr,variables).simplify());
    }
    private Expr convert(Expression expr, Map<String, Integer> variables) {
        if (expr instanceof AdditionOperator) {
            return convert((AdditionOperator)expr,variables);
        } else if(expr instanceof AndOperator) {
            return convert((AndOperator)expr,variables);
        } else if(expr instanceof BitAndOperator) {
            return convert((BitAndOperator)expr,variables);
        } else if (expr instanceof BitOrOperator) {
            return convert((BitOrOperator) expr,variables);
        } else if (expr instanceof DivisionOperator) {
            return convert((DivisionOperator)expr,variables);
        } else if (expr instanceof EqualityOperator) {
            return convert((EqualityOperator)expr,variables);
        } else if (expr instanceof ExclOrOperator) {
            return convert((ExclOrOperator)expr,variables);
        } else if (expr instanceof GreaterThanEqOperator) {
            return convert((GreaterThanEqOperator)expr,variables);
        } else if (expr instanceof GreaterThanOperator) {
            return convert((GreaterThanOperator)expr,variables);
        } else if (expr instanceof IntegerOperand) {
            return convert((IntegerOperand)expr);
        } else if (expr instanceof LeftShiftOperator) {
            return convert((LeftShiftOperator) expr,variables);
        } else if (expr instanceof LessThanEqOperator) {
            return convert((LessThanEqOperator)expr,variables);
        } else if (expr instanceof LessThanOperator) {
            return convert((LessThanOperator)expr,variables);
        } else if (expr instanceof ModuloOperator) {
            return convert((ModuloOperator)expr,variables);
        } else if (expr instanceof MultiplicationOperator) {
            return convert((MultiplicationOperator)expr,variables);
        } else if (expr instanceof NotEqualOperator) {
            return convert((NotEqualOperator)expr,variables);
        } else if (expr instanceof OrOperator) {
            return convert((OrOperator)expr,variables);
        } else if (expr instanceof RightShiftOperator) {
            return convert((RightShiftOperator) expr,variables);
        } else if (expr instanceof SubtractionOperator) {
            return convert((SubtractionOperator)expr,variables);
        } else if (expr instanceof VariableOperand) {
            return convert((VariableOperand)expr,variables);
        } else if (expr instanceof BitNotOperator) {
            return convert((BitNotOperator)expr,variables);
        }else if (expr instanceof NotOperator) {
            return convert((NotOperator)expr,variables);
        }else if (expr instanceof BooleanOperand) {
            return convert((BooleanOperand)expr);
        }
        //This should never happen.
        throw new IllegalStateException("Solver reached an unexpected state");
    }
    //We use BitVectors here instead of Integers so that we have access to bitwise operators.
    private BitVecExpr convert(BitNotOperator expr, Map<String, Integer> variables) {
        return context.mkBVNot((BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(NotOperator expr, Map<String, Integer> variables) {
        return context.mkNot((BoolExpr) convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(AndOperator expr, Map<String, Integer> variables) {
        return context.mkAnd((BoolExpr) convert(expr.getLeftHandSide(), variables), (BoolExpr) convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(OrOperator expr, Map<String, Integer> variables) {
        return context.mkOr((BoolExpr) convert(expr.getLeftHandSide(), variables), (BoolExpr) convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(ExclOrOperator expr, Map<String, Integer> variables) {
        return context.mkXor((BoolExpr) convert(expr.getLeftHandSide(), variables), (BoolExpr) convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(GreaterThanEqOperator expr, Map<String, Integer> variables) {
        return context.mkBVSGE((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(GreaterThanOperator expr, Map<String, Integer> variables) {
        return context.mkBVSGT((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(LessThanEqOperator expr, Map<String, Integer> variables) {
        return context.mkBVSLE((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(LessThanOperator expr, Map<String, Integer> variables) {
        return context.mkBVSLT((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(EqualityOperator expr, Map<String, Integer> variables) {
        return context.mkEq(convert(expr.getLeftHandSide(), variables), convert(expr.getRightHandSide(), variables));
    }
    private BoolExpr convert(NotEqualOperator expr, Map<String, Integer> variables) {
        return context.mkNot(context.mkEq(convert(expr.getLeftHandSide(), variables), convert(expr.getRightHandSide(), variables)));
    }
    private BitVecExpr convert(ModuloOperator expr, Map<String, Integer> variables) {
        return context.mkBVSMod((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BitVecExpr convert(MultiplicationOperator expr, Map<String, Integer> variables) {
        return context.mkBVMul((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BitVecExpr convert(SubtractionOperator expr, Map<String, Integer> variables) {
        return context.mkBVSub((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BitVecExpr convert(DivisionOperator expr, Map<String, Integer> variables) {
        return context.mkBVSDiv((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BitVecExpr convert(AdditionOperator expr, Map<String, Integer> variables) {
        return context.mkBVAdd((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BitVecExpr convert(BitAndOperator expr, Map<String, Integer> variables) {
        return context.mkBVAND((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BitVecExpr convert(BitOrOperator expr, Map<String, Integer> variables) {
        return context.mkBVOR((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BitVecExpr convert(LeftShiftOperator expr, Map<String, Integer> variables) {
        return context.mkBVSHL((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
    }
    private BitVecExpr convert(RightShiftOperator expr, Map<String, Integer> variables) {
        return context.mkBVASHR((BitVecExpr) convert(expr.getLeftHandSide(), variables), (BitVecExpr) convert(expr.getRightHandSide(), variables));
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

    public Guard combineGuards(Guard first, Guard second) {
        Guard ret = new Guard();
        ret.setVariables(first.getVariables());
        ret.setNext(second.getNext());
        HashMap<String,Expression> subMap = new HashMap<>();
        for (String str: first.getNextMap().keySet()) {
            subMap.put(str,Expression.constructExpression(first.getNextMap().get(str)));
        }
        Expression secondGuard = second.getGuard();

        ret.setGuard(simplify(new AndOperator(first.getGuard(),substitute(secondGuard,subMap)), Collections.emptyMap()));
        return ret;
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
