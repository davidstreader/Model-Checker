package mc.plugins;

import com.microsoft.z3.Context;
import mc.exceptions.CompilationException;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;

import java.util.Collection;
import java.util.Set;

/**
 * Currently only simple parameteriseation of parser
 *           +  no parameterisation of Expander
 * This interface describes a "function" in the LTS language variant in the use.
 * The function has the syntax of: @code{functionName{flag(s)}(Automaton(s))}
 * <p>
 * Classes that implement this interface are automatically loaded into the compiler at run time if
 * located in {@code mc.operations}.
 *
 *          This is for abs, simp,  Galois, .....   NOT ~, =f, <q, ....
 * @author Jacob Beal
 * @see IProcessInfixFunction
 * @see IProcessFunction
 */
public interface IProcessFunction {

  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  String getFunctionName();

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  Collection<String> getValidFlags();

  /**
   * Gets the number of automata to parse into the function.
   *
   * @return the number of arguments
   */
  int getNumberArguments();


  /**
   * Define function on which ever input best then get other input to convert to that type
   * Caller must convert back  (stop intermediate conversion with nested calls)
   * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   * @throws CompilationException when the function fails
   */
  Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
      throws CompilationException;

  /**
   * Execute the function on one or more petrinet.
   *
   * @param id        the id of the resulting petrinet
   * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param petrinets the variable number of petrinets taken in by the function
   * @return the resulting petrinet of the operation
   * @throws CompilationException when the function fails
   */
  Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException;

  MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException;


}
