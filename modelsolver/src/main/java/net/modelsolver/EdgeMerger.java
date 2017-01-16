package net.modelsolver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONObject;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
      //Initilize the solver
      Configuration config = Configuration.defaultConfiguration();
      LogManager logger = BasicLogManager.create(config);
      ShutdownNotifier notifier = ShutdownNotifier.createDummy();
      JSONObject ret = new JSONObject();
      boolean success;
      // create the solver context, which includes all necessary parts for building, manipulating,
      // and solving formulas.
      try (SolverContext context =
             SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.SMTINTERPOL)) {
        FormulaManager fmgr = context.getFormulaManager();
        try (ProverEnvironment prover = context.newProverEnvironment()) {
          guard1.getConstraints(fmgr).forEach(prover::addConstraint);
          guard2.getConstraints(fmgr).forEach(prover::addConstraint);
          //Check if all the constraints are satisfiable
          success = !prover.isUnsat();

        }
        ret.put("success", success);

        Map<String,String> nextMap = new HashMap<>();
        for (String var: guard1.next) {
          //Some symbols have an extra = sign, so to avoid issues, we can splce it out here.
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
        //Create a booleanformula of the resultant equation
        List<BooleanFormula> formulas = FormulaUtils.parseGuards(guard,fmgr.getIntegerFormulaManager(),new HashMap<>());
        BooleanFormula formula = formulas.remove(0);
        while (formulas.size() > 0) {
          formula = fmgr.getBooleanFormulaManager().and(formula,formulas.remove(0));
        }
        //Simplify and Convert the formula to infix (its dumped as postfix)
        result.guard = OperatorUtils.postfixToInfix(fmgr.dumpFormula(fmgr.simplify(formula)));
        //At this point, procGuard is the expression.
        result.procGuard = success+"";
        result.next = guard2.next;
        ret.put("guard",new JSONObject(gson.toJson(result)));
      }
      return ret.toString();
    } catch (Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }
}
