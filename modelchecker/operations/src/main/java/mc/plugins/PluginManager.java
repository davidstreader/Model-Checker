package mc.plugins;

import com.google.common.collect.ImmutableSet;

import java.util.Objects;

import lombok.Getter;
import mc.compiler.EquationEvaluator;
import mc.compiler.Lexer;
import mc.compiler.OperationEvaluator;
import mc.compiler.Parser;
import mc.compiler.interpreters.AutomatonInterpreter;

import mc.util.Utils;
import org.reflections.Reflections;


public class PluginManager {

  /**
   * Singleton pattern.
   */
  @Getter
  private static final PluginManager instance = new PluginManager();

  /**
   * An org.reflections object that is used to retrieve classes within the
   * {@code mc.operations} package.
   */
  private final Reflections reflection = new Reflections("mc.operations");

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
   */
  public void registerPlugins() {
    //register the {@code f(x)} style functions to the interpreter
    getFunctions().forEach(AutomatonInterpreter::addFunction);
    getFunctions().forEach(Lexer::registerFunction);
    getFunctions().forEach(Parser::registerFunction);

    //register the {@code X||Y} style functions to the interpreter
    getInfixFunctions().forEach(AutomatonInterpreter::addInfixFunction);
    getInfixFunctions().forEach(Lexer::registerInfixFunction);
    getInfixFunctions().forEach(Parser::registerInfixFunction);

    //register the operations functions to the interpreter
    getInfixOperations().forEach(OperationEvaluator::addOperations);
    //register the operations functions to the equation generator
    getInfixOperations().forEach(EquationEvaluator::addOperations);
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

}
