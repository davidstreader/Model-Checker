package mc.compiler;

import static mc.util.Utils.instantiateClass;

import java.util.logging.Logger;
import mc.compiler.interpreters.InterpreterFunctionRegister;
import mc.plugins.IOperationInfixFunction;
import mc.plugins.IProcessFunction;
import mc.plugins.IProcessInfixFunction;

public class ParserFunctionRegister {

  public static void registerFunction(Class<? extends IProcessFunction> clazz) {
    String name = instantiateClass(clazz).getFunctionName();
  //  Logger.getLogger(ParserFunctionRegister.class.getSimpleName())
  //      .info("LOADED " + name + " FUNCTION PLUGIN");
    Lexer.functions.add(name);
    Parser.functions.put(name, clazz);
  }

  public static void registerInfixFunction(Class<? extends IProcessInfixFunction> clazz) {
    String name = instantiateClass(clazz).getNotation();
   // Logger.getLogger(ParserFunctionRegister.class.getSimpleName())
   //     .info("LOADED " + name + " INFIX FUNCTION PLUGIN");
    Lexer.infixFunctions.add(name);
    Parser.infixFunctions.put(name, clazz);
  }

  public static void registerOperation(Class<? extends IOperationInfixFunction> clazz) {
    String name = instantiateClass(clazz).getNotation();
    System.out.println("registering Operations "+ name);
    Logger.getLogger(ParserFunctionRegister.class.getSimpleName())
        .info("ParserFunctionRegister LOADED " + name + " INFIX operatiion PLUGIN");
    Lexer.operationFunctions.add(name);
    Parser.operationFunctions.put(name,clazz);
  }
}
