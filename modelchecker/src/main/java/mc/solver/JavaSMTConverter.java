package mc.solver;

import com.google.common.base.Ascii;
import mc.util.expr.*;
import org.sosy_lab.common.NativeLibraries;
import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.*;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSMTConverter {

  //Since we end up using this multiple times from javascript, its much easier to cache it once.
  private static SolverContext context;
  static {
    try {
      String arch = Ascii.toLowerCase(NativeLibraries.Architecture.guessVmArchitecture().name());
      String os = Ascii.toLowerCase(NativeLibraries.OS.guessOperatingSystem().name());
      //By default, JavaSMT looks for native libraries next to the jar file.
      //However, this doesn't help us since the jar its reading is in the maven directory.
      //So here we override it with the native library.
      Field nativePath = NativeLibraries.class.getDeclaredField("nativePath");
      nativePath.setAccessible(true);
      nativePath.set(null,Paths.get("native", arch + "-" + os));
      HashMap<String,String> envMap = new HashMap<>();
      envMap.put("LD_LIBRARY_PATH",Paths.get("native", arch + "-" + os).toString());
      envMap.put("DYLD_LIBRARY_PATH",Paths.get("native", arch + "-" + os).toString());
      setEnv(envMap);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
    }
    try {
      getContext();
    } catch (InvalidConfigurationException e) {
      e.printStackTrace();
    }
  }
  protected static void setEnv(Map<String, String> newenv)
  {
    try
    {
      Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
      Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
      theEnvironmentField.setAccessible(true);
      Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
      env.putAll(newenv);
      Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
      theCaseInsensitiveEnvironmentField.setAccessible(true);
      Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
      cienv.putAll(newenv);
    }
    catch (NoSuchFieldException e)
    {
      try {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for(Class cl : classes) {
          if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Object obj = field.get(env);
            Map<String, String> map = (Map<String, String>) obj;
            map.clear();
            map.putAll(newenv);
          }
        }
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    } catch (Exception e1) {
      e1.printStackTrace();
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
  private BitvectorFormulaManager bvmgr() {
    return context.getFormulaManager().getBitvectorFormulaManager();
  }
  private BooleanFormulaManager bmgr() {
    return context.getFormulaManager().getBooleanFormulaManager();
  }
  public Expression simplify(Expression expr) {
    try {
      Formula f = context.getFormulaManager().simplify(convert(expr));
      String out = f.toString();
      //Convert from #x00000000 hex format to decimal
      Matcher matcher = Pattern.compile("#x\\w{8}").matcher(out);
      while (matcher.find()) {
        out = out.replaceAll(matcher.group(0),""+Integer.parseInt(matcher.group(0).substring(2),16));
      }
      out = out.replace("and","&&");
      out = out.replace("bvand","&");
      out = out.replace("bvnot","~");
      out = out.replace("bvor","|");
      out = out.replace("bvashr",">>");
      out = out.replace("bvshl","<<");
      out = out.replace("eq","==");
      out = out.replace("not","!");
      out = out.replace("=","==");
      //Reverse all tokens, as our RPN notation is op left right and Z3's is left right op
      String[] tokens = out.split("\\s+");
      Collections.reverse(Arrays.asList(tokens));
      out = String.join(" ",tokens);
      return new ShuntingYardAlgorithm().importExpr(out);
    } catch (InterruptedException e) {
      e.printStackTrace();
      return expr;
    }
  }
  private Formula convert(Expression expr) {
    if (expr instanceof AdditionOperator) {
      return convert((AdditionOperator)expr);
    } else if(expr instanceof AndOperator) {
      return convert((AndOperator)expr);
    } else if(expr instanceof BitAndOperator) {
      return convert((BitAndOperator)expr);
    } else if (expr instanceof BitOrOperator) {
      return convert((BitOrOperator) expr);
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
      return convert((LeftShiftOperator) expr);
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
      return convert((RightShiftOperator) expr);
    } else if (expr instanceof SubtractionOperator) {
      return convert((SubtractionOperator)expr);
    } else if (expr instanceof VariableOperand) {
      return convert((VariableOperand)expr);
    } else if (expr instanceof BitNotOperator) {
      return convert((BitNotOperator)expr);
    }else if (expr instanceof NotOperator) {
      return convert((NotOperator)expr);
    }
    //This should never happen.
    throw new IllegalStateException("Solver reached an unexpected state");
  }
  private BitvectorFormula convert(BitNotOperator expr) {
    return bvmgr().not((BitvectorFormula) convert(expr.getRightHandSide()));
  }
  private BooleanFormula convert(NotOperator expr) {
    return bmgr().not((BooleanFormula) convert(expr.getRightHandSide()));
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
    return bvmgr().greaterOrEquals((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()),true);
  }
  private BooleanFormula convert(GreaterThanOperator expr) {
    return bvmgr().greaterThan((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()),true);
  }
  private BooleanFormula convert(LessThanEqOperator expr) {
    return bvmgr().lessOrEquals((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()),true);
  }
  private BooleanFormula convert(LessThanOperator expr) {
    return bvmgr().lessThan((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()),true);
  }
  private BooleanFormula convert(EqualityOperator expr) {
    return bvmgr().equal((BitvectorFormula)convert(expr.getLeftHandSide()),(BitvectorFormula) convert(expr.getRightHandSide()));
  }
  private BooleanFormula convert(NotEqualOperator expr) {
    return bmgr().not(bvmgr().equal((BitvectorFormula)convert(expr.getLeftHandSide()), (BitvectorFormula)convert(expr.getRightHandSide())));
  }
  private BitvectorFormula convert(ModuloOperator expr) {
    return bvmgr().modulo((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()),true);
  }
  private BitvectorFormula convert(MultiplicationOperator expr) {
    return bvmgr().multiply((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()));
  }
  private BitvectorFormula convert(SubtractionOperator expr) {
    return bvmgr().subtract((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()));
  }
  private BitvectorFormula convert(DivisionOperator expr) {
    return bvmgr().divide((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()),true);
  }
  private BitvectorFormula convert(AdditionOperator expr) {
    return bvmgr().add((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()));
  }
  private BitvectorFormula convert(BitAndOperator expr) {
    return bvmgr().and((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()));
  }
  private BitvectorFormula convert(BitOrOperator expr) {
    return bvmgr().or((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()));
  }
  private BitvectorFormula convert(LeftShiftOperator expr) {
    return bvmgr().shiftLeft((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()));
  }
  private BitvectorFormula convert(RightShiftOperator expr) {
    return bvmgr().shiftRight((BitvectorFormula) convert(expr.getLeftHandSide()), (BitvectorFormula) convert(expr.getRightHandSide()),true);
  }
  private BitvectorFormula convert(VariableOperand expr) {
    return bvmgr().makeVariable(FormulaType.getBitvectorTypeWithSize(32),expr.getValue());
  }
  private BitvectorFormula convert(IntegerOperand expr) {
    return bvmgr().makeBitvector(32,expr.getValue());
  }
}
