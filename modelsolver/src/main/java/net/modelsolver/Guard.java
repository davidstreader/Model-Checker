package net.modelsolver;

import lombok.ToString;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import java.util.HashMap;
import java.util.Map;

import static net.modelsolver.FormulaUtils.parseGuard;

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

  /**
   * Get a BooleanFormula from the guard
   * @param mgr A FormulaManager for creating the formulae
   * @return A BooleanFp
   */
  BooleanFormula getFormula(FormulaManager mgr) {
    IntegerFormulaManager imgr = mgr.getIntegerFormulaManager();
    //Create a map of variables
    Map<String,IntegerFormula> varMap = new HashMap<>();
    for (String var : variables) {
      String[] split = var.split("=");
      varMap.put(split[0],imgr.makeNumber(Integer.parseInt(split[1])));
    }
    return parseGuard(procGuard,mgr,varMap);
  }
}
