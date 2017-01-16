package net.modelsolver;

import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormulaManager;

import java.util.*;
import java.util.regex.Pattern;

import static org.sosy_lab.java_smt.api.NumeralFormula.*;

/**
 * Created by sanjay on 16/01/2017.
 */
public class FormulaUtils {
  /**
   * Convert a guard string to a list of BooleanFormulae
   * @param expr the guard string
   * @param imgr An integerFormulaManager
   * @return a list of all parsed guards
   */
  static List<BooleanFormula> parseGuards(String expr, IntegerFormulaManager imgr, Map<String, IntegerFormula> varMap) {
    List<BooleanFormula> list = new ArrayList<>();
    Stack<IntegerFormula> stack = new Stack<>();
    //Use a ShuntingYard algorithm to convert the expression to infixToPostfix, then parse the infixToPostfix notation
    String[] tokens = OperatorUtils.infixToPostfix(expr).split("\\s+");
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
        stack.push(parseForumla(imgr,token,varMap));
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
  public static IntegerFormula parseForumla(IntegerFormulaManager imgr, String formula, Map<String,IntegerFormula> varMap) {
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

  /**
   * Return the first Pattern that matches string, or null if none is found.
   * @param string the string to find a match for
   * @param map a map with Patterns for keys
   * @return A matching Pattern, or null if none is found
   */
  private static Pattern findInMap(String string, Map<Pattern, ?> map) {
    for (Pattern pattern: map.keySet()) {
      if (pattern.matcher(string).find()) {
        return pattern;
      }
    }
    return null;
  }
}
