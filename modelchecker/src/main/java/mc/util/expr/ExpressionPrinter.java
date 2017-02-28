package mc.util.expr;

import java.util.*;

public class ExpressionPrinter {
    public String printExpression(Expression expression, Map<String, Integer> variableMap){
        return print(expression, variableMap);
    }

    private String print(Expression expression, Map<String, Integer> variableMap){
        if(expression instanceof IntegerOperand){
            return print((IntegerOperand)expression);
        }
        else if(expression instanceof BooleanOperand){
            return print((BooleanOperand)expression);
        }
        else if(expression instanceof VariableOperand){
            return print((VariableOperand)expression, variableMap);
        }
        else if(expression instanceof AdditionOperator){
            return print((AdditionOperator)expression, variableMap);
        }
        else if(expression instanceof SubtractionOperator){
            return print((SubtractionOperator)expression, variableMap);
        }
        else if(expression instanceof MultiplicationOperator){
            return print((MultiplicationOperator)expression, variableMap);
        }
        else if(expression instanceof DivisionOperator){
            return print((DivisionOperator)expression, variableMap);
        }
        else if(expression instanceof ModuloOperator){
            return print((ModuloOperator)expression, variableMap);
        }
        else if(expression instanceof LeftShiftOperator){
            return print((LeftShiftOperator)expression, variableMap);
        }
        else if(expression instanceof RightShiftOperator){
            return print((RightShiftOperator)expression, variableMap);
        }
        else if(expression instanceof OrOperator){
            return print((OrOperator)expression, variableMap);
        }
        else if(expression instanceof BitOrOperator){
            return print((BitOrOperator)expression, variableMap);
        }
        else if(expression instanceof ExclOrOperator){
            return print((ExclOrOperator)expression, variableMap);
        }
        else if(expression instanceof AndOperator){
            return print((AndOperator)expression, variableMap);
        }
        else if(expression instanceof BitAndOperator){
            return print((BitAndOperator)expression, variableMap);
        }
        else if(expression instanceof EqualityOperator){
            return print((EqualityOperator)expression, variableMap);
        }
        else if(expression instanceof NotEqualOperator){
            return print((NotEqualOperator)expression, variableMap);
        }
        else if(expression instanceof LessThanOperator){
            return print((LessThanOperator)expression, variableMap);
        }
        else if(expression instanceof LessThanEqOperator){
            return print((LessThanEqOperator)expression, variableMap);
        }
        else if(expression instanceof GreaterThanOperator){
            return print((GreaterThanOperator)expression, variableMap);
        }
        else if(expression instanceof GreaterThanEqOperator){
            return print((GreaterThanEqOperator)expression, variableMap);
        }
        else if(expression instanceof NotOperator){
            return print((NotOperator)expression, variableMap);
        }
        else if(expression instanceof BitNotOperator){
            return print((BitNotOperator)expression, variableMap);
        }

        throw new IllegalArgumentException("");
    }

    private String print(IntegerOperand expression){
        return expression.getValue()+"";
    }
    private String print(BooleanOperand expression){
        return expression.getValue()+"";
    }

    private String print(VariableOperand expression, Map<String, Integer> variableMap){
        return variableMap.containsKey(expression.getValue())?variableMap.get(expression.getValue())+"":expression.getValue();
    }

    private String print(AdditionOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs +"+"+ rhs+")";
    }

    private String print(SubtractionOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs +"-"+ rhs+")";
    }

    private String print(MultiplicationOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs +"*"+ rhs+")";
    }

    private String print(DivisionOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs +"/"+ rhs+")";
    }

    private String print(ModuloOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs +"%"+ rhs+")";
    }

    private String print(LeftShiftOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs +"<<"+ rhs+")";
    }

    private String print(RightShiftOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs +">>"+ rhs+")";
    }

    private String print(OrOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+"||"+rhs+")";
    }

    private String print(BitOrOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+"|"+rhs+")";
    }

    private String print(ExclOrOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+"^"+rhs+")";
    }

    private String print(AndOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+"&&"+rhs+")";
    }

    private String print(BitAndOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+"&"+rhs+")";
    }

    private String print(EqualityOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+"=="+rhs+")";
    }

    private String print(NotEqualOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+"!="+rhs+")";
    }

    private String print(LessThanOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+"<"+rhs+")";
    }

    private String print(LessThanEqOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+"<="+rhs+")";
    }

    private String print(GreaterThanOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+">"+rhs+")";
    }

    private String print(GreaterThanEqOperator expression, Map<String, Integer> variableMap){
        String lhs = print(expression.getLeftHandSide(), variableMap);
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+lhs+">="+rhs+")";
    }

    private String print(NotOperator expression, Map<String, Integer> variableMap){
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+"!"+rhs+")";
    }

    private String print(BitNotOperator expression, Map<String, Integer> variableMap){
        String rhs = print(expression.getRightHandSide(), variableMap);
        return "("+"~"+rhs+")";
    }

    public String printExpression(Expression expression) {
        return printExpression(expression, Collections.emptyMap());
    }
}
