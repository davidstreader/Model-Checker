package mc.operations.functions;

import com.microsoft.z3.Context;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;

import java.util.*;


    public class DeltaFunction implements IProcessFunction {
        /**
         * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
         *
         * @return the name of the function
         */
        @Override
        public String getFunctionName() {
            return "block";
        }

        /**
         * Get the available flags for the function described by this interface (e.g. {@code unfair} in
         * {@code abs{unfair}(A)}.
         *
         * @return a collection of available flags (note, no variables may be flags)
         */
        @Override
        public Collection<String> getValidFlags() {
            return Collections.singleton("*");
        }

        /**
         * Gets the number of automata to parse into the function.
         *
         * @return the number of arguments
         */
        @Override
        public int getNumberArguments() {
            return 1;
        }

        /**
         * Execute the function on automata.
         *
         * @param id       the id of the resulting automaton
         * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
         * @param automata a variable number of automata taken in by the function
         * @return the resulting automaton of the operation
         */
        @Override
        public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
            throws CompilationException {

            Automaton automaton = automata[0].copy();
            Set<String> alphabet = automaton.getAlphabet();
            for (String action : flags) {
                if (alphabet.contains(action)) {
                    automaton.relabelEdges(action, Constant.DEADLOCK);
                } else {
                    System.out.println("Unable to find action " + action+ " for delta in "+automaton.getId());
       /* throw new CompilationException(getClass(), "Unable to find action " + action
            + " for hiding.", null); */
                }
            }

            return  automaton;
        }

        /**
         * TODO:
         * Execute the function on one or more petrinet.
         *
         * @param id        the id of the resulting petrinet
         * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
         * @param context
         * @param petrinets the variable number of petrinets taken in by the function
         * @return the resulting petrinet of the operation
         * @throws CompilationException when the function fails
         */
        @Override
        public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
            return null;
        }
        @Override
        public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
            return null;
        }
    }

