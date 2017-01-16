package net.modelsolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.*;

import java.util.*;

/**
 * This class allows you to solve guard information via an SMTSolver.
 */
public class EdgeMerger {
  /**
   * Pull guard data from edgeJSON, parsing it then turning it into a series of constraints
   * @param edge1JSON JSON data from the first edge to merge
   * @param edge2JSON JSON data from the second edge to merge
   * @return Results from the solver
   */
  public String mergeEdges(String edge1JSON, String edge2JSON) {
    try {
      JSONObject guard1JSON = new JSONObject(edge1JSON).getJSONObject("metaData").optJSONObject("guard");
      JSONObject guard2JSON = new JSONObject(edge2JSON).getJSONObject("metaData").optJSONObject("guard");
      //If either edge has no guard then nothing needs to be done.
      if (guard1JSON == null || guard2JSON == null) return null;
      Gson gson = new GsonBuilder().create();
      //Turn the above guard into a Guard object
      Guard guard1 = gson.fromJson(guard1JSON.toString(), Guard.class);
      Guard guard2 = gson.fromJson(guard2JSON.toString(), Guard.class);
      FormulaManager fmgr = getContext().getFormulaManager();
      boolean isSatisfied = checkSatisfied(guard1.getFormula(fmgr),guard2.getFormula(fmgr));
      Map<String,String> nextMap = new HashMap<>();
      for (String var: guard1.next) {
        //Some symbols have an extra = sign, so to avoid issues, we can splice it out here.
        var = var.replaceAll("(.)=?","$1");
        //If we encounter an assignment operator, just replace with the result.
        //Otherwise, replace with the operation itself
        nextMap.put(var.split("\\W")[0],var.contains(":")?var.split(":")[1]:var);
      }
      Guard result = new Guard();
      //Replace all outgoing variables from guard1 in guard2
      String guard = guard2.guard;
      for (String key: nextMap.keySet()) {
        //to avoid partial matches, surround the key with word boundaries.
        guard = guard.replaceAll("(\\b)"+key+"(\\b)","("+nextMap.get(key)+")");
      }
      //And guard1 and guard2 together
      guard = guard1.guard+"&&"+guard;
      //Convert the guard to a BooleanFormula
      BooleanFormula formulas = FormulaUtils.parseGuard(guard, fmgr, new HashMap<>());
      //Convert the formula to infix (its dumped as postfix)
      result.guard = OperatorUtils.postfixToInfix(fmgr.dumpFormula(formulas));
      //At this point, procGuard is the result.
      result.procGuard = isSatisfied+"";
      result.next = guard2.next;
      return gson.toJson(result);

    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  /**
   * Check if a set of formula results in an equation that is satisfiable
   * @param formulae The formulae to validate
   * @return true if satisfiable, false otherwise
   * @throws SolverException There was an exception while trying to solve
   * @throws InterruptedException The thread was interrupted while we attempted to solve
   */
  private boolean checkSatisfied(BooleanFormula... formulae) throws SolverException, InterruptedException, InvalidConfigurationException {
    try (ProverEnvironment prover = getContext().newProverEnvironment()) {
      Arrays.stream(formulae).forEach(prover::addConstraint);
      //Check if all the constraints are satisfiable
      return !prover.isUnsat();
    }
  }
  //Since we end up using this multiple times from javascript, its much easier to cache it once.
  private static SolverContext context;
  private SolverContext getContext() throws InvalidConfigurationException {
    if (context == null) {
      //Initilize the solver
      Configuration config = Configuration.defaultConfiguration();
      LogManager logger = BasicLogManager.create(config);
      ShutdownNotifier notifier = ShutdownNotifier.createDummy();
      // create the solver context, which includes all necessary parts for building, manipulating,
      // and solving formulas.
      context = SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.SMTINTERPOL);
    }
    return context;
  }
  /**
   * Simplify an expression
   * @param expr The expression
   * @return A simplified expression, or the original expression if an error occurs.
   */
  public String simplifyExpression(String expr) {
    try {
      FormulaManager fmgr = getContext().getFormulaManager();
      //Convert the guard to a BooleanFormula
      BooleanFormula formulas = FormulaUtils.parseGuard(expr, fmgr, new HashMap<>());
      //Convert the formula to infix (its dumped as postfix)
      return OperatorUtils.postfixToInfix(fmgr.dumpFormula(formulas));

    } catch (Exception ex) {
      ex.printStackTrace();
      return expr;
    }
  }
}
