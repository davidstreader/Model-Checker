package mc.plugins;

import com.google.common.collect.ImmutableSet;
import mc.compiler.EvaluatorFunctionRegister;
import mc.compiler.ParserFunctionRegister;
import mc.compiler.interpreters.InterpreterFunctionRegister;
import mc.util.Utils;
import org.reflections.Reflections;

import java.util.Objects;
import java.util.logging.Logger;


public class PluginManager {

  /**
   * Singleton pattern.
   */
  private static final PluginManager instance = new PluginManager();

  /**
   * An org.reflections object that is used to retrieve classes within the
   * {@code mc.operations} package.
   */
  private final Reflections reflection = new Reflections("mc.operations");

  public static PluginManager getInstance() {
    return PluginManager.instance;
  }

  /**
   * This retrieves an instance of every single valid function within the
   * {@code mc.operations} package.
   *
   * @return A collection of Functions
   */
  private ImmutableSet<Class<? extends IProcessFunction>> getFunctions() {
    return ImmutableSet.copyOf(reflection.getSubTypesOf(IProcessFunction.class));
  }

  /**
   * This retrieves an instance of every single valid infix function within the
   * {@code mc.operations} package.
   *
   * @return A collection of Infix Functions
   */
  private ImmutableSet<Class<? extends IProcessInfixFunction>> getInfixFunctions() {
    return ImmutableSet.copyOf(reflection.getSubTypesOf(IProcessInfixFunction.class));
  }

  /**
   * This retrieves an instance of every single valid infix operation within the
   * {@code mc.operations} package.
   *
   * @return A collection of Infix Operations
   */
  private ImmutableSet<Class<? extends IOperationInfixFunction>> getInfixOperations() {
    return ImmutableSet.copyOf(reflection.getSubTypesOf(IOperationInfixFunction.class));
  }

  /**
   * This function calls the relevant parts of the parser and
   * provides the requisite class files to the parser.
   * NOTE:
   *     the dynamically loaded functions can be defined for both
   *     automata and PetriNets the processing of a particular Node by the Interpreter
   *     controlls if the PetriNet or the automata version is applied
   */
  public void registerPlugins() {
    //register the {@code f(x)} style functions to the interpreter
    StringBuilder sb = new StringBuilder();
    for (String f: getFunctionList()){
      sb.append(f+",");
    }
    Logger.getLogger(InterpreterFunctionRegister.class.getSimpleName())
      .info("**LOADED " + sb.toString() + " FUNCTION PLUGIN");
    getFunctions().forEach(InterpreterFunctionRegister::registerFunction);
    getFunctions().forEach(ParserFunctionRegister::registerFunction);

    sb = new StringBuilder();
    for (String f: getInfixFunctionList()){
      sb.append(f+",");
    }
    Logger.getLogger(InterpreterFunctionRegister.class.getSimpleName())
      .info("**LOADED " + sb.toString() + " InfixFUNCTION PLUGIN");
    //register the {@code X||Y} style functions to the interpreter
    getInfixFunctions().forEach(InterpreterFunctionRegister::registerInfixFunction);
    getInfixFunctions().forEach(ParserFunctionRegister::registerInfixFunction);

    //register the operations functions to the interpreter
    getInfixOperations().forEach(ParserFunctionRegister::registerOperation);

    sb = new StringBuilder();
    for (String f: getInfixFunctionList()){
      sb.append(f+",");
    }
    Logger.getLogger(InterpreterFunctionRegister.class.getSimpleName())
      .info("**LOADED " + sb.toString() + " InfixOPERATION PLUGIN evaluator");
    getInfixOperations().forEach(EvaluatorFunctionRegister::registerOperation);
  }

  /**
   * This returns a list of the functions valid for use in the syntax parser.
   *
   * @return an array of function names
   */
  public String[] getFunctionList() {
    return getFunctions().stream()
        .map(Utils::instantiateClass)
        .filter(Objects::nonNull)
        .map(IProcessFunction::getFunctionName)
        .map(String::toLowerCase)
        .toArray(String[]::new);
  }
  public String[] getInfixFunctionList() {
    return getInfixFunctions().stream()
      .map(Utils::instantiateClass)
      .filter(Objects::nonNull)
      .map(IProcessInfixFunction::getFunctionName)
      .map(String::toLowerCase)
      .toArray(String[]::new);
  }
}
