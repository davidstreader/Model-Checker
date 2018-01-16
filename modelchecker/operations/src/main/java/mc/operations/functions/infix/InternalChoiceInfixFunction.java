package mc.operations.functions.infix;

import java.util.HashMap;
import java.util.Map;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

public class InternalChoiceInfixFunction implements IProcessInfixFunction {
  /**
   * A method of tracking the function
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "internalChoice";
  }

  /**
   * The form which the function will appear when composed in the text
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
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2) throws CompilationException {
    Automaton choice = new Automaton(id, !Automaton.CONSTRUCT_ROOT);

    choice.addAutomaton(automaton1);

    Map<AutomatonNode, AutomatonNode> automaton2NodeMap = new HashMap<>();

    automaton2.getNodes().forEach(n -> {
      AutomatonNode newN = choice.addNode();
      automaton2NodeMap.put(n, newN);
      newN.copyProperties(n);
      if(n.isStartNode()){
        newN.setStartNode(true);
      }
    });

    for (AutomatonEdge e : automaton2.getEdges()) {
      choice.addEdge(e.getLabel(), automaton2NodeMap.get(e.getFrom()),
          automaton2NodeMap.get(e.getTo()), e.getGuard() == null ? null : e.getGuard().copy());
    }


    return choice;
  }
}
