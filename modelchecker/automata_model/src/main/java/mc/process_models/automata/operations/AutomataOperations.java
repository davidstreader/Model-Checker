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

    private AutomataLabeller labeller;

    public AutomataOperations(){
        this.labeller = new AutomataLabeller();
    }

    public Automaton removeUnreachableNodes(Automaton automaton){
        return AutomataReachability.removeUnreachableNodes(automaton);
    }

    public Automaton labelAutomaton(Automaton automaton, String label) throws CompilationException {
        return labeller.labelAutomaton(automaton, label);
    }

}
