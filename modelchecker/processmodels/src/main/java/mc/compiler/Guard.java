package mc.compiler;

import com.google.common.base.Objects;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.rits.cloning.Cloner;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.AutomatonNode;
import mc.util.Location;
import mc.util.expr.ExpressionEvaluator;
import mc.util.expr.ExpressionPrinter;
import mc.util.expr.VariableCollector;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static mc.util.expr.Expression.*;

// build all setters?
public class Guard implements Serializable {

  // this is the boolean guard for input to Z3
  BoolExpr guard;

  /**
   * the field variables is actually an evaluation, a variable 2 literal mapping
   * used for variables that are not symbolic (root evaluation held on Petrinet)
   */
  Map<String, Integer> variables = new HashMap<>();

  // next Assignment "i:=2"  Only needed for symbolic variables
  List<String> next = new ArrayList<>();

  // nextMap Assignment $v5 -> $i-1,  $v6 -> $j+1 Only needed for hidden variables"
  Map<String, String> nextMap = new HashMap<>();

  private boolean shouldDisplay = false;
  private Set<String> symbolicVariables = new HashSet<>();
  private Map<String, Expr> globalVariableMap = new HashMap<>();

  public Guard(BoolExpr guard, Map<String, Integer> variables, List<String> next, Map<String, String> nextMap, boolean shouldDisplay, Set<String> symbolicVariables, Map<String, Expr> globalVariableMap) {
    this.guard = guard;
    this.variables = variables;
    this.next = next;
    this.nextMap = nextMap;
    this.shouldDisplay = shouldDisplay;
    this.symbolicVariables = symbolicVariables;
    this.globalVariableMap = globalVariableMap;
  }

  public Guard() {
  }

  /**
   * Get the guard as a string, used for serialization.
   *
   * @return The guard as a string, or an empty string if none exists.
   */
  public String getGuardStr() {
    if (guard == null ){ //|| symbolicVariables.isEmpty()) {
      return "";
    }
    return rmPrefix(ExpressionPrinter.printExpression(guard, Collections.emptyMap()));

  }
  public String getAssStr() {
    String out;
    if (nextMap.size()==0) {
      out = "";
    } else{
      out = nextMap.keySet().stream().map(x->x+":="+nextMap.get(x)+",").collect(Collectors.joining());
    }
    return out;

  }


  public String myShortString() {
    return getGuardStr()+" "+ getAssStr();
  }
  public String myString() {
    StringBuilder sb = new StringBuilder();

    sb.append( getGuardStr()+" "+ getAssStr());
    if (variables.size()>0) {
      sb.append(" var = ");
      for (String s : variables.keySet()) {
        sb.append(s + "=" + variables.get(s).toString());
      }
    }
    if (next.size()>0) {
      sb.append(next.stream().reduce("", (x, y) -> x + " " + y + " "));
    }
    if (nextMap.size()>0) {
      sb.append(" nextMap = ");
      for (String s : nextMap.keySet()) {
        sb.append( s + " " + nextMap.get(s) + " ");
      }
    }
    return sb.toString();
  }

  /**
   * Get the
   *
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  public String getSymbolicGuardStr() throws CompilationException, InterruptedException {
    if (guard == null || symbolicVariables.isEmpty()) {
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
      return symbolicVariables.contains(ex.toString().substring(1));
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
   *  NEVER USED!
   * @return The variable list as a string, or an empty string if none exists.
   * SIDE EFFECT! removes hidden variables from variables
   */
  public String getVarStr() {
    if (guard == null) {
      return "";
    }
    Set<String> vars = new VariableCollector().getVariables(guard, null).keySet();
    variables.keySet().removeIf(s -> !vars.contains(s.substring(1)));
    if (variables.isEmpty() || symbolicVariables.isEmpty()) {
      return "";
    }
    variables.keySet().removeAll(symbolicVariables);
    StringBuilder builder = new StringBuilder();
    for (Map.Entry<String, Integer> entry : variables.entrySet()) {
      builder.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
    }
    String str = builder.toString();
    return rmPrefix(str.substring(0, str.length() - 1));
  }

  /**
   * Only called in ModelView
   *
   * @return The next variable list as a string, or an empty string if none exists.
   */
  public String getNextStr() {
    if (next.isEmpty() || symbolicVariables.isEmpty()) {
      return "";
    }

    return rmPrefix(String.join(",", next)).split("%")[0];
  }

  /**
   * Parse an identifier and turn it into a list of variable assignments.
   *
   * @param identifier        The identifier  Could be "C[$i][j+1][4]"
   * @param globalVariableMap   $v0->$y+1,  $v1->3,
   * @param identMap          A map from identifiers to a list of the variables in them
   *                          (L[$i][1][$j] = L -> [$i,$j])
   * Method changes state of Guard .nextMap  and .next
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

      } else if (var.matches("\\d+")) {
        String varName = varNames.get(vars.indexOf(var));
        next.removeIf(s -> s.matches(varName + "\\W.*"));
        next.add(rmPrefix(varName + ":=" + var));
        nextMap.put(var, varName);
      }
    }
/*System.out.print("parseNext next as string"+ next.toString()+"\n");
    for(String k: nextMap.keySet()) {
      System.out.print(" "+k+" -> "+ nextMap.get(k)+", ");
    } System.out.print(" nextMap end \n");
System.out.print("parseNext "+ myString()+"\n"); */
  }

  private static String rmPrefix(String str) {
    return str.replace("$", "");
  }

  /**
   *
   * @param guard
   */
  public void mergeWith(Guard guard) {
    if (guard.guard != null) {
      this.guard = guard.guard;
    }
      this.variables.putAll(guard.variables);
      //Remove any existing variables
      this.next.removeIf(t -> next.stream()
        .anyMatch(s -> Pattern.compile(s.split("\\W")[0] + "\\W").matcher(t).find()));
      this.next.addAll(guard.next);
      this.nextMap.putAll(guard.getNextMap());
      this.symbolicVariables.addAll(guard.symbolicVariables);
      symbolicVariables.removeAll(variables.keySet());

  }
  public boolean hasData() {
    return guard != null || !variables.isEmpty() || !next.isEmpty();
  }



  /**
   *  Assume BoolExpr is immutable
   * @return   a deep copy
   */
  public Guard copy() {
    Cloner cloner = new Cloner();
    cloner.dontClone(BoolExpr.class);
    return cloner.deepClone(this);

  }

  public Guard(BoolExpr guard, Map<String, Integer> variables, Set<String> symbolicVariables) {
    setGuard(guard);
    setVariables(variables);
    setSymbolicVariables(symbolicVariables);
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
    if (symbolicVariables.isEmpty() && guard1.symbolicVariables.isEmpty()) {
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
    return Objects.hashCode(variables, next, nextMap, shouldDisplay, symbolicVariables);
  }

  public void setGuard(BoolExpr guard) {
    this.guard = guard;
  }

  public void setVariables(Map<String, Integer> variables) {
    this.variables = variables;
  }

  public void setNext(List<String> next) {
    this.next = next;
  }

  public void setNextMap(Map<String, String> nextMap) {
    this.nextMap = nextMap;
  }

  public void setShouldDisplay(boolean shouldDisplay) {
    this.shouldDisplay = shouldDisplay;
  }

  public void setSymbolicVariables(Set<String> symbolicVariables) {
    this.symbolicVariables = symbolicVariables;
  }

  public String toString() {
    return "Guard(guard=" + this.guard + ", variables=" + this.variables + ", next=" + this.next + ", nextMap=" + this.nextMap + ", shouldDisplay=" + this.shouldDisplay + ", symbolicVariables=" + this.symbolicVariables + ", globalVariableMap=" + this.globalVariableMap + ")";
  }

  public BoolExpr getGuard() {
    return this.guard;
  }

  public Map<String, Integer> getVariables() {
    return this.variables;
  }

  public List<String> getNext() {
    return this.next;
  }

  public Map<String, String> getNextMap() {
    return this.nextMap;
  }

  public boolean isShouldDisplay() {
    return this.shouldDisplay;
  }

  public Map<String, Expr> getGlobalVariableMap() {
    return this.globalVariableMap;
  }

  public void setGlobalVariableMap(Map<String, Expr> globalVariableMap) {
    this.globalVariableMap = globalVariableMap;
  }
}
