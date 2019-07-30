package mc.compiler;

import mc.plugins.IOperationInfixFunction;

import static mc.util.Utils.instantiateClass;

public class EvaluatorFunctionRegister {
  public static void registerOperation(Class<? extends IOperationInfixFunction> clazz) {
    String name = instantiateClass(clazz).getNotation();
   // Logger.getLogger(EvaluatorFunctionRegister.class.getSimpleName())
   //     .info("LOADED " + name + " INFIX FUNCTION PLUGIN");
    EquationEvaluator.operationsMap.put(name,clazz);
    OperationEvaluator.operationsMap.put(name,clazz);
  }
}
