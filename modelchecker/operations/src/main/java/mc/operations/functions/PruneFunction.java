package mc.operations.functions;

import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.AutomataReachability;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.operations.PetrinetReachability;

public class PruneFunction implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "prune";
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

  /**
   * MAKES NO SENCE
   * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the z3 context to execute guards
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context,  Automaton... automata) {
    return null;
  }
 /* @Override
  public Automaton compose(String id, Set<String> flags, Context context,  Automaton... automata)
  throws CompilationException {
    // find the hidden edges within the automaton
    Automaton automaton = automata[0];
    List<AutomatonEdge> hiddenEdges = automaton.getEdges().stream()
        .filter(AutomatonEdge::isHidden)
        .collect(Collectors.toList());

    // if there are no hidden edges then there is nothing to prune
    if (hiddenEdges.isEmpty()) {
      return automaton;
    }

    for (AutomatonEdge hiddenEdge : hiddenEdges) {
      AutomatonNode from = hiddenEdge.getFrom();
      AutomatonNode to = hiddenEdge.getTo();

      List<AutomatonEdge> incomingHiddenEdges = from.getIncomingEdges().stream()
          .filter(AutomatonEdge::isHidden)
          .collect(Collectors.toList());

      // if there are incoming hidden edges then we cannot prune the current edge
      if (!incomingHiddenEdges.isEmpty()) {
        continue;
      }

      List<AutomatonEdge> outgoingHiddenEdges = to.getOutgoingEdges().stream()
          .filter(AutomatonEdge::isHidden)
          .collect(Collectors.toList());

      // if there are outgoing hidden edges then we cannot prune the current edge
      if (!outgoingHiddenEdges.isEmpty()) {
        continue;
      }

      // since there are no incoming or outgoing hidden edges we can merge the two nodes
      try {
        automaton.combineNodes(from, to, context);
      } catch (InterruptedException ignored) {
        throw new CompilationException(getClass(), ignored.getMessage());
      }
      automaton.removeEdge(hiddenEdge);
    }

    return AutomataReachability.removeUnreachableNodes(automaton);
  }
*/
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
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets)
      throws CompilationException {
    assert petrinets.length == 1;
    return PetrinetReachability.removeUnreachableStates(petrinets[0].copy());
  }
  @Override
  public MultiProcessModel compose(String id, Set<String> flags, Context context, MultiProcessModel... multiProcess) throws CompilationException {
    return null;
  }
}
