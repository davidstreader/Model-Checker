package mc.compiler;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import mc.exceptions.CompilationException;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import mc.util.expr.*;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Guard implements Serializable{
    //Don't serialize these getters as we serialize the below methods instead.
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
    private Set<String> hiddenVariables = new HashSet<>();
    /**
     * Get the guard as a string, used for serialization.
     * @return The guard as a string, or an empty string if none exists.
     */
    public String getGuardStr() throws CompilationException {
        if (guard == null || hiddenVariables.isEmpty()) return "";
        return rm$(new ExpressionPrinter().printExpression(guard, Collections.emptyMap()));
    }
    public String getHiddenGuardStr() throws CompilationException {
        if (guard == null || hiddenVariables.isEmpty()) return "";
        List<Expression> andList = new ArrayList<>();
        collectAnds(andList,guard);
        //If there are no ands in the expression, use the root guard.
        if (andList.isEmpty()) andList.add(guard);
        andList.removeIf(s -> !containsHidden(s));
        if (andList.isEmpty()) return "";
        Expression combined = andList.remove(0);
        while (!andList.isEmpty()) {
            combined = new AndOperator(combined,andList.remove(0));
        }
        return rm$(new ExpressionPrinter().printExpression(combined, Collections.emptyMap()));
    }
    private void collectAnds(List<Expression> andList, Expression ex) {
        if (ex instanceof AndOperator) {
            andList.add(((AndOperator) ex).getLeftHandSide());
            andList.add(((AndOperator) ex).getRightHandSide());
        }
        if (ex instanceof UnaryOperator) collectAnds(andList,((UnaryOperator) ex).getRightHandSide());
        if (ex instanceof BinaryOperator) {
            collectAnds(andList,((BinaryOperator) ex).getLeftHandSide());
            collectAnds(andList,((BinaryOperator) ex).getRightHandSide());
        }
    }

    private boolean containsHidden(Expression ex) {
        //If there is an and inside this expression, then don't check its variables as it is added on its own.
        if (ex instanceof AndOperator) return false;
        if (ex instanceof UnaryOperator) return containsHidden(((UnaryOperator) ex).getRightHandSide());
        if (ex instanceof BinaryOperator) {
            return containsHidden(((BinaryOperator) ex).getRightHandSide()) || containsHidden(((BinaryOperator) ex).getLeftHandSide());
        }
        //Substring away the $ as the hidden map does not have them.
        return ex instanceof VariableOperand && hiddenVariables.contains(((VariableOperand) ex).getValue().substring(1));
    }
    /**
     * Get the variable list as a string, used for serialization.
     * @return The variable list as a string, or an empty string if none exists.
     */
    public String getVarStr() {
        Set<String> vars = new VariableCollector().getVariables(guard,null).keySet();
        variables.keySet().removeIf(s -> !vars.contains(s.substring(1)));
        if (variables.isEmpty() || hiddenVariables.isEmpty()) return "";
        variables.keySet().removeAll(hiddenVariables);
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
        if (next.isEmpty() || hiddenVariables.isEmpty()) return "";
        return rm$(String.join(",",next));
    }

    /**
     * Parse an identifier and turn it into a list of variable assignments
     * @param identifier The identifier
     * @param globalVariableMap The global variable map
     * @param identMap A map from identifiers to a list of the variables in them (L[$i] = L -> [$i])
     */
    public void parseNext(String identifier, Map<String, Expression> globalVariableMap, Map<String, List<String>> identMap) throws CompilationException {
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
                String variable = new VariableCollector().getVariables(globalVariableMap.get(var),null).keySet().iterator().next();
                printed = printed.substring(1, printed.length() - 1);
                nextMap.put(var, printed);
                next.add(variable+":="+rm$(printed));
                break;
            } else if (var.matches("\\d+")) {
                String varName = varNames.get(vars.indexOf(var));
                next.removeIf(s -> s.matches(varName+"\\W.*"));
                next.add(rm$(varName + ":=" + var));
                nextMap.put(var, varName);
            }
        }
    }
    private static String rm$(String str) {
        return str.replace("$","");
    }

    public void mergeWith(Guard guard) {
        if (guard.guard != null) this.guard = guard.guard;
        this.variables.putAll(guard.variables);
        //Remove any existing variables
        this.next.removeIf(t -> next.stream().anyMatch(s -> Pattern.compile(s.split("\\W")[0]+"\\W").matcher(t).find()));
        this.next.addAll(guard.next);
        this.hiddenVariables.addAll(guard.hiddenVariables);
        hiddenVariables.removeAll(variables.keySet());
    }
    @JsonIgnore
    boolean hasData() {
        return guard != null || !variables.isEmpty() || !next.isEmpty();
    }
    public Guard copy() {
        return new Guard(guard,variables,next,nextMap,shouldDisplay,hiddenVariables);
    }
    public Guard(Expression guard, Map<String,Integer> variables, Set<String> hiddenVariables) {
        setGuard(guard);
        setVariables(variables);
        setHiddenVariables(hiddenVariables);
    }

    public boolean equals(Object o, Map<String, Expression> replacements, AutomatonNode first, AutomatonNode second) throws CompilationException {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Guard guard1 = (Guard) o;
        if (hiddenVariables.isEmpty() && guard1.hiddenVariables.isEmpty()) return true;
        Expression exp1 = ExpressionSimplifier.substitute(guard.copy(), replacements);
        Expression exp2 = ExpressionSimplifier.substitute(guard1.guard.copy(), replacements);
        if (!NodeUtils.findLoopsAndPathToRoot(first).map(NodeUtils::collectVariables)
            .map(s -> ExpressionSimplifier.substitute(exp1.copy(),s))
            .allMatch(s->ExpressionSimplifier.isSolveable(s,Collections.emptyMap()))) return false;
        if (!NodeUtils.findLoopsAndPathToRoot(second).map(NodeUtils::collectVariables)
            .map(s -> ExpressionSimplifier.substitute(exp2.copy(),s))
            .allMatch(s->ExpressionSimplifier.isSolveable(s,Collections.emptyMap()))) return false;
        return ExpressionSimplifier.isSolveable(new EqualityOperator(exp1,exp2),Collections.emptyMap());
    }
    @Override
    public int hashCode() {
        int result = guard != null ? guard.hashCode() : 0;
        result = 31 * result + (variables != null ? variables.hashCode() : 0);
        result = 31 * result + (next != null ? next.hashCode() : 0);
        result = 31 * result + (nextMap != null ? nextMap.hashCode() : 0);
        result = 31 * result + (shouldDisplay ? 1 : 0);
        result = 31 * result + (hiddenVariables != null ? hiddenVariables.hashCode() : 0);
        return result;
    }
}
