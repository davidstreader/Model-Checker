package mc.operations.functions.infix;

import mc.compiler.ast.*;
import mc.compiler.interpreters.AutomatonInterpreter;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * The sliding infix function is a "decaying choice" or "sliding choice" operation.
 * <p>
 * This is a choice that may decay into only one possible execution path.
 * <p>
 *
 *
 * @author Jacob Beal
 * @see InternalChoiceInfixFunction
 * @see ChoiceNode
 * @see IProcessInfixFunction
 * @see mc.plugins.PluginManager
 */
public class SlidingInfixFunction implements IProcessInfixFunction {
  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "slidingFunction";
  }
  @Override
  public Collection<String> getValidFlags(){return new HashSet<>();}
  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "|>";
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
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2) throws CompilationException {

    IdentifierNode aut1 = new IdentifierNode("P", "*",null);
    IdentifierNode aut2 = new IdentifierNode("Q", "*",null);
    TerminalNode stop = new TerminalNode("STOP", null);

    CompositeNode nondeterministicChoice = new CompositeNode(
        new InternalChoiceInfixFunction().getNotation(), aut1, aut2, null,new HashSet<>());

    ChoiceNode choice = new ChoiceNode(nondeterministicChoice, aut2, null);

    ProcessNode process = new ProcessNode(id, "*", choice, Collections.emptyList(), null);
    process.addType("automata");
    Map<String, ProcessModel> processesDefined = new HashMap<>();
    processesDefined.put("P", automaton1);
    processesDefined.put("Q", automaton2);

    AutomatonInterpreter interpreter = new AutomatonInterpreter();
    try {
      return (Automaton) interpreter.interpret(
         process,
         processesDefined,
        null,
        null,
        null,
        false);
    } catch (InterruptedException | ExecutionException e) {
      throw new CompilationException(getClass(), "Interrupted in compilation!");
    }

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
    return null;
  }
}
