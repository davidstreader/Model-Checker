package mc.util.expr;

import java.util.Map;

/**
 * Created by sheriddavi on 19/01/17.
 */
public class ExpressionPrinter {

  public String printExpression(Expression expression, Map<String, Integer> variableMap){
    return print(expression, variableMap);
  }

  private String print(Expression expression, Map<String, Integer> variableMap){
    if(expression instanceof IntegerOperand){
      return evaluate((IntegerOperand)expression);
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

  private String evaluate(IntegerOperand expression){
    return expression.getValue()+"";
  }

  private String evaluate(VariableOperand expression, Map<String, Integer> variableMap){
    return variableMap.containsKey(expression.getValue())?variableMap.get(expression.getValue())+"":expression.getValue();
  }

  private String evaluate(AdditionOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs +"+"+ rhs+")";
  }

  private String evaluate(SubtractionOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs +"-"+ rhs+")";
  }

  private String evaluate(MultiplicationOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs +"*"+ rhs+")";
  }

  private String evaluate(DivisionOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs +"/"+ rhs+")";
  }

  private String evaluate(ModuloOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs +"%"+ rhs+")";
  }

  private String evaluate(LeftShiftOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs +"<<"+ rhs+")";
  }

  private String evaluate(RightShiftOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs +">>"+ rhs+")";
  }

  private String evaluate(OrOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+"||"+rhs+")";
  }

  private String evaluate(BitOrOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+"|"+rhs+")";
  }

  private String evaluate(ExclOrOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+"^"+rhs+")";
  }

  private String evaluate(AndOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+"&&"+rhs+")";
  }

  private String evaluate(BitAndOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+"&"+rhs+")";
  }

  private String evaluate(EqualityOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+"=="+rhs+")";
  }

  private String evaluate(NotEqualOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+"!="+rhs+")";
  }

  private String evaluate(LessThanOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+"<"+rhs+")";
  }

  private String evaluate(LessThanEqOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+"<="+rhs+")";
  }

  private String evaluate(GreaterThanOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+">"+rhs+")";
  }

  private String evaluate(GreaterThanEqOperator expression, Map<String, Integer> variableMap){
    String lhs = print(expression.getLeftHandSide(), variableMap);
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+lhs+">="+rhs+")";
  }

  private String evaluate(NotOperator expression, Map<String, Integer> variableMap){
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+"!"+rhs+")";
  }

  private String evaluate(BitNotOperator expression, Map<String, Integer> variableMap){
    String rhs = print(expression.getRightHandSide(), variableMap);
    return "("+"~"+rhs+")";
  }
}
