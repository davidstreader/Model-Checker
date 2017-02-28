package mc.util.expr;

import java.util.*;

public class VariableCollector {

    private void collectVariables(Expression expression, List<String> vars){
        if(expression instanceof VariableOperand){
            collectVariables((VariableOperand)expression, vars);
        }
        else if(expression instanceof BinaryOperator){
            collectVariables((BinaryOperator)expression, vars);
        }
        else if(expression instanceof UnaryOperator){
            collectVariables((UnaryOperator)expression, vars);
        }
    }

    private void collectVariables(VariableOperand expression, List<String> vars){
        vars.add(expression.getValue());
    }

    private void collectVariables(BinaryOperator expression, List<String> vars){
        collectVariables(expression.getLeftHandSide(), vars);
        collectVariables(expression.getRightHandSide(), vars);
    }

    private void collectVariables(UnaryOperator expression, List<String> vars){
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
