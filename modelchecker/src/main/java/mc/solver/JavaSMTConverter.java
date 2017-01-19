package mc.solver;

import mc.util.expr.*;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;

import java.math.BigInteger;

public class JavaSMTConverter {

  //Since we end up using this multiple times from javascript, its much easier to cache it once.
  private static SolverContext context;
  static {
    try {
      getContext();
    } catch (InvalidConfigurationException e) {
      e.printStackTrace();
    }
  }
  private static SolverContext getContext() throws InvalidConfigurationException {
    if (context == null) {
      //Initilize the solver
      Configuration config = Configuration.defaultConfiguration();
      LogManager logger = BasicLogManager.create(config);
      ShutdownNotifier notifier = ShutdownNotifier.createDummy();
      // create the solver context, which includes all necessary parts for building, manipulating,
      // and solving formulas.
      context = SolverContextFactory.createSolverContext(config, logger, notifier, Solvers.Z3);
    }
    return context;
  }
  private IntegerFormulaManager imgr() {
    return context.getFormulaManager().getIntegerFormulaManager();
  }
  private BooleanFormulaManager bmgr() {
    return context.getFormulaManager().getBooleanFormulaManager();
  }
  private BitvectorFormulaManager bvmgr() {
    return context.getFormulaManager().getBitvectorFormulaManager();
  }
  public String simplify(Expression expr) {
    Formula f = null;
    try {
      f = context.getFormulaManager().simplify(convert(expr));
    } catch (InterruptedException e) {
      e.printStackTrace();
      return null;
    }
    return f.toString();
  }
  public Formula convert(Expression expr) {
    if (expr instanceof AdditionOperator) {
      return convert((AdditionOperator)expr);
    } else if(expr instanceof AndOperator) {
      return convert((AndOperator)expr);
    } else if(expr instanceof BitAndOperator) {
      throw new UnsupportedOperationException("Solver does not support bit and");
    } else if (expr instanceof BitOrOperator) {
      throw new UnsupportedOperationException("Solver does not support bit or");
    } else if (expr instanceof DivisionOperator) {
      return convert((DivisionOperator)expr);
    } else if (expr instanceof EqualityOperator) {
      return convert((EqualityOperator)expr);
    } else if (expr instanceof ExclOrOperator) {
      return convert((ExclOrOperator)expr);
    } else if (expr instanceof GreaterThanEqOperator) {
      return convert((GreaterThanEqOperator)expr);
    } else if (expr instanceof GreaterThanOperator) {
      return convert((GreaterThanOperator)expr);
    } else if (expr instanceof IntegerOperand) {
      return convert((IntegerOperand)expr);
    } else if (expr instanceof LeftShiftOperator) {
      throw new UnsupportedOperationException("Solver does not support left shift");
    } else if (expr instanceof LessThanEqOperator) {
      return convert((LessThanEqOperator)expr);
    } else if (expr instanceof LessThanOperator) {
      return convert((LessThanOperator)expr);
    } else if (expr instanceof ModuloOperator) {
      return convert((ModuloOperator)expr);
    } else if (expr instanceof MultiplicationOperator) {
      return convert((MultiplicationOperator)expr);
    } else if (expr instanceof NotEqualOperator) {
      return convert((NotEqualOperator)expr);
    } else if (expr instanceof OrOperator) {
      return convert((OrOperator)expr);
    } else if (expr instanceof RightShiftOperator) {
      throw new UnsupportedOperationException("Solver does not support right shift");
    } else if (expr instanceof SubtractionOperator) {
      return convert((SubtractionOperator)expr);
    } else if (expr instanceof VariableOperand) {
      return convert((VariableOperand)expr);
    }
    return null;
  }
  private BooleanFormula convert(AndOperator expr) {
    return bmgr().and((BooleanFormula) convert(expr.getLeftHandSide()), (BooleanFormula) convert(expr.getRightHandSide()));
  }
  private BooleanFormula convert(OrOperator expr) {
    return bmgr().or((BooleanFormula) convert(expr.getLeftHandSide()), (BooleanFormula) convert(expr.getRightHandSide()));
  }
  private BooleanFormula convert(ExclOrOperator expr) {
    return bmgr().xor((BooleanFormula) convert(expr.getLeftHandSide()), (BooleanFormula) convert(expr.getRightHandSide()));
  }

  private BooleanFormula convert(GreaterThanEqOperator expr) {
    return imgr().greaterOrEquals((IntegerFormula) convert(expr.getLeftHandSide()), (IntegerFormula) convert(expr.getRightHandSide()));
  }
  private BooleanFormula convert(GreaterThanOperator expr) {
    return imgr().greaterThan((IntegerFormula) convert(expr.getLeftHandSide()), (IntegerFormula) convert(expr.getRightHandSide()));
  }
  private BooleanFormula convert(LessThanEqOperator expr) {
    return imgr().lessOrEquals((IntegerFormula) convert(expr.getLeftHandSide()), (IntegerFormula) convert(expr.getRightHandSide()));
  }
  private BooleanFormula convert(LessThanOperator expr) {
    return imgr().lessThan((IntegerFormula) convert(expr.getLeftHandSide()), (IntegerFormula) convert(expr.getRightHandSide()));
  }
  private BooleanFormula convert(EqualityOperator expr) {
    return imgr().equal((IntegerFormula)convert(expr.getLeftHandSide()),(IntegerFormula) convert(expr.getRightHandSide()));
  }
  private BooleanFormula convert(NotEqualOperator expr) {
    return bmgr().not(imgr().equal((IntegerFormula)convert(expr.getLeftHandSide()), (IntegerFormula)convert(expr.getRightHandSide())));
  }
//    public IntegerFormula convert(BitAndOperator expr) {
//      IntegerFormula left = (IntegerFormula) convert(expr.getLeftHandSide());
//      IntegerFormula right = (IntegerFormula) convert(expr.getLeftHandSide());
//    return context.getFormulaManager().;
//  }
//  public IntegerFormula convert(BitOrOperator expr) {
//    return context.mkOr((BooleanFormula)convert(expr.getLeftHandSide()),(BooleanFormula)convert(expr.getRightHandSide()));
//  }
  private IntegerFormula convert(ModuloOperator expr) {
    return imgr().modulo((IntegerFormula) convert(expr.getLeftHandSide()), (IntegerFormula) convert(expr.getRightHandSide()));
  }
  private IntegerFormula convert(MultiplicationOperator expr) {
    return imgr().multiply((IntegerFormula) convert(expr.getLeftHandSide()), (IntegerFormula) convert(expr.getRightHandSide()));
  }
  private IntegerFormula convert(SubtractionOperator expr) {
    return imgr().subtract((IntegerFormula) convert(expr.getLeftHandSide()), (IntegerFormula) convert(expr.getRightHandSide()));
  }
  private IntegerFormula convert(DivisionOperator expr) {
    return imgr().divide((IntegerFormula) convert(expr.getLeftHandSide()), (IntegerFormula) convert(expr.getRightHandSide()));
  }
  private IntegerFormula convert(AdditionOperator expr) {
    return imgr().add((IntegerFormula) convert(expr.getLeftHandSide()), (IntegerFormula) convert(expr.getRightHandSide()));
  }
  private IntegerFormula convert(VariableOperand expr) {
    return imgr().makeVariable(expr.getValue());
  }
  private IntegerFormula convert(IntegerOperand expr) {
    return imgr().makeNumber(expr.getValue());
  }
}
