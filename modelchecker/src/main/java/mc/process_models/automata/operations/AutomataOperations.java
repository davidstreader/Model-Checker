package mc.process_models.automata.operations;

import mc.process_models.automata.Automaton;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class AutomataOperations {

    private AutomataParallelComposition composition;
    private AutomataAbstraction abstraction;
    private AutomataReachability reachability;
    private AutomataLabeller labeller;

    public AutomataOperations(){
        this.composition = new AutomataParallelComposition();
        this.abstraction = new AutomataAbstraction();
        this.reachability = new AutomataReachability();
        this.labeller = new AutomataLabeller();
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

    public Automaton removeUnreachableNodes(Automaton automaton){
        return reachability.removeUnreachableNodes(automaton);
    }

    public Automaton labelAutomaton(Automaton automaton, String label){
        return labeller.labelAutomaton(automaton, label);
    }
}
