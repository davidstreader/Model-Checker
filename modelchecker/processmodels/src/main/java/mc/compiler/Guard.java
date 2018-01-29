package mc.compiler;

import static mc.util.expr.Expression.equate;
import static mc.util.expr.Expression.getContextFrom;
import static mc.util.expr.Expression.isSolvable;
import static mc.util.expr.Expression.substitute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.AutomatonNode;
import mc.util.Location;
import mc.util.expr.ExpressionEvaluator;
import mc.util.expr.ExpressionPrinter;
import mc.util.expr.VariableCollector;

@Setter
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class Guard implements Serializable {
  //Don't serialize these getters as we serialize the below methods instead.
  @Getter(onMethod = @__(@JsonIgnore))
  BoolExpr guard;
  @Getter(onMethod = @__(@JsonIgnore))
  Map<String, Integer> variables = new HashMap<>();
  @Getter(onMethod = @__(@JsonIgnore))
  List<String> next = new ArrayList<>();
  @Getter(onMethod = @__(@JsonIgnore))
  Map<String, String> nextMap = new HashMap<>();
  @Getter
  private boolean shouldDisplay = false;
  private Set<String> hiddenVariables = new HashSet<>();

  /**
   * Get the guard as a string, used for serialization.
   *
   * @return The guard as a string, or an empty string if none exists.
   */
  public String getGuardStr() throws CompilationException {
    if (guard == null || hiddenVariables.isEmpty()) {
      return "";
    }
    return rmPrefix(ExpressionPrinter.printExpression(guard, Collections.emptyMap()));

  }
  public String myString(){
    String var = "var = ";
    for(String s: variables.keySet()){
      var = var+s+"="+variables.get(s).toString();
    }
    String nxt = next.stream().reduce("",(x,y)-> x+" "+y+" ");
    String nm = "nextMap = ";
    for(String s: nextMap.keySet()){
      nm = nm+s+" "+nextMap.get(s)+" ";
    }
    return " guard "+ guard +" "+ var + nxt+ nm;
  }
  /**
   * Get the
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  public String getHiddenGuardStr() throws CompilationException, InterruptedException {
    if (guard == null || hiddenVariables.isEmpty()) {
      return "";
    }
    List<BoolExpr> andList = new ArrayList<>();
    collectAnds(andList, guard);
    //If there are no ands in the expression, use the root guard.
    if (andList.isEmpty()) {
      andList.add(guard);
    }
    andList.removeIf(s -> !containsHidden(s));
    if (andList.isEmpty()) {
      return "";
    }
    Expr combined = getContextFrom(andList.get(0)).mkAnd(andList.toArray(new BoolExpr[0]));
    return rmPrefix(ExpressionPrinter.printExpression(combined, Collections.emptyMap()));
  }

  private void collectAnds(List<BoolExpr> andList, Expr ex) {

    if (ex.isAnd()) {
      for (Expr expr : ex.getArgs()) {
        andList.add((BoolExpr) expr);
      }
    } else {
      for (Expr expr : ex.getArgs()) {
        collectAnds(andList, expr);
      }
    }
  }

  private boolean containsHidden(Expr ex) {
    // If there is an 'and' inside this expression
    // then don't check its variables as it is added on its own.
    if (ex.isAnd()) {
      return false;
    }
    if (ex.isConst()) {
      return hiddenVariables.contains(ex.toString().substring(1));
    }
    for (Expr expr : ex.getArgs()) {
      if (containsHidden(expr)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the variable list as a string, used for serialization.
   *
   * @return The variable list as a string, or an empty string if none exists.
   */
  public String getVarStr() {
    if (guard == null) {
      return "";
    }
    Set<String> vars = new VariableCollector().getVariables(guard, null).keySet();
    variables.keySet().removeIf(s -> !vars.contains(s.substring(1)));
    if (variables.isEmpty() || hiddenVariables.isEmpty()) {
      return "";
    }
    variables.keySet().removeAll(hiddenVariables);
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, Integer> entry : variables.entrySet()) {
      builder.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
    }
    String str = builder.toString();
    return rmPrefix(str.substring(0, str.length() - 1));
  }

  /**
   * Get the next variable list as a string, used for serialization.
   *
   * @return The next variable list as a string, or an empty string if none exists.
   */
  public String getNextStr() {
    if (next.isEmpty() || hiddenVariables.isEmpty()) {
      return "";
    }
    return rmPrefix(String.join(",", next));
  }

  /**
   * Parse an identifier and turn it into a list of variable assignments.
   *
   * @param identifier        The identifier
   * @param globalVariableMap The global variable map
   * @param identMap          A map from identifiers to a list of the variables in them
   *                          (L[$i] = L -> [$i])
   */
  public void parseNext(String identifier, Map<String, Expr> globalVariableMap,
                        Map<String, List<String>> identMap, Location location)
      throws CompilationException, InterruptedException {
    //Check that there are actually variables in the identifier
    if (!identifier.contains("[")) {
      return;
    }
    //Get a list of all variables
    List<String> vars = new ArrayList<>(Arrays.asList(
        identifier.replace("]", "").split("\\[")));
    //Remove the actual identifier from the start.
    String ident = vars.remove(0);
    List<String> varNames = identMap.get(ident);
    if (varNames == null) {
      throw new CompilationException(Guard.class, "Unable to find identifier: " + ident, location);
    }
    //Loop through the ranges and variables
    for (String var : vars) {
      if (globalVariableMap.containsKey(var)) {
        String printed = ExpressionPrinter.printExpression(globalVariableMap.get(var));
        String variable;
        if (new ExpressionEvaluator().isExecutable(globalVariableMap.get(var))) {
          variable = varNames.get(vars.indexOf(var));
        } else {
          variable = new VariableCollector().getVariables(
              globalVariableMap.get(var), null).keySet().iterator().next();
          //Strip extra brackets
          printed = printed.substring(1, printed.length() - 1);
        }
        nextMap.put(var, printed);
        next.add(variable + ":=" + rmPrefix(printed));
        break;
      } else if (var.matches("\\d+")) {
        String varName = varNames.get(vars.indexOf(var));
        next.removeIf(s -> s.matches(varName + "\\W.*"));
        next.add(rmPrefix(varName + ":=" + var));
        nextMap.put(var, varName);
      }
    }
  }

  private static String rmPrefix(String str) {
    return str.replace("$", "");
  }

  public void mergeWith(Guard guard) {
    if (guard.guard != null) {
      this.guard = guard.guard;
    }
    this.variables.putAll(guard.variables);
    //Remove any existing variables
    this.next.removeIf(t -> next.stream()
        .anyMatch(s -> Pattern.compile(s.split("\\W")[0] + "\\W").matcher(t).find()));
    this.next.addAll(guard.next);
    this.hiddenVariables.addAll(guard.hiddenVariables);
    hiddenVariables.removeAll(variables.keySet());
  }

  @JsonIgnore
  public boolean hasData() {
    return guard != null || !variables.isEmpty() || !next.isEmpty();
  }

  public Guard copy() {
    return new Guard(guard, variables, next, nextMap, shouldDisplay, hiddenVariables);
  }

  public Guard(BoolExpr guard, Map<String, Integer> variables, Set<String> hiddenVariables) {
    setGuard(guard);
    setVariables(variables);
    setHiddenVariables(hiddenVariables);
  }

  public boolean equals(Object o, Map<String, Expr> replacements, AutomatonNode first,
                        AutomatonNode second, Context context) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Guard guard1 = (Guard) o;
    if (hiddenVariables.isEmpty() && guard1.hiddenVariables.isEmpty()) {
      return true;
    }
    Expr exp1 = substitute(guard, replacements, context);
    Expr exp2 = substitute(guard1.guard, replacements, context);
    return NodeUtils.findLoopsAndPathToRoot(first)
        .map(edges -> NodeUtils.collectVariables(edges, context))
        .map(s -> substitute(exp1, s, context))
        .allMatch(s -> isSolvable((BoolExpr) s, Collections.emptyMap(), context))
        && NodeUtils.findLoopsAndPathToRoot(second)
        .map(edges -> NodeUtils.collectVariables(edges, context))
        .map(s -> substitute(exp2, s, context))
        .allMatch(s -> isSolvable((BoolExpr) s, Collections.emptyMap(), context))
        && equate(this, guard1, context);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(variables, next, nextMap, shouldDisplay, hiddenVariables);
  }
}
