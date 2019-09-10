package mc.operations.functions.infix;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessInfixFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.AutomataReachability;
import mc.processmodels.automata.operations.ChoiceFun;
import mc.processmodels.automata.operations.ProbabilsticChoiceFunction;
import mc.processmodels.petrinet.Petrinet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ProbChoice implements IProcessInfixFunction {

    /**
     * A method of tracking the function
     *
     * @return The Human-Readable form of the function name
     */
    public String getFunctionName() {
        return "external choice";
    }
    private final Set<String> valid = Collections.singleton("*d");
    public Collection<String> getValidFlags(){return valid;}
    /**
     * The form which the function will appear when composed in the text
     *
     * @return the textual notation of the infix function
     */
    public String getNotation() {
        return "[p]";
    }

    /**
     * Execute the function
     *
     * @param id         the id of the resulting automaton
     * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
     * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
     * @return the resulting automaton of the operation
     */
    public Automaton compose(String id, Automaton automaton1, Automaton automaton2)
        throws CompilationException {
        throw new CompilationException( this.getClass()," must not apply Prob choice to Automata "+ id);
    }

    /**
     * Execute the function.
     *
     * @param id        the id of the resulting petrinet
     * @param net1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
     * @param net2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
     * @param flags
     * @return the resulting petrinet of the operation
     */
//  @Override
    public Petrinet compose(String id, Petrinet net1, Petrinet net2, Set<String> flags)
        throws CompilationException {

        System.out.println("ProbChoice");
        ProbabilsticChoiceFunction sf = new ProbabilsticChoiceFunction();
        Petrinet choice = sf.compose(id,net1,net2,flags);
        return choice;
    }





}


