package mc.compiler.interpreters;

import static mc.util.Utils.instantiateClass;

import java.util.logging.Logger;
import mc.plugins.IProcessFunction;
import mc.plugins.IProcessInfixFunction;

public final class InterpreterFunctionRegister {

  public static void registerFunction(Class<? extends IProcessFunction> clazz) {
    String name = instantiateClass(clazz).getFunctionName();
   // Logger.getLogger(InterpreterFunctionRegister.class.getSimpleName())
   //     .info("**LOADED " + name + " FUNCTION PLUGIN");
    AutomatonInterpreter.functions.put(name, clazz);
    PetrinetInterpreter.functions.put(name, clazz);
  }

  public static void registerInfixFunction(Class<? extends IProcessInfixFunction> clazz) {
    String name = instantiateClass(clazz).getNotation();
    //Logger.getLogger(InterpreterFunctionRegister.class.getSimpleName())
    //    .info("LOADED " + name + " INFIX FUNCTION PLUGIN");
    AutomatonInterpreter.infixFunctions.put(name, clazz);
    PetrinetInterpreter.infixFunctions.put(name, clazz);
  }
}
