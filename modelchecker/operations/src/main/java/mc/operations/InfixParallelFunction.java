package mc.operations;

import lombok.Value;
import mc.exceptions.CompilationException;
import mc.operations.impl.ParallelFunction;
import mc.plugins.IProcessInfixFunction;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.operations.AutomataReachability;

@Value
public class InfixParallelFunction implements IProcessInfixFunction{



    /**
     * A method of tracking the function
     *
     * @return The Human-Readable form of the function name
     */
    @Override
    public String getFunctionName() {
        return "Parallel";
    }

    /**
     * The form which the function will appear when composd in the text
     *
     * @return
     */
    @Override
    public String getNotation() {
        return "||";
    }

    /**
     * Execute the function
     *
     * @param id         the id of the resulting automaton
     * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
     * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
     * @return the resulting automaton of the operation
     */
    @Override
    public Automaton compose(String id, Automaton automaton1, Automaton automaton2) throws CompilationException {
        //use this secondary function in new object, as without mutable state the function becomes **very** hard
        //to properly compose without a wild mess of lambdas ... I tried
        return AutomataReachability.removeUnreachableNodes(new ParallelFunction().execute(id,automaton1,automaton2));
    }
}
