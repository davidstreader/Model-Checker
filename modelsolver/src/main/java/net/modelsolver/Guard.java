package net.modelsolver;

import lombok.ToString;
import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sanjay on 13/01/2017.
 */
@ToString
public class Guard {
  String[] next;
  String[] variables;
  String guard;
  private HashMap<String,IntegerFormula> varMap = new HashMap<>();
  public List<BooleanFormula> getConstraints(FormulaManager mgr) {
    IntegerFormulaManager imgr = mgr.getIntegerFormulaManager();
    BooleanFormulaManager bmgr = mgr.getBooleanFormulaManager();
    processVariables(imgr);
    return parseGuards(guard,imgr);
  }

  private void processVariables(IntegerFormulaManager imgr) {
    for (String var : variables) {
      String[] split = var.split("=");
      varMap.put(split[0],imgr.makeNumber(Integer.parseInt(split[1])));
    }
  }
  private IntegerFormula convertVariable(String var, IntegerFormulaManager imgr) {
    String combined = "";
    for (Pattern pattern: convertMap.keySet()) {
      combined+="|"+pattern.pattern();
    }
    combined = combined.substring(1);
    var = var.replaceAll(combined,"~$0~");
    String[] tokens = var.split("~");
    //(1+2 < 2 && t < 3)
    IntegerFormula formula = parseForumla(imgr,tokens[0]);
    for (int i = 1; i<tokens.length;i+=2) {
      Pattern cons = findInMap(tokens[i],convertMap);
      formula = convertMap.get(cons).convert(imgr,formula,parseForumla(imgr, tokens[i+1]));
    }
    return formula;
  }

  private Pattern findInMap(String token, HashMap<Pattern, ?> map) {
    return map.keySet().stream().filter(sym -> sym.matcher(token).find()).findFirst().get();
  }

  private List<BooleanFormula> parseGuards(String guard, IntegerFormulaManager imgr) {
    List<BooleanFormula> list = new ArrayList<>();
    System.out.println(ShuntingYard.postfix(guard));
    String[] guards = guard.split("&&");
    for (String g : guards) {
      //list.add(parseGuard(g,imgr));
    }
    return list;
  }
  /**
   * Parse a guard (Use parseGuards with combined guards)
   * @param guard the guard
   * @param imgr the IntegerFormulaManager
   * @return
   */
  private BooleanFormula parseGuard(String guard, IntegerFormulaManager imgr) {
    String combined = "";
    for (Pattern pattern: constMap.keySet()) {
      combined+="|"+pattern.pattern();
    }
    combined = combined.substring(1);
    guard = guard.replaceAll(combined,"~$0~");
    System.out.println(guard);
    String[] tokens = guard.split("~");
    IntegerFormula left = convertVariable(tokens[0], imgr);
    IntegerFormula right = convertVariable(tokens[2], imgr);
    Pattern cons = findInMap(tokens[1],constMap);
    return constMap.get(cons).convert(imgr,left,right);
  }
  private IntegerFormula parseForumla(IntegerFormulaManager imgr, String formula) {
    if (formula.matches("^\\d*$")) {
      return imgr.makeNumber(Integer.parseInt(formula));
    }
    if (varMap.containsKey(formula)) return varMap.get(formula);
    IntegerFormula var = imgr.makeVariable(formula);
    varMap.put(formula,var);
    return var;
  }

  private static HashMap<Pattern,PatternFormula> convertMap = fillMap();
  private static HashMap<Pattern,PatternConstraint> constMap = fillConst();
  private static HashMap<Pattern, PatternFormula> fillMap() {
    HashMap<Pattern,PatternFormula> convertMap = new HashMap<>();
    convertMap.put(Pattern.compile("\\+=?"), NumeralFormulaManager::add);
    convertMap.put(Pattern.compile("-=?"), NumeralFormulaManager::subtract);
    convertMap.put(Pattern.compile("%"), NumeralFormulaManager::modulo);
    convertMap.put(Pattern.compile("\\*=?"), NumeralFormulaManager::multiply);
    convertMap.put(Pattern.compile("(\\\\|\\/)=?"), NumeralFormulaManager::divide);
    return convertMap;
  }
  private static HashMap<Pattern, PatternConstraint> fillConst() {
    HashMap<Pattern,PatternConstraint> convertMap = new HashMap<>();
    convertMap.put(Pattern.compile("<"), NumeralFormulaManager::lessThan);
    convertMap.put(Pattern.compile(">"), NumeralFormulaManager::greaterThan);
    convertMap.put(Pattern.compile("<="), NumeralFormulaManager::lessOrEquals);
    convertMap.put(Pattern.compile(">="), NumeralFormulaManager::greaterOrEquals);
    convertMap.put(Pattern.compile("=="), NumeralFormulaManager::equal);
    return convertMap;
  }
  private interface PatternFormula {
    IntegerFormula convert(IntegerFormulaManager mgr, IntegerFormula a, IntegerFormula b);
  }

  private interface PatternConstraint {
    BooleanFormula convert(IntegerFormulaManager mgr, IntegerFormula a, IntegerFormula b);
  }
}
