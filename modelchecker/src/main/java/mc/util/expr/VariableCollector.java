package mc.util.expr;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class VariableCollector {

    private Stream<String> collectVariables(Expression expression){
        if(expression instanceof VariableOperand){
            return Stream.of(((VariableOperand)expression).getValue());
        }
        else if(expression instanceof BinaryOperator){
            return collectVariables((BinaryOperator)expression);
        }
        else if(expression instanceof UnaryOperator){
            return collectVariables((UnaryOperator)expression);
        }
        return Stream.empty();
    }

    private Stream<String> collectVariables(BinaryOperator expression){
        return Stream.concat(collectVariables(expression.getLeftHandSide()),collectVariables(expression.getRightHandSide()));
    }

    private Stream<String> collectVariables(UnaryOperator expression){
        return collectVariables(expression.getRightHandSide());
    }

    public Map<String, Integer> getVariables(Expression expression, Map<String, Object> variableMap) {
        //Get just the variables from the map
        HashMap<String, Integer> varMap = new HashMap<>();
        if (variableMap != null) {
            for (String key : variableMap.keySet()) {
                if (variableMap.get(key) instanceof Integer) {
                    varMap.put(key, (Integer) variableMap.get(key));
                }
            }
        }
        //Map from used variables to list of variables and their values
        Map<String,Integer> newVarMap = new HashMap<>();
        collectVariables(expression).distinct().forEach(var -> {
            if (variableMap == null) newVarMap.put(var,null);
            if (varMap.containsKey(var))
                newVarMap.put(var,varMap.get(var));
        });
        return newVarMap;
    }
}
