package mc.operations;

import com.microsoft.z3.Context;
import mc.exceptions.CompilationException;
import mc.operations.impl.ParallelFunction;
import mc.plugins.IProcessFunction;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.operations.AutomataReachability;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ParallelPrefixFunction implements IProcessFunction{
    /**
     * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
     *
     * @return the name of the function
     */
    @Override
    public String getFunctionName() {
        return "parallel";
    }

    /**
     * Get the available flags for the function described by this interface (e.g. {@code unfair} in
     * {@code abs{unfair}(A)}
     *
     * @return a collection of available flags (note, no variables may be flags)
     */
    @Override
    public Collection<String> getValidFlags() {
        return Collections.emptySet();
    }

    /**
     * Gets the number of automata to parse into the function
     *
     * @return the number of arguments
     */
    @Override
    public int getNumberArguments() {
        return 2;
    }

    /**
     * Execute the function on automata
     *
     * @param id       the id of the resulting automaton
     * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
     * @param context
     * @param automata a variable number of automata taken in by the function
     * @return the resulting automaton of the operation
     * @throws CompilationException when the function fails
     */
    @Override
    public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata) throws CompilationException {
        assert automata.length == 2;
        Automaton automaton1 = automata[0];
        Automaton automaton2 = automata[1];
        return AutomataReachability.removeUnreachableNodes(new ParallelFunction().execute(id,automaton1,automaton2));
    }
}
