package mc.util.expr;

import mc.exceptions.CompilationException;
import mc.solver.ExpressionSimplifier;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sheriddavi on 19/01/17.
 */
public class ExpressionEvaluator {

    public boolean isExecutable(Expression expression) throws CompilationException {
        //If you simplify an expression with no variables it will be evaluated by the solver.
        return ExpressionSimplifier.simplify(expression, Collections.emptyMap()) instanceof Operand;
    }

    public int evaluateExpression(Expression expression, Map<String, Integer> variableMap){
        return evaluate(expression, variableMap);
    }

    private int evaluate(Expression expression, Map<String, Integer> variableMap){
        if(expression instanceof IntegerOperand){
            return evaluate((IntegerOperand)expression);
        }
        else if(expression instanceof BooleanOperand){
            return evaluate((BooleanOperand)expression);
        }
        else if(expression instanceof VariableOperand){
            return evaluate((VariableOperand)expression, variableMap);
        }
        else if(expression instanceof AdditionOperator){
            return evaluate((AdditionOperator)expression, variableMap);
        }
        else if(expression instanceof SubtractionOperator){
            return evaluate((SubtractionOperator)expression, variableMap);
        }
        else if(expression instanceof MultiplicationOperator){
            return evaluate((MultiplicationOperator)expression, variableMap);
        }
        else if(expression instanceof DivisionOperator){
            return evaluate((DivisionOperator)expression, variableMap);
        }
        else if(expression instanceof ModuloOperator){
            return evaluate((ModuloOperator)expression, variableMap);
        }
        else if(expression instanceof LeftShiftOperator){
            return evaluate((LeftShiftOperator)expression, variableMap);
        }
        else if(expression instanceof RightShiftOperator){
            return evaluate((RightShiftOperator)expression, variableMap);
        }
        else if(expression instanceof OrOperator){
            return evaluate((OrOperator)expression, variableMap);
        }
        else if(expression instanceof BitOrOperator){
            return evaluate((BitOrOperator)expression, variableMap);
        }
        else if(expression instanceof ExclOrOperator){
            return evaluate((ExclOrOperator)expression, variableMap);
        }
        else if(expression instanceof AndOperator){
            return evaluate((AndOperator)expression, variableMap);
        }
        else if(expression instanceof BitAndOperator){
            return evaluate((BitAndOperator)expression, variableMap);
        }
        else if(expression instanceof EqualityOperator){
            return evaluate((EqualityOperator)expression, variableMap);
        }
        else if(expression instanceof NotEqualOperator){
            return evaluate((NotEqualOperator)expression, variableMap);
        }
        else if(expression instanceof LessThanOperator){
            return evaluate((LessThanOperator)expression, variableMap);
        }
        else if(expression instanceof LessThanEqOperator){
            return evaluate((LessThanEqOperator)expression, variableMap);
        }
        else if(expression instanceof GreaterThanOperator){
            return evaluate((GreaterThanOperator)expression, variableMap);
        }
        else if(expression instanceof GreaterThanEqOperator){
            return evaluate((GreaterThanEqOperator)expression, variableMap);
        }
        else if(expression instanceof NotOperator){
            return evaluate((NotOperator)expression, variableMap);
        }
        else if(expression instanceof BitNotOperator){
            return evaluate((BitNotOperator)expression, variableMap);
        }

        throw new IllegalArgumentException("");
    }

    private int evaluate(BooleanOperand expression) {
        return expression.getValue()?1:0;
    }

    private int evaluate(IntegerOperand expression){
        return expression.getValue();
    }

    private int evaluate(VariableOperand expression, Map<String, Integer> variableMap){
        if(variableMap.containsKey(expression.getValue())){
            return variableMap.get(expression.getValue());
        }

        return 0;
    }

    private int evaluate(AdditionOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        return lhs + rhs;
    }

    private int evaluate(SubtractionOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        return lhs - rhs;
    }

    private int evaluate(MultiplicationOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        return lhs * rhs;
    }

    private int evaluate(DivisionOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        return lhs / rhs;
    }

    private int evaluate(ModuloOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        return lhs % rhs;
    }

    private int evaluate(LeftShiftOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        return lhs << rhs;
    }

    private int evaluate(RightShiftOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        return lhs >> rhs;
    }

    private int evaluate(OrOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs != 0 || rhs != 0;
        return result ? 1 : 0;
    }

    private int evaluate(BitOrOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs != 0 | rhs != 0;
        return result ? 1 : 0;
    }

    private int evaluate(ExclOrOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs != 0 ^ rhs != 0;
        return result ? 1 : 0;
    }

    private int evaluate(AndOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs != 0 && rhs != 0;
        return result ? 1 : 0;
    }

    private int evaluate(BitAndOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs != 0 & rhs != 0;
        return result ? 1 : 0;
    }

    private int evaluate(EqualityOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs == rhs;
        return result ? 1 : 0;
    }

    private int evaluate(NotEqualOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs != rhs;
        return result ? 1 : 0;
    }

    private int evaluate(LessThanOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs < rhs;
        return result ? 1 : 0;
    }

    private int evaluate(LessThanEqOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs <= rhs;
        return result ? 1 : 0;
    }

    private int evaluate(GreaterThanOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs > rhs;
        return result ? 1 : 0;
    }

    private int evaluate(GreaterThanEqOperator expression, Map<String, Integer> variableMap){
        int lhs = evaluate(expression.getLeftHandSide(), variableMap);
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        boolean result = lhs >= rhs;
        return result ? 1 : 0;
    }

    private int evaluate(NotOperator expression, Map<String, Integer> variableMap){
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        return rhs == 0 ? 1 : 0;
    }

    private int evaluate(BitNotOperator expression, Map<String, Integer> variableMap){
        int rhs = evaluate(expression.getRightHandSide(), variableMap);
        return ~rhs;
    }
}
