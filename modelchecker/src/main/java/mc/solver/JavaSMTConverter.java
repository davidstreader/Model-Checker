package mc.solver;

import com.microsoft.z3.*;
import mc.compiler.Guard;
import mc.util.expr.*;

import java.util.HashMap;

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
            return new VariableOperand(f.getSExpr());
        }
        if (f instanceof BitVecNum) {
            return new IntegerOperand(((BitVecNum) f).getInt());
        }
        throw new IllegalStateException("Error");
    }
    public Expression simplify(Expression expr) {
        return convert(convert(expr).simplify());
    }
    private Expr convert(Expression expr) {
        if (expr instanceof AdditionOperator) {
            return convert((AdditionOperator)expr);
        } else if(expr instanceof AndOperator) {
            return convert((AndOperator)expr);
        } else if(expr instanceof BitAndOperator) {
            return convert((BitAndOperator)expr);
        } else if (expr instanceof BitOrOperator) {
            return convert((BitOrOperator) expr);
        } else if (expr instanceof DivisionOperator) {
            return convert((DivisionOperator)expr);
        } else if (expr instanceof EqualityOperator) {
            return convert((EqualityOperator)expr);
        } else if (expr instanceof ExclOrOperator) {
            return convert((ExclOrOperator)expr);
        } else if (expr instanceof GreaterThanEqOperator) {
            return convert((GreaterThanEqOperator)expr);
        } else if (expr instanceof GreaterThanOperator) {
            return convert((GreaterThanOperator)expr);
        } else if (expr instanceof IntegerOperand) {
            return convert((IntegerOperand)expr);
        } else if (expr instanceof LeftShiftOperator) {
            return convert((LeftShiftOperator) expr);
        } else if (expr instanceof LessThanEqOperator) {
            return convert((LessThanEqOperator)expr);
        } else if (expr instanceof LessThanOperator) {
            return convert((LessThanOperator)expr);
        } else if (expr instanceof ModuloOperator) {
            return convert((ModuloOperator)expr);
        } else if (expr instanceof MultiplicationOperator) {
            return convert((MultiplicationOperator)expr);
        } else if (expr instanceof NotEqualOperator) {
            return convert((NotEqualOperator)expr);
        } else if (expr instanceof OrOperator) {
            return convert((OrOperator)expr);
        } else if (expr instanceof RightShiftOperator) {
            return convert((RightShiftOperator) expr);
        } else if (expr instanceof SubtractionOperator) {
            return convert((SubtractionOperator)expr);
        } else if (expr instanceof VariableOperand) {
            return convert((VariableOperand)expr);
        } else if (expr instanceof BitNotOperator) {
            return convert((BitNotOperator)expr);
        }else if (expr instanceof NotOperator) {
            return convert((NotOperator)expr);
        }
        //This should never happen.
        throw new IllegalStateException("Solver reached an unexpected state");
    }
    //We use BitVectors here instead of Integers so that we have access to bitwise operators.
    private BitVecExpr convert(BitNotOperator expr) {
        return context.mkBVNot((BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(NotOperator expr) {
        return context.mkNot((BoolExpr) convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(AndOperator expr) {
        return context.mkAnd((BoolExpr) convert(expr.getLeftHandSide()), (BoolExpr) convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(OrOperator expr) {
        return context.mkOr((BoolExpr) convert(expr.getLeftHandSide()), (BoolExpr) convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(ExclOrOperator expr) {
        return context.mkXor((BoolExpr) convert(expr.getLeftHandSide()), (BoolExpr) convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(GreaterThanEqOperator expr) {
        return context.mkBVSGE((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(GreaterThanOperator expr) {
        return context.mkBVSGT((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(LessThanEqOperator expr) {
        return context.mkBVSLE((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(LessThanOperator expr) {
        return context.mkBVSLT((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(EqualityOperator expr) {
        return context.mkEq(convert(expr.getLeftHandSide()), convert(expr.getRightHandSide()));
    }
    private BoolExpr convert(NotEqualOperator expr) {
        return context.mkNot(context.mkEq(convert(expr.getLeftHandSide()), convert(expr.getRightHandSide())));
    }
    private BitVecExpr convert(ModuloOperator expr) {
        return context.mkBVSMod((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BitVecExpr convert(MultiplicationOperator expr) {
        return context.mkBVMul((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BitVecExpr convert(SubtractionOperator expr) {
        return context.mkBVSub((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BitVecExpr convert(DivisionOperator expr) {
        return context.mkBVSDiv((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BitVecExpr convert(AdditionOperator expr) {
        return context.mkBVAdd((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BitVecExpr convert(BitAndOperator expr) {
        return context.mkBVAND((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BitVecExpr convert(BitOrOperator expr) {
        return context.mkBVOR((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BitVecExpr convert(LeftShiftOperator expr) {
        return context.mkBVSHL((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BitVecExpr convert(RightShiftOperator expr) {
        return context.mkBVASHR((BitVecExpr) convert(expr.getLeftHandSide()), (BitVecExpr) convert(expr.getRightHandSide()));
    }
    private BitVecExpr convert(VariableOperand expr) {
        return context.mkBVConst(expr.getValue(),32);
    }
    private BitVecExpr convert(IntegerOperand expr) {
        return context.mkBV(expr.getValue(),32);
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

        ret.setGuard(simplify(new AndOperator(first.getGuard(),substitute(secondGuard,subMap))));
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
