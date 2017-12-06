package mc.operations;

import mc.plugins.IProcessFunction;
import mc.process_models.automata.Automaton;

import java.util.Collection;

public class HideFunction implements IProcessFunction{
    /**
     * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
     *
     * @return the name of the function
     */
    @Override
    public String getFunctionName() {
        return "hide";
    }

    /**
     * Get the available flags for the function described by this interface (e.g. {@code unfair} in
     * {@code abs{unfair}(A)}
     *
     * @return a collection of available flags (note, no variables may be flags)
     */
    @Override
    public Collection<String> getValidFlags() {
        return null;
    }

    /**
     * Execute the function on automata
     *
     * @param id       the id of the resulting automaton
     * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
     * @param automata a variable number of automata taken in by the function
     * @return the resulting automaton of the operation
     */
    @Override
    public Automaton compose(String id, String[] flags, Automaton... automata) {
        return null;
    }
}
