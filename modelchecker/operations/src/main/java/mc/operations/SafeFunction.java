package mc.operations;

import com.microsoft.z3.Context;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.process_models.automata.Automaton;

import java.util.Collection;
import java.util.Collections;

public class SafeFunction implements IProcessFunction{
    /**
     * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
     *
     * @return the name of the function
     */
    @Override
    public String getFunctionName() {
        return "safe";
    }

    /**
     * Get the available flags for the function described by this interface (e.g. {@code unfair} in
     * {@code abs{unfair}(A)}
     *
     * @return a collection of available flags (note, no variables may be flags)
     */
    @Override
    public Collection<String> getValidFlags() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Gets the number of automata to parse into the function
     *
     * @return the number of arguments
     */
    @Override
    public int getNumberArguments() {
        return 1;
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
    public Automaton compose(String id, String[] flags, Context context, Automaton... automata) throws CompilationException {
        assert automata.length == 1;
        return automata[0].copy();
    }
}
