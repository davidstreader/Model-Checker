package mc.plugins;

import mc.process_models.automata.Automaton;

import java.util.Collection;

/**
 * This interface describes a "function" in the LTS language variant in the use
 * The function has the syntax of: @code{functionName{flag(s)}(Automaton(s))}
 */
public interface IProcessFunction {

    /**
     * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
     *
     * @return the name of the function
     */
    String getFunctionName();

    /**
     * Get the available flags for the function described by this interface (e.g. {@code unfair} in
     * {@code abs{unfair}(A)}
     *
     * @return a collection of available flags (note, no variables may be flags)
     */
    Collection<String> getValidFlags();

    /**
     * Execute the function on automata
     *
     * @param id       the id of the resulting automaton
     * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
     * @param automata a variable number of automata taken in by the function
     * @return the resulting automaton of the operation
     */
    Automaton compose(String id, String[] flags, Automaton... automata);

    //TODO: Petrinet functions
}
