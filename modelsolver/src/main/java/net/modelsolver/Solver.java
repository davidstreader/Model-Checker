package net.modelsolver;

import org.json.JSONObject;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

public class Solver {
  public String solve(String modelJSON) throws InvalidConfigurationException, SolverException, InterruptedException {
    JSONObject processModel = new JSONObject(modelJSON);
    Configuration config = Configuration.defaultConfiguration();
    LogManager logger = BasicLogManager.create(config);
    ShutdownNotifier notifier = ShutdownNotifier.createDummy();
    JSONObject ret = new JSONObject();
    // create the solver context, which includes all necessary parts for building, manipulating,
    // and solving formulas.
    try (SolverContext context =
           SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.SMTINTERPOL)) {

      FormulaManager fmgr = context.getFormulaManager();

      BooleanFormulaManager bmgr = fmgr.getBooleanFormulaManager();
      IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();
      IntegerFormula x = imgr.makeNumber(20);
      IntegerFormula y = imgr.makeVariable("y");
      BooleanFormula f = bmgr.and(imgr.equal(x,y),imgr.greaterThan(x=imgr.add(x,imgr.makeNumber(1)),imgr.makeNumber(0)));
      x = imgr.multiply(x,imgr.makeNumber(2));

      try (ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {
        prover.addConstraint(f);
        boolean isUnsat = prover.isUnsat();
        if (!isUnsat) {
          Model model = prover.getModel();
          ret.put("success",true);
          ret.put("value",model.evaluate(x));
        } else {
          ret.put("success",false);
        }
      }
    }
    return ret.toString();
  }
}
