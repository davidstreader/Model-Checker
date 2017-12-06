package mc.plugins;

import mc.process_models.automata.Automaton;

public interface IProcessInfixFunction {

    /**
     * A method of tracking the function
     * @return The Human-Readable form of the function name
     */
    String getFunctionName();

    /**
     * The form which the function will appear when composd in the text
     * @return
     */
    String getNotation();

    /**
     * Execute the function
     * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
     * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
     * @return the resulting automaton of the operation
     */
    Automaton compose(Automaton automaton1, Automaton automaton2);

    //TODO: Petrinet
}
