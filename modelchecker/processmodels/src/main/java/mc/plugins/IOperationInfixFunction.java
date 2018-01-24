package mc.plugins;

import java.util.Collection;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;

public interface IOperationInfixFunction {

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
   * Evaluate the function.
   *
   * @param automata the automata in the function (e.g. {@code A} and {@code B} in {@code A ~ B})
   * @return whether or not the automata provided pass the testing semantics.
   */
  boolean evaluate(Collection<Automaton> automata) throws CompilationException;
}
