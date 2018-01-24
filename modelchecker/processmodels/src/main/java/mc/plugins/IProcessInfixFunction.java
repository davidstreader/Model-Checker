package mc.plugins;

import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;

/**
 * Classes implementing this type located in the {@code mc.operations} package are loaded at run
 * time as infix functions for use in the code.
 * <p>
 * This interface represents a function of the form {@code P1 operation P2}.
 *
 * @author Jacob Beal
 * @see IProcessFunction
 * @see IOperationInfixFunction
 * @see mc.plugins.PluginManager
 */
public interface IProcessInfixFunction {

  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  String getFunctionName();

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  String getNotation();


  /**
   * Execute the function.
   *
   * @param id         the id of the resulting automaton
   * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
   * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting automaton of the operation
   */
  Automaton compose(String id, Automaton automaton1, Automaton automaton2)
      throws CompilationException;


  /**
   * Execute the function.
   *
   * @param id        the id of the resulting petrinet
   * @param petrinet1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param petrinet2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting petrinet of the operation
   */
  Petrinet compose(String id, Petrinet petrinet1, Petrinet petrinet2)
      throws CompilationException;

}
