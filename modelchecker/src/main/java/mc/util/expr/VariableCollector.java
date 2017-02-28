package mc.util.expr;

import java.util.*;

public class VariableCollector {

    private void collectVariables(Expression expression, List<String> vars){
        if(expression instanceof VariableOperand){
            collectVariables((VariableOperand)expression, vars);
        }
        else if(expression instanceof AdditionOperator){
            collectVariables((AdditionOperator)expression, vars);
        }
        else if(expression instanceof SubtractionOperator){
            collectVariables((SubtractionOperator)expression, vars);
        }
        else if(expression instanceof MultiplicationOperator){
            collectVariables((MultiplicationOperator)expression, vars);
        }
        else if(expression instanceof DivisionOperator){
            collectVariables((DivisionOperator)expression, vars);
        }
        else if(expression instanceof ModuloOperator){
            collectVariables((ModuloOperator)expression, vars);
        }
        else if(expression instanceof LeftShiftOperator){
            collectVariables((LeftShiftOperator)expression, vars);
        }
        else if(expression instanceof RightShiftOperator){
            collectVariables((RightShiftOperator)expression, vars);
        }
        else if(expression instanceof OrOperator){
            collectVariables((OrOperator)expression, vars);
        }
        else if(expression instanceof BitOrOperator){
            collectVariables((BitOrOperator)expression, vars);
        }
        else if(expression instanceof ExclOrOperator){
            collectVariables((ExclOrOperator)expression, vars);
        }
        else if(expression instanceof AndOperator){
            collectVariables((AndOperator)expression, vars);
        }
        else if(expression instanceof BitAndOperator){
            collectVariables((BitAndOperator)expression, vars);
        }
        else if(expression instanceof EqualityOperator){
            collectVariables((EqualityOperator)expression, vars);
        }
        else if(expression instanceof NotEqualOperator){
            collectVariables((NotEqualOperator)expression, vars);
        }
        else if(expression instanceof LessThanOperator){
            collectVariables((LessThanOperator)expression, vars);
        }
        else if(expression instanceof LessThanEqOperator){
            collectVariables((LessThanEqOperator)expression, vars);
        }
        else if(expression instanceof GreaterThanOperator){
            collectVariables((GreaterThanOperator)expression, vars);
        }
        else if(expression instanceof GreaterThanEqOperator){
            collectVariables((GreaterThanEqOperator)expression, vars);
        }
        else if(expression instanceof NotOperator){
            collectVariables((NotOperator)expression, vars);
        }
        else if(expression instanceof BitNotOperator){
            collectVariables((BitNotOperator)expression, vars);
        }
    }

    private void collectVariables(VariableOperand expression, List<String> vars){
        vars.add(expression.getValue());
    }

    private void collectVariables(AdditionOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(SubtractionOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(MultiplicationOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(DivisionOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(ModuloOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(LeftShiftOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(RightShiftOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(OrOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(BitOrOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(ExclOrOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(AndOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(BitAndOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(EqualityOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(NotEqualOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(LessThanOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(LessThanEqOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(GreaterThanOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(GreaterThanEqOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(NotOperator expression, List<String> vars){
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(BitNotOperator expression, List<String> vars){
        collectVariables(expression.getRightHandSide(), vars);
    }

    public Map<String, Integer> getVariables(Expression expression, Map<String, Object> variableMap) {
        ArrayList<String> vars = new ArrayList<>();
        //Get just the variables from the map
        HashMap<String,Integer> varMap = new HashMap<>();
        for (String key : variableMap.keySet()) {
            if (variableMap.get(key) instanceof Integer) {
                varMap.put(key, (Integer) variableMap.get(key));
            }
        }
        //Print the expression, keeping track of all used variables
        collectVariables(expression, vars);
        //Map from used variables to list of variables and their values
        Map<String,Integer> newVarMap = new HashMap<>();
        for (String var: vars) {
            if (varMap.containsKey(var))
                newVarMap.put(var,varMap.get(var));
        }
        return newVarMap;
    }
}
