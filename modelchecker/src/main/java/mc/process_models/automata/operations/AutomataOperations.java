package mc.process_models.automata.operations;

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

    public Automaton parallelComposition(String id, Automaton automaton1, Automaton automaton2){
        Automaton processedAutomaton = composition.performParallelComposition(id, automaton1, automaton2);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton abstraction(Automaton automaton){
        Automaton processedAutomaton = abstraction.performAbstraction(automaton, true);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton simplification(Automaton automaton){
        return bisimulation.performSimplification(automaton);
    }

    public boolean bisimulation(List<Automaton> automata){
        return bisimulation.areBisimular(automata);
    }

    public Automaton removeUnreachableNodes(Automaton automaton){
        return reachability.removeUnreachableNodes(automaton);
    }

    public Automaton labelAutomaton(Automaton automaton, String label){
        return labeller.labelAutomaton(automaton, label);
    }

  public Automaton nfa2dfa(Automaton model) {
    return nfa2DFA.preformNFA2DFA(model);
  }
}
