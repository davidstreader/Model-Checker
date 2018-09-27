package mc.operations.functions.infix;

import lombok.Value;
import mc.exceptions.CompilationException;
import mc.operations.impl.AutomataParallelFunction;
import mc.processmodels.automata.operations.PetrinetParallelFunction;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.operations.AutomataReachability;
import mc.processmodels.petrinet.Petrinet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

@Value
public class InfixParallelFunction implements IProcessInfixFunction {

  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "Parallel";
  }
  Set<String> valid = Collections.singleton("*");
  @Override
  public Collection<String> getValidFlags(){return valid;}
  /**
   * The form which the function will appear when composed in the text.
   *
   * @return
   */
  @Override
  public String getNotation() {
    return "||";
  }

  /**
   * Execute the function.
   *
   * @param id         the id of the resulting automaton
   * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
   * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2)
      throws CompilationException {

    //use this secondary function in new object, as without mutable state the function
    // becomes **very** hard to properly compose without a wild mess of lambdas.

    return AutomataReachability.removeUnreachableNodes(new AutomataParallelFunction()
            .execute(id, automaton1, automaton2));
  }

  /**
   * TODO:
   * Execute the function.
   *
   * @param id        the id of the resulting petrinet
   * @param petrinet1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param petrinet2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @param flags
   * @return the resulting petrinet of the operation
   */
  @Override
  public Petrinet compose(String id, Petrinet petrinet1, Petrinet petrinet2, Set<String> flags) throws CompilationException {
    //System.out.println("infixparallel "+flags);
    return PetrinetParallelFunction.compose(petrinet1,petrinet2,flags);
  }
}
