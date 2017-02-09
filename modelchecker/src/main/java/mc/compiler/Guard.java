package mc.compiler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionPrinter;

import java.io.Serializable;
import java.util.*;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Guard implements Serializable{
    //Don't serialize this data as we serialize the below methods instead.
    @Getter(onMethod = @__(@JsonIgnore))
    Expression guard;
    @Getter(onMethod = @__(@JsonIgnore))
    Map<String,Integer> variables = new HashMap<>();
    @Getter(onMethod = @__(@JsonIgnore))
    List<String> next = new ArrayList<>();
    @Getter(onMethod = @__(@JsonIgnore))
    Map<String,String> nextMap = new HashMap<>();
    @Getter
    private boolean shouldDisplay = false;

    /**
     * Get the guard as a string, used for serialization.
     * @return The guard as a string, or an empty string if none exists.
     */
    public String getGuardStr() {
        if (guard == null) return "";
        return rm$(new ExpressionPrinter().printExpression(guard, Collections.emptyMap()));
    }
    /**
     * Get the variable list as a string, used for serialization.
     * @return The variable list as a string, or an empty string if none exists.
     */
    public String getVarStr() {
        if (variables.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (String var: variables.keySet()) {
            builder.append(var+"="+variables.get(var)+",");
        }
        String str = builder.toString();
        return rm$(str.substring(0,str.length()-1));
    }
    /**
     * Get the next variable list as a string, used for serialization.
     * @return The next variable list as a string, or an empty string if none exists.
     */
    public String getNextStr() {
        if (next.isEmpty()) return "";
        return rm$(String.join(",",next));
    }

    /**
     * Parse an identifier and turn it into a list of variable assignments
     * @param identifier The identifier
     * @param globalVariableMap The global variable map
     * @param identMap A map from identifiers to a list of the variables in them (L[$i] = L -> [$i]
     */
    public void parseNext(String identifier, Map<String, Expression> globalVariableMap, Map<String, List<String>> identMap) {
        //Check that there are actually variables in the identifier
        if (!identifier.contains("[")) return;
        //Get a list of all variables
        List<String> vars = new ArrayList<>(Arrays.asList(identifier.replace("]","").split("\\[")));
        //Remove the actual identifier from the start.
        List<String> varNames = identMap.get(vars.remove(0));
        //Loop through the ranges and variables
        for (String var : vars) {
            if (globalVariableMap.containsKey(var)) {
                String printed = new ExpressionPrinter().printExpression(globalVariableMap.get(var));
                printed = printed.substring(1, printed.length() - 1);
                nextMap.put(var, printed);
                next.add(rm$(printed));
                break;
            } else if (var.matches("\\d+")) {
                String varName = varNames.get(vars.indexOf(var));
                next.add(rm$(varName + ":=" + var));
                nextMap.put(var, varName);
            }
        }
        //Replace symbols with their assignment counterparts.
        for (int i = 0; i < next.size(); i++) {
            String nextVar = next.get(i);
            nextVar = nextVar.replaceAll(operators,"$1=");
            next.set(i,nextVar);
        }
    }
    private static String rm$(String str) {
        return str.replace("$","");
    }

    public void mergeWith(Guard guard) {
        if (guard.guard != null) this.guard = guard.guard;
        this.variables.putAll(guard.variables);
        this.next.addAll(guard.next);
    }
    @JsonIgnore
    boolean hasData() {
        return guard != null || !variables.isEmpty() || !next.isEmpty();
    }
    private static String operators = "(&|\\^|<<|>>|\\+|-|\\*|/|%)";

}
