package mc.compiler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import mc.compiler.ast.IndexNode;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionPrinter;

import java.io.Serializable;
import java.util.*;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Guard implements Serializable{
    //Dont serialize this data as we serialize the below methods instead.
    @Getter(onMethod = @__(@JsonIgnore))
    Expression guard;
    @Getter(onMethod = @__(@JsonIgnore))
    Map<String,Integer> variables = new HashMap<>();
    @Getter(onMethod = @__(@JsonIgnore))
    List<String> next = new ArrayList<>();
    public String getGuardStr() {
        if (guard == null) return "";
        return rm$(new ExpressionPrinter().printExpression(guard, Collections.emptyMap()));
    }
    public String getVarStr() {
        if (variables.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (String var: variables.keySet()) {
            builder.append(var+"="+variables.get(var)+",");
        }
        String str = builder.toString();
        return rm$(str.substring(0,str.length()-1));
    }
    public String getNextStr() {
        if (next.isEmpty()) return "";
        return rm$(String.join(",",next));
    }

    /**
     * Parse an identifier and turn it into a list of variable assignments
     * @param identifier The identifier
     * @param globalVariableMap The global variable map
     * @param ranges The ranges for the current identifier
     */
    public void parseNext(String identifier, Map<String, String> globalVariableMap, List<IndexNode> ranges) {
        if (!identifier.contains("[")) return;
        List<String> vars = new ArrayList<>(Arrays.asList(identifier.replace("]","").split("\\[")));
        //Remove the actual identifier from the start.
        vars.remove(0);
        for (int i = 0; i < ranges.size(); i++) {
            IndexNode range = ranges.get(i);
            String var = vars.get(i);
            boolean found = false;
            for (String gVar : globalVariableMap.keySet()) {
                if (identifier.contains(gVar)) {
                    next.add(rm$(globalVariableMap.get(gVar)));
                    found = true;
                    break;
                }
            }
            if (!found) {
                next.add(rm$(range.getVariable()+"="+var));
            }
        }
        for (int i = 0; i < next.size(); i++) {
            String nextVar = next.get(i);
            nextVar = nextVar.replace("=",":=");
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
    public boolean hasData() {
        return guard != null || !variables.isEmpty() || !next.isEmpty();
    }
    private static String operators = "(&|\\^|<<|>>|\\+|-|\\*|/|%)";


}
