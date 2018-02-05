package mc.operations.functions.infix;

import java.util.HashMap;
import java.util.Map;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;

/**
 * This covers the "internal choice" function.
 * This is a way that introduces non-determinism into the processes.
 * <p>
 * If this is introduced without a prefix, this will lead to multiple start nodes.
 * <p>
 * e.g. {@code A = B+C.}
 * <p>
 * <pre>
 * B   C
 * </pre>
 * However, if there are transitions that lead into the internal choice, it duplicates these
 * connections, creating a nondeterministic choice between the two processes provided
 * <p>
 * e.g. {@code A = b->C+D.}
 * <p>
 * <pre>
 *           ROOT
 *          /    \
 *         b      b
 *        /       \
 *       C         D
 * </pre>
 *
 * @author Jacob Beal
 * @see Automaton
 * @see mc.compiler.interpreters.AutomatonInterpreter
 */
public class InternalChoiceInfixFunction implements IProcessInfixFunction {

  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "internalChoice";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "+";
  }

  /**
   * Execute the function
   *
   * @param id         the id of the resulting automaton
   * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
   * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2)
      throws CompilationException {

    Automaton choice = new Automaton(id, !Automaton.CONSTRUCT_ROOT);

    choice.addAutomaton(automaton1);

    Map<AutomatonNode, AutomatonNode> automaton2NodeMap = new HashMap<>();

    automaton2.getNodes().forEach(n -> {
      AutomatonNode newN = choice.addNode();
      automaton2NodeMap.put(n, newN);
      newN.copyProperties(n);
      if (n.isStartNode()) {
        newN.setStartNode(true);
      }
    });

    for (AutomatonEdge e : automaton2.getEdges()) {
      choice.addEdge(e.getLabel(), automaton2NodeMap.get(e.getFrom()),
          automaton2NodeMap.get(e.getTo()), e.getGuard() == null ? null : e.getGuard().copy(), false);
    }


    return choice;
  }

  /**
   * TODO:
   * Execute the function.
   *
   * @param id        the id of the resulting petrinet
   * @param petrinet1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param petrinet2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting petrinet of the operation
   */
  @Override
  public Petrinet compose(String id, Petrinet petrinet1, Petrinet petrinet2) throws CompilationException {
    return null;
  }
}
