package mc.operations.functions;

import java.util.*;
import java.util.stream.Collectors;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.microsoft.z3.Context;


import mc.Constant;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

import mc.util.expr.Expression;

public class AbstractionFunction implements IProcessFunction {

  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "abs";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return ImmutableSet.of("unfair", "fair");
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
   * @param context  the z3 context to access the stuff
   * @return the resulting automaton of the operation
   */
  @Override
  public Automaton compose(String id, Set<String> flags,
                           Context context, Automaton... automata) throws CompilationException {
    if (automata.length != getNumberArguments()) {
      throw new CompilationException(this.getClass(), null);
    }

    Automaton abstraction = pruneHiddenNodes(context, automata);
    boolean isFair = flags.contains("fair") || !flags.contains("unfair");

    // retrieve the unobservable edges from the specified automaton
    List<AutomatonEdge> hiddenEdges = abstraction.getEdges().stream()
            .filter(AutomatonEdge::isHidden)
            .collect(Collectors.toList());

    //Construct  edges to replace the unobservable edges
    while (!hiddenEdges.isEmpty()) {
      AutomatonEdge hiddenEdge = hiddenEdges.get(0);

      hiddenEdges.remove(hiddenEdge);
      abstraction.removeEdge(hiddenEdge);

      if (hiddenEdge.getFrom().equals(hiddenEdge.getTo())) {
        if (!isFair) {
          AutomatonNode deadlockNode = abstraction.addNode();
          deadlockNode.setTerminal("ERROR");

          abstraction.addEdge(Constant.DEADLOCK, hiddenEdge.getFrom(),
                  deadlockNode, null);
        } else {
          if (hiddenEdge.getFrom().getOutgoingEdges().size() == 0) {
            hiddenEdge.getFrom().setTerminal("STOP");
          }
        }

      }
/* if hidden edge ends at the terminal noded then the start node is terminal
 * any edge added that is hidden must be added to the list of hidden edges to be removed.
 */
      try {
        if (hiddenEdge.getTo().isTerminal()) {
          hiddenEdge.getFrom().setTerminal(hiddenEdge.getTo().getTerminal());
        }


        hiddenEdges.addAll(
                constructOutgoingEdges(abstraction, hiddenEdge, context));

        hiddenEdges.addAll(
                constructIncomingEdges(abstraction, hiddenEdge, context));
      } catch (InterruptedException ignored) {
        throw new CompilationException(this.getClass(), null);
      }
    }
    return abstraction;
  }

  /**
   *
   * @param abstraction  automaton
   * @param hiddenEdge   to be removed
   * @param context    Symbolic
   * @return list of new hidden edges
   * @throws CompilationException
   * @throws InterruptedException
   * adds  n-a->m when  n-a->x and x-tau->m to abstraction
   */
  private List<AutomatonEdge> constructOutgoingEdges(Automaton abstraction, AutomatonEdge hiddenEdge,
                                                     Context context)
          throws CompilationException, InterruptedException {
    Guard hiddenGuard = hiddenEdge.getGuard();
    List<AutomatonEdge> incomingEdges = hiddenEdge.getFrom().getIncomingEdges();
    List<AutomatonEdge> hiddenAdded = new ArrayList<>();

    AutomatonNode to = hiddenEdge.getTo();
    for (AutomatonEdge edge : incomingEdges) {
      AutomatonNode from = edge.getFrom();

      Guard fromGuard = from.getGuard();
      Guard outGuard;

      if(fromGuard != null && hiddenGuard != null)
        outGuard = Expression.combineGuards(hiddenGuard, fromGuard, context);
      else if(fromGuard != null) {
        outGuard = fromGuard;
      } else {
        outGuard = hiddenGuard;
      }

      //if edge doesn't exist add it
      if (abstraction.getEdge(edge.getLabel(), from, to) == null) {
        AutomatonEdge added = abstraction.addEdge(edge.getLabel(), from, to, outGuard);

        if (added.isHidden()) {
          hiddenAdded.add(added);
        }
      }
    }
    return hiddenAdded;
  }

  /**
   *
   * @param abstraction  automaton
   * @param hiddenEdge   to be removed
   * @param context   to do with symbolic events
   * @return list of new hidden edges
   * @throws CompilationException
   * @throws InterruptedException
   * adds  n-a->m when  n-tau->x and x-a->m  to abstraction
   */
  private List<AutomatonEdge> constructIncomingEdges(Automaton abstraction, AutomatonEdge hiddenEdge,
                                                      Context context)
          throws CompilationException, InterruptedException {
    Guard hiddenGuard = hiddenEdge.getGuard();
    List<AutomatonEdge> outgoingEdges = hiddenEdge.getTo().getOutgoingEdges();
    List<AutomatonEdge> hiddenAdded = new ArrayList<>();
    AutomatonNode from = hiddenEdge.getFrom();
    for (AutomatonEdge edge : outgoingEdges) {
      AutomatonNode to = edge.getTo();
      Guard toGuard = to.getGuard();
      Guard newAbstractionEdgeGuard;

      if(toGuard != null && hiddenGuard != null)
        newAbstractionEdgeGuard = Expression.combineGuards(hiddenGuard, toGuard, context);
      else if(toGuard != null) {
        newAbstractionEdgeGuard = toGuard;
      } else {
        newAbstractionEdgeGuard = hiddenGuard;
      }

      // if not already there add edge
      if (abstraction.getEdge(edge.getLabel(), from, to) == null) {
        AutomatonEdge added = abstraction.addEdge(edge.getLabel(), from, to, newAbstractionEdgeGuard);
        if (added.isHidden()) {
          hiddenAdded.add(added);
        }
      }
    }
    return hiddenAdded;
  }

  /**
   *
   * @param context The structure linking to z3
   * @param automata  The automaton to prune
   * @throws CompilationException
   * prunes any node that is only connected by hidden events
   *    This method solely acts as an accelerator and is only valid for
   *    Testing / Failure  semamtics
   */

  private Automaton  pruneHiddenNodes( Context context, Automaton... automata)
    throws CompilationException
  {
  if (automata.length != getNumberArguments()) {
    throw new CompilationException(this.getClass(), null);
  }
    Automaton abstraction = automata[0].copy();

    List<AutomatonNode> nodes = abstraction.getNodes();


    for (AutomatonNode n: nodes ) {
      boolean del = true;
      for (AutomatonEdge e : Iterables.concat(n.getIncomingEdges(),n.getOutgoingEdges())) {
        if (!e.isHidden()) {
          del = false;
          break;
        }
      }

      if (n.isStartNode() || n.getOutgoingEdges().size() == 0) {
            del = false;
      }

      if (del) {
        try {
          for (AutomatonEdge second : n.getOutgoingEdges()) {
            for (AutomatonEdge first : n.getIncomingEdges()) {
                abstraction.addEdge(Constant.HIDDEN, first.getFrom(), second.getTo(),
                cbGuards(first, second, context));
            }
          }

        abstraction.removeNode(n);  // tidies up all the edges
        } catch (InterruptedException ignored) {
          throw new CompilationException(this.getClass(), null);
        }
      }
    }
  return abstraction;
  }

    private Guard cbGuards(AutomatonEdge from, AutomatonEdge to, Context context)
      throws CompilationException, InterruptedException  {
      Guard outGuard;
      Guard fromGuard =   from.getGuard();
      Guard toGuard   =   to.getGuard();

      if(fromGuard != null && toGuard != null)
        outGuard = Expression.combineGuards(toGuard, fromGuard, context);
      else if(fromGuard == null) {
        outGuard = toGuard;
      } else {
        outGuard = fromGuard;
      }

    return outGuard;
  }


}