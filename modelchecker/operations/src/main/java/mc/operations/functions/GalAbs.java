
package mc.operations.functions;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;

import java.util.Collection;
import java.util.Set;


public class GalAbs implements IProcessFunction {

  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "gAbs";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
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
   *Call GaloisAbstraction  (For Testing)
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the z3 context to access the stuff
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags,
                           Context context, Automaton... automata) throws CompilationException {
    if (automata.length != getNumberArguments()) {
      throw new CompilationException(this.getClass(), null);
    }
    Automaton startA = automata[0].copy();
    System.out.println("gAbs START flags "+flags+" " + startA.myString());

    AbstractionFunction af = new AbstractionFunction();
    Automaton abstraction =  af.GaloisBCabs(id,flags,context,startA);
    SimpFunction sf = new SimpFunction();
    abstraction = sf.compose(id, flags,context,abstraction);
    System.out.println("gAbs END "+ abstraction.myString());
    return abstraction;
  }

  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
  return null;
  }
  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
    return null;
  }
  }

