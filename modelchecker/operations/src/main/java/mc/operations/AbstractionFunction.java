package mc.operations;

import mc.plugins.IProcessFunction;
import mc.plugins.IProcessInfixFunction;
import mc.process_models.automata.Automaton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class AbstractionFunction implements IProcessFunction{
    /**
     * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
     *
     * @return the name of the function
     */
    @Override
    public String getFunctionName() {
        return "abs";
    }

    /**
     * Get the available flags for the function described by this interface (e.g. {@code unfair} in
     * {@code abs{unfair}(A)}
     *
     * @return a collection of available flags (note, no variables may be flags)
     */
    @Override
    public Collection<String> getValidFlags() {
        return Collections.singletonList("unfair");
    }

    /**
     * Execute the function on automata
     *
     * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
     * @param automata a variable number of automata taken in by the function
     * @return the resulting automaton of the operation
     */
    @Override
    public Automaton compose(String[] flags, Automaton... automata) {
        return null;
    }
}
