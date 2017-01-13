package net.modelsolver;

import lombok.ToString;
import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import java.util.*;
import java.util.regex.Pattern;

/**
 * A guard, automatically created from JSON via GSON.
 */
@ToString
class Guard {
  /**
   * Output variables
   */
  String[] next;
  /**
   * Input variables
   */
  String[] variables;
  /**
   * The displayed guard, this shows all variables, even known ones.
   */
  String guard;
  /**
   * The processed guard, this contains all known variables already substituted in.
   */
  String procGuard;
  private Map<String,IntegerFormula> varMap = new HashMap<>();
  public List<BooleanFormula> getConstraints(FormulaManager mgr) {
    IntegerFormulaManager imgr = mgr.getIntegerFormulaManager();
    //We need to load in all variables as the guard may reference them
    for (String var : variables) {
      String[] split = var.split("=");
      varMap.put(split[0],imgr.makeNumber(Integer.parseInt(split[1])));
    }
    return parseGuards(procGuard,imgr);
  }

  /**
   * Return the first Pattern that matches string, or null if none is found.
   * @param string the string to find a match for
   * @param map a map with Patterns for keys
   * @return A matching Pattern, or null if none is found
   */
  private Pattern findInMap(String string, Map<Pattern, ?> map) {
    for (Pattern pattern: map.keySet()) {
      if (pattern.matcher(string).find()) {
        return pattern;
      }
    }
    return null;
  }

  /**
   * Convert a guard string to a list of BooleanFormulae
   * @param expr the guard string
   * @param imgr An integerFormulaManager
   * @return a list of all parsed guards
   */
  private List<BooleanFormula> parseGuards(String expr, IntegerFormulaManager imgr) {
    List<BooleanFormula> list = new ArrayList<>();
    Stack<IntegerFormula> stack = new Stack<>();
    //Use a ShuntingYard algorithm to convert the expression to postfix, then parse the postfix notation
    String[] tokens = ShuntingYard.postfix(expr).split("\\s+");
    for (String token: tokens) {
      //We can just not deal with this, and then we are returned a list of all guards.
      if (Objects.equals(token, "&&")) continue;
      Pattern tmp = findInMap(token, operationMap);
      Pattern tmp2 = findInMap(token, comparisonMap);
      if (tmp != null) {
        //We have found an operation.
        //Apply the operation to its args then push the result to the stack
        stack.push(operationMap.get(tmp).apply(imgr, stack.pop(), stack.pop()));
      } else if (tmp2 != null) {
        //Since AND is such a low priority, it will be as far to the right as possible.
        //Because of this, we can just take its args from the stack and work out the guard.
        //Because of how things are parsed, we need to do comparisons in reverse.
        IntegerFormula right = stack.pop();
        IntegerFormula left = stack.pop();
        list.add(comparisonMap.get(tmp2).compare(imgr, left,right));
      } else {
        stack.push(parseForumla(imgr,token));
      }
    }
    return list;
  }


  /**
   * Parse a string as either a variable or a value
   * @param imgr An IntegerForumlaManager
   * @param formula the formula to parse
   * @return an IntegerFormula either representing a variable or a value
   */
  private IntegerFormula parseForumla(IntegerFormulaManager imgr, String formula) {
    //Directly make a number for digits
    if (formula.matches("^\\d*$")) {
      return imgr.makeNumber(Integer.parseInt(formula));
    }
    //Return existing variables
    if (varMap.containsKey(formula)) return varMap.get(formula);
    //Create a new variable
    IntegerFormula var = imgr.makeVariable(formula);
    varMap.put(formula,var);
    return var;
  }

  /**
   * A map from patterns to match all the different operations to their respective functions
   */
  private static Map<Pattern,Operation> operationMap = new HashMap<Pattern,Operation>(){{
    //Some of the regexps below offer an optional equals sign, so they can parse += and +.
    put(Pattern.compile("\\+=?"), NumeralFormulaManager::add);
    put(Pattern.compile("-=?"), NumeralFormulaManager::subtract);
    put(Pattern.compile("%"), NumeralFormulaManager::modulo);
    put(Pattern.compile("\\*=?"), NumeralFormulaManager::multiply);
    //Parse both back slash and forward slash as divide.
    put(Pattern.compile("(\\\\|\\/)=?"), NumeralFormulaManager::divide);
  }};
  /**
   * A map from patterns to match all the different comparisons to their respective functions
   */
  private static Map<Pattern,Comparison> comparisonMap = new HashMap<Pattern,Comparison>(){
    {
      put(Pattern.compile("<"), NumeralFormulaManager::lessThan);
      put(Pattern.compile(">"), NumeralFormulaManager::greaterThan);
      put(Pattern.compile("<="), NumeralFormulaManager::lessOrEquals);
      put(Pattern.compile(">="), NumeralFormulaManager::greaterOrEquals);
      put(Pattern.compile("=="), NumeralFormulaManager::equal);
    }};

  /**
   * A formula that takes in two IntegerFormulas and outputs an operation on them
   */
  private interface Operation {
    IntegerFormula apply(IntegerFormulaManager mgr, IntegerFormula a, IntegerFormula b);
  }

  /**
   * A formula that takes in two IntegerFormulas and performs a comparison between them
   */
  private interface Comparison {
    BooleanFormula compare(IntegerFormulaManager mgr, IntegerFormula a, IntegerFormula b);
  }
}
