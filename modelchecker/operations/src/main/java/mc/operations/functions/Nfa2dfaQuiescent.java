
package mc.operations.functions;

  import com.microsoft.z3.Context;

  import java.util.Collection;
  import java.util.HashSet;
  import java.util.Set;

  import mc.TraceType;
  import mc.exceptions.CompilationException;
  import mc.operations.QuiescentRefinement;
  import mc.plugins.IProcessFunction;
  import mc.processmodels.MultiProcessModel;
  import mc.processmodels.automata.Automaton;
  import mc.processmodels.petrinet.Petrinet;

public class Nfa2dfaQuiescent implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "nfa2dfaQ";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return new HashSet<>();
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

  boolean dfaTerminating = false;
  /**
   * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the z3 context
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
    throws CompilationException {
    QuiescentRefinement qr = new QuiescentRefinement();

    Nfa2dfaWorks nfa2dfaWorks = new Nfa2dfaWorks();
    return nfa2dfaWorks.compose(id,flags,context,TraceType.QuiescentTrace,qr::quiescentWrapped,  automata);
  }

  /**
   * DONOTDO
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

