package mc.compiler;

import static mc.util.Utils.instantiateClass;

import java.util.logging.Logger;
import mc.plugins.IOperationInfixFunction;

public class EvaluatorFunctionRegister {
  public static void registerOperation(Class<? extends IOperationInfixFunction> clazz) {
    String name = instantiateClass(clazz).getNotation();
    Logger.getLogger(EvaluatorFunctionRegister.class.getSimpleName())
        .info("LOADED " + name + " INFIX FUNCTION PLUGIN");
    EquationEvaluator.operationsMap.put(name,clazz);
    OperationEvaluator.operationsMap.put(name,clazz);
  }
}
