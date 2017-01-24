package mc.process_models.automata.operations;

import mc.process_models.automata.Automaton;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class AutomataOperations {

    private AutomataParallelComposition composition;
    private AutomataReachability reachability;

    public AutomataOperations(){
        this.composition = new AutomataParallelComposition();
        this.reachability = new AutomataReachability();
    }

    public Automaton parallelComposition(String id, Automaton automaton1, Automaton automaton2){
        return composition.performParallelComposition(id, automaton1, automaton2);
    }

    public Automaton removeUnreachableNodes(Automaton automaton){
        return reachability.removeUnreachableNodes(automaton);
    }
}
