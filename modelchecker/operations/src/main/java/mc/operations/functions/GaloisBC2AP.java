package mc.operations.functions;

import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetTransition;

public class GaloisBC2AP implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "fap2bc";
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
    if (automaton.getAlphabetBeforeHiding() == null) {
      automaton.setAlphabetBeforeHiding(new HashSet<>(automaton.getAlphabet()));
    }
    for (String action : flags) {
      if (alphabet.contains(action)) {
        automaton.relabelEdges(action, Constant.HIDDEN);
      } else {
        throw new CompilationException(getClass(), "Unable to find action " + action
          + " for hiding.", null);
      }
    }
    return new   AbstractionFunction().compose(id, new HashSet<>(), context, automaton);
  }

  /**
   * Petri Nets  relabel transitions b! to b^  and b? to b
   * Add listening loops to a Petri Net
   *    for each listening transition
   *       for all reachable Markings subset to location of transition
   *           add listening loop on resulting subMarkings
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
    System.out.println("GaloisBC2AP");
    Throwable t = new Throwable();
    t.printStackTrace();
    Petrinet petrinet = petrinets[0].reId("Gal");
    for(PetriNetTransition tr: petrinet.getTransitions().values()){
        tr.setLabel(reLabel(tr.getLabel()));
      System.out.println(" tr "+ tr.getId()+ " => "+tr.getLabel());
    }
    Set<String> alphabet = petrinet.getAlphabet().keySet().stream().filter(x->x.endsWith("?")).collect(Collectors.toSet());

/*
   Add listening loops to a Petri Net?
   for each listening transition
      for all reachable Markings subset to location of transition
          add listening loop on resulting subMarkings
 */
    return   petrinet;
  }
  private String reLabel(String ac){
    System.out.print("reLabel "+ ac);
    if (ac.endsWith("!"))
      return ac.substring(0,ac.length()-1)+"^";
    else if (ac.endsWith("?")) {
      return ac.substring(0, ac.length() - 1);
    }
    return ac;
  }


  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
    return null;
  }
}

