package mc.process_models.automata.operations;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;

import java.util.List;
import java.util.Map;

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

    public Automaton parallelComposition(String id, Automaton automaton1, Automaton automaton2, Context context) throws CompilationException {
        Automaton processedAutomaton = composition.performParallelComposition(id, automaton1, automaton2, context);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton abstraction(Automaton automaton, boolean isFair, boolean prune, Context context) throws CompilationException, InterruptedException {
        Automaton processedAutomaton = automaton;
        if(prune){
            prune(processedAutomaton, context);
        }

        processedAutomaton = abstraction.performAbstraction(automaton, isFair,context);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton prune(Automaton automaton, Context context) throws CompilationException, InterruptedException {
        Automaton processedAutomaton = pruning.performPruning(automaton, context);
        processedAutomaton = removeUnreachableNodes(processedAutomaton);
        return processedAutomaton;
    }

    public Automaton simplification(Automaton automaton, Map<String, Expr> replacements, Context context) throws CompilationException, InterruptedException {
        return bisimulation.performSimplification(automaton, replacements, context);
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

  public Automaton nfaToDFA(Automaton model) throws CompilationException {
    return nfaToDFA.performNFAToDFA(model);
  }


}
