package net.modelsolver;

import lombok.ToString;
import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import java.util.*;
import java.util.regex.Pattern;

import static net.modelsolver.FormulaUtils.parseGuards;

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
    return parseGuards(procGuard,imgr,varMap);
  }
}
