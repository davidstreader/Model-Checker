package mc.process_models.automata.operations;

import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.util.expr.Expression;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by sheriddavi on 24/01/17.
 */
public class AutomataOperations {

    private AutomataParallelComposition composition;
    private AutomataAbstraction abstraction;
    private AutomataPruning pruning;
    private AutomataBisimulation bisimulation;
    private AutomataReachability reachability;
    private AutomataLabeller labeller;
    private AutomataNFAToDFA nfaToDFA;

    public AutomataOperations(){
        this.composition = new AutomataParallelComposition();
        this.abstraction = new AutomataAbstraction();
        this.pruning = new AutomataPruning();
        this.bisimulation = new AutomataBisimulation();
        this.reachability = new AutomataReachability();
        this.labeller = new AutomataLabeller();
        this.nfaToDFA = new AutomataNFAToDFA();
    }

    public Automaton parallelComposition(String id, Automaton automaton1, Automaton automaton2) throws CompilationException {
        Automaton processedAutomaton = composition.performParallelComposition(id, automaton1, automaton2);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton abstraction(Automaton automaton, boolean isFair, boolean prune) throws CompilationException {
        Automaton processedAutomaton = automaton;
        if(prune){
            prune(processedAutomaton);
        }

        processedAutomaton = abstraction.performAbstraction(automaton, isFair);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton prune(Automaton automaton) throws CompilationException {
        Automaton processedAutomaton = pruning.performPruning(automaton);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton simplification(Automaton automaton, Map<String, Expression> replacements) throws CompilationException {
        return bisimulation.performSimplification(automaton, replacements);
    }

    public boolean bisimulation(List<Automaton> automata, Supplier<Boolean> checkToStop){
        return bisimulation.areBisimular(automata, checkToStop);
    }

    public Automaton removeUnreachableNodes(Automaton automaton){
        return reachability.removeUnreachableNodes(automaton);
    }

    public Automaton labelAutomaton(Automaton automaton, String label) throws CompilationException {
        return labeller.labelAutomaton(automaton, label);
    }

  public Automaton nfaToDFA(Automaton model) throws CompilationException {
    return nfaToDFA.performNFAToDFA(model);
  }


}
