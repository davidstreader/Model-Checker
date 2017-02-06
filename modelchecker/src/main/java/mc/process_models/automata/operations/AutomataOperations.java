package mc.process_models.automata.operations;

import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;

import java.util.List;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class AutomataOperations {

    private AutomataParallelComposition composition;
    private AutomataAbstraction abstraction;
    private AutomataBisimulation bisimulation;
    private AutomataReachability reachability;
    private AutomataLabeller labeller;
    private AutomataNFA2DFA nfa2DFA;

    public AutomataOperations(){
        this.composition = new AutomataParallelComposition();
        this.abstraction = new AutomataAbstraction();
        this.bisimulation = new AutomataBisimulation();
        this.reachability = new AutomataReachability();
        this.labeller = new AutomataLabeller();
        this.nfa2DFA = new AutomataNFA2DFA();
    }

    public Automaton parallelComposition(String id, Automaton automaton1, Automaton automaton2) throws CompilationException {
        Automaton processedAutomaton = composition.performParallelComposition(id, automaton1, automaton2);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton abstraction(Automaton automaton) throws CompilationException {
        Automaton processedAutomaton = abstraction.performAbstraction(automaton, true);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton simplification(Automaton automaton) throws CompilationException {
        return bisimulation.performSimplification(automaton);
    }

    public boolean bisimulation(List<Automaton> automata){
        return bisimulation.areBisimular(automata);
    }

    public Automaton removeUnreachableNodes(Automaton automaton){
        return reachability.removeUnreachableNodes(automaton);
    }

    public Automaton labelAutomaton(Automaton automaton, String label) throws CompilationException {
        return labeller.labelAutomaton(automaton, label);
    }

  public Automaton nfa2dfa(Automaton model) throws CompilationException {
    return nfa2DFA.preformNFA2DFA(model);
  }
}
