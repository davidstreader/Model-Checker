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
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

/**
 * This class allows you to solve guard information via an SMTSolver.
 */
public class Solver {
  /**
   * Pull guard data from edgeJSON, parsing it then turning it into a series of constraints
   * @param edgeJSON Edge data in the format {edge1:edge1,edge2:edge2}
   * @return Results from the solver
   */
  public String solve(String edgeJSON) {
    try {
      //Create a JSONObject from the passed in data
      JSONObject edges = new JSONObject(edgeJSON);
      JSONObject guard1JSON = edges.getJSONObject("edge1").optJSONObject("metaData");
      JSONObject guard2JSON = edges.getJSONObject("edge2").optJSONObject("metaData");
      //If either edge has no guard then nothing needs to be done.
      if (guard1JSON == null || guard2JSON == null) return null;
      //Get the guards themselves
      guard1JSON = guard1JSON.getJSONObject("guard");
      guard2JSON = guard2JSON.getJSONObject("guard");
      Gson gson = new GsonBuilder().create();
      //Turn the above guard into a Guard object
      Guard guard1 = gson.fromJson(guard1JSON.toString(), Guard.class);
      Guard guard2 = gson.fromJson(guard2JSON.toString(), Guard.class);
      //Initilize the solver
      Configuration config = Configuration.defaultConfiguration();
      LogManager logger = BasicLogManager.create(config);
      ShutdownNotifier notifier = ShutdownNotifier.createDummy();
      JSONObject ret = new JSONObject();
      // create the solver context, which includes all necessary parts for building, manipulating,
      // and solving formulas.
      try (SolverContext context =
             SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.SMTINTERPOL)) {
        FormulaManager fmgr = context.getFormulaManager();
        try (ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {
          guard1.getConstraints(fmgr).forEach(prover::addConstraint);
          guard2.getConstraints(fmgr).forEach(prover::addConstraint);
          //Check if all the constraints are satisfiable
          ret.put("success", !prover.isUnsat());
        }
      }
      return ret.toString();
    } catch (Exception ex) {
      return null;
    }
  }
}
