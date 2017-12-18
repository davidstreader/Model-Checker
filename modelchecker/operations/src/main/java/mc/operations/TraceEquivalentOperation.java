package mc.operations;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.automata.Automaton;

public class TraceEquivalentOperation implements IOperationInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "TraceEquivalent";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "#";
  }

  /**
   * Evaluate the function.
   *
   * @param automata automaton in the function (e.g. {@code A} in {@code A ~ B})
   * @return the resulting automaton of the operation
   */
  @Override
  public boolean evaluate(Collection<Automaton> automata) throws CompilationException {
    NFtoDFconvFunction func = new NFtoDFconvFunction();
    return new BisimulationOperation().evaluate(automata.stream().map(a -> {
      try {
        return func.compose(a.getId(), Collections.emptySet(), null, a);
      } catch (CompilationException e) {
        return null;
      }
    }).filter(Objects::nonNull).collect(Collectors.toList()));


  }
}
