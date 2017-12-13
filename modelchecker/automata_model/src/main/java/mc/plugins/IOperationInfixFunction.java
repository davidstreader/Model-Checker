package mc.plugins;

import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;

import java.util.Collection;

public interface IOperationInfixFunction {

    /**
     * A method of tracking the function
     * @return The Human-Readable form of the function name
     */
    String getFunctionName();

    /**
     * The form which the function will appear when composed in the text
     *
     * @return the textual notation of the infix function
     */
    String getNotation();


    /**
     * Evaluate the function
     * @param automata the automata in the function (e.g. {@code A} and {@code B} in {@code A ~ B})
     * @return the resulting automaton of the operation
     */
    boolean evaluate(Collection<Automaton> automata) throws CompilationException;


    //TODO: Petrinet
}
