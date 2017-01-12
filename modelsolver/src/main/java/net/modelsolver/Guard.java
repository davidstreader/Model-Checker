package net.modelsolver;

import lombok.ToString;
import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
    ArrayList<BooleanFormula> constraints = new ArrayList<>();
    processVariables(imgr);
    String[] guards = guard.split("&&");
    System.out.println(Arrays.toString(guards));
    return constraints;
  }

  private void processVariables(IntegerFormulaManager imgr) {
    for (String var : variables) {
      String[] split = var.split("=");
      varMap.put(split[0],imgr.makeNumber(Integer.parseInt(split[1])));
    }
  }
  private IntegerFormula convertVariable(String var, IntegerFormulaManager imgr) {

    for (Pattern pattern: convertMap.keySet()) {

    }
    return null;
  }
  static {
    fillMap();
  }
  private static HashMap<Pattern,PattermFormula> convertMap = new HashMap<>();
  private static void fillMap() {
    convertMap.put(Pattern.compile("\\+=?"), NumeralFormulaManager::add);
    convertMap.put(Pattern.compile("-=?"), NumeralFormulaManager::subtract);
    convertMap.put(Pattern.compile("\\+=?"), NumeralFormulaManager::add);
    convertMap.put(Pattern.compile("%"), NumeralFormulaManager::modulo);
    convertMap.put(Pattern.compile("\\*=?"), NumeralFormulaManager::multiply);
    convertMap.put(Pattern.compile("(\\\\|\\/)=?"), NumeralFormulaManager::divide);
  }
  private interface PattermFormula {
    IntegerFormula convert(IntegerFormulaManager mgr, IntegerFormula a, IntegerFormula b);
  }
}
