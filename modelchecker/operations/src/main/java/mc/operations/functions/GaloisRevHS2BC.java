package mc.operations.functions;

import com.google.common.collect.Multiset;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.Mapping;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

/*
Need to add this as a function on  MultiProcessModel if going to reuse the Markings
shortcut is to recompute reachability
 */

public class GaloisRevHS2BC implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "revhs2bc";
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
    return null;
  }

  /*
      If called only with a petri Net  then use token rule to build reachable markings stored in MultiModel
      then call MultiModel
   */
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {

    Petrinet petrinet = petrinets[0].reId("Rev") ;
    System.out.println("Rev start "+petrinet.myString());
    for(PetriNetTransition tr : petrinet.getTransitions().values()) {
      String prefix1 = tr.getLabel().substring(0,tr.getLabel().length()-1);
      if (tr.getLabel().endsWith(".t?") ||
        tr.getLabel().endsWith(".t!") ||
        tr.getLabel().endsWith(".r!") ||
        tr.getLabel().endsWith(".r?")    ) tr.setLabel(Constant.HIDDEN);
      else if (tr.getLabel().endsWith("?") ) tr.setLabel(prefix1+"^");
      else if (tr.getLabel().endsWith("!") ) tr.setLabel(prefix1);

    }
    AbstractionFunction ab = new AbstractionFunction();
    Petrinet[] p = new Petrinet[]{petrinet};

    // petrinet = ab.compose(id,flags,context,p);
    System.out.println(petrinet.myString()+ "Rev end ");
    return petrinet;
  }



/*
    This is needed to obtain the pre computed markingToNode
 */

  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {

    return null;
  }
}

