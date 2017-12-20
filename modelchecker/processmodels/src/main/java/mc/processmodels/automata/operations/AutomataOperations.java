package mc.processmodels.automata.operations;

import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class AutomataOperations {

  private AutomataLabeller labeller;

  public AutomataOperations() {
    this.labeller = new AutomataLabeller();
  }

  public Automaton removeUnreachableNodes(Automaton automaton) {
    return AutomataReachability.removeUnreachableNodes(automaton);
  }

  public Automaton labelAutomaton(Automaton automaton, String label) throws CompilationException {
    return labeller.labelAutomaton(automaton, label);
  }

}
