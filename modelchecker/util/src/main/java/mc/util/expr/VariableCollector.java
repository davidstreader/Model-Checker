package mc.util.expr;

import com.microsoft.z3.Expr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class VariableCollector {

    private Stream<String> collectVariables(Expr expression){
        if(expression.isConst()){
            return Stream.of(expression.toString());
        }
        return Arrays.stream(expression.getArgs()).map(this::collectVariables).flatMap(s->s);
    }

    public Map<String, Integer> getVariables(Expr expression, Map<String, Object> variableMap) {
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
