package mc.plugins;

import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;

public interface IProcessInfixFunction {

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
     * Execute the function
     * @param id         the id of the resulting automaton
     * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
     * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
     * @return the resulting automaton of the operation
     */
    Automaton compose(String id, Automaton automaton1, Automaton automaton2) throws CompilationException;


    //TODO: Petrinet

}
