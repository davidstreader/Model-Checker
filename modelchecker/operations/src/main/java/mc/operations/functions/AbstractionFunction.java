package mc.operations.functions;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Functions;
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
import mc.processmodels.automata.operations.AutomataReachability;
import mc.util.expr.Expression;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  //@Override
  public Automaton compose(String id, Set<String> flags,
                           Context context, Automaton... automata) throws CompilationException {
    //System.out.println("compose");

    if (automata.length != getNumberArguments()) {
      throw new CompilationException(this.getClass(), null);
    }
//must copy automata else B in  A= abs(B ) may change
    //Automaton abstraction = automata[0].copy();
    Automaton abstraction = pruneHiddenNodes(id,flags, context, automata);
    boolean isFair = flags.contains("fair") || !flags.contains("unfair");

    // retrieve the unobservable edges from the specified automaton
    List<AutomatonEdge> hiddenEdges = abstraction.getEdges().stream()
            .filter(AutomatonEdge::isHidden)
            .collect(Collectors.toList());

    // construct  edges to replace the unobservable edges
    while (!hiddenEdges.isEmpty()) {
      //System.out.println("Todo "+ hiddenEdges.size());
      //for (AutomatonEdge hiddenEdge : hiddenEdges) {
      AutomatonEdge hiddenEdge = hiddenEdges.get(0);
//      AutomatonNode b = constructEdgeOnlyTau(abstraction, hiddenEdge);
//      toRemove.add(b);
      //System.out.println("removing "+hiddenEdge.toString());
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
        //  if (hiddenEdge.getFrom().isStartNode()  ) {
        //    hiddenEdge.getTo().setStartNode(true);
        //  }

        hiddenEdges.addAll(
                constructOutgoingEdges(abstraction, hiddenEdge, isFair, context));

        hiddenEdges.addAll(
                constructIncomingEdges(abstraction, hiddenEdge, isFair, context));
      } catch (InterruptedException ignored) {
        throw new CompilationException(this.getClass(), null);
      }
      //System.out.println("One done "+ hiddenEdges.size());
    }
    //   toRemove.forEach(s -> {
    //     if (s != null) {
    //      abstraction.removeNode(s);
    //     }
    //  });
//    return AutomataReachability.removeUnreachableNodes(abstraction);
    return abstraction;
  }

  /**
   *
   * @param abstraction  automaton
   * @param hiddenEdge   to be removed
   * @param isFair
   * @param context    Symbolic
   * @return list of new hidden edges
   * @throws CompilationException
   * @throws InterruptedException
   * adds  n-a->m when  n-a->x and x-tau->m to abstraction
   */
  private List<AutomatonEdge> constructOutgoingEdges(Automaton abstraction, AutomatonEdge hiddenEdge,
                                                     boolean isFair, Context context)
          throws CompilationException, InterruptedException {
    Guard hiddenGuard = hiddenEdge.getGuard();
    List<AutomatonEdge> incomingEdges = hiddenEdge.getFrom().getIncomingEdges();
    List<AutomatonEdge> hiddenAdded = new ArrayList<AutomatonEdge>();

    AutomatonNode to = hiddenEdge.getTo();
    for (AutomatonEdge edge : incomingEdges) {
      AutomatonNode from = edge.getFrom();

      Guard fromGuard = from.getGuard();
      Guard outGuard = null;
      if (fromGuard != null && hiddenGuard == null) {
        outGuard = fromGuard;
      } else if (fromGuard == null && hiddenGuard != null) {
        outGuard = hiddenGuard;
      } else if (fromGuard != null) {
        outGuard = Expression.combineGuards(hiddenGuard, fromGuard, context);
      }
      //if edge dose not exist add it
      if (abstraction.getEdge(edge.getLabel(), from, to) == null) {
        AutomatonEdge added;
        if (outGuard != null) {
          added = abstraction.addEdge(edge.getLabel(), from, to, outGuard);
        } else {
          added = abstraction.addEdge(edge.getLabel(), from, to, null);
        }
        if (added.isHidden()) {
          hiddenAdded.add(added);
          //System.out.println("hidden");
        }
        //System.out.println("Added " + added.toString());
      }
    }
    return hiddenAdded;
  }

  /**
   *
   * @param abstraction  automaton
   * @param hiddenEdge   to be removed
   * @param isFair
   * @param context   to do with symbolic events
   * @return list of new hidden edges
   * @throws CompilationException
   * @throws InterruptedException
   * adds  n-a->m when  n-tau->x and x-a->m  to abstraction
   */
  private List<AutomatonEdge> constructIncomingEdges(Automaton abstraction, AutomatonEdge hiddenEdge,
                                                     boolean isFair, Context context)
          throws CompilationException, InterruptedException {
    Guard hiddenGuard = hiddenEdge.getGuard();
    List<AutomatonEdge> outgoingEdges = hiddenEdge.getTo().getOutgoingEdges();
    List<AutomatonEdge> hiddenAdded = new ArrayList<AutomatonEdge>();
    AutomatonNode from = hiddenEdge.getFrom();
    for (AutomatonEdge edge : outgoingEdges) {
      AutomatonNode to = edge.getTo();
      Guard toGuard = to.getGuard();
      Guard outGuard = null;
      if (toGuard != null && hiddenGuard == null) {
        outGuard = toGuard;
      } else if (toGuard == null && hiddenGuard != null) {
        outGuard = hiddenGuard;
      } else if (toGuard != null) {
        outGuard = Expression.combineGuards(hiddenGuard, toGuard, context);
      }
      // if not all ready there add edge
      if (abstraction.getEdge(edge.getLabel(), from, to) == null) {
        AutomatonEdge added = abstraction.addEdge(edge.getLabel(), from, to, outGuard);
        if (added.isHidden()) {
          hiddenAdded.add(added);
          //System.out.println("hidden");
        }
        //System.out.println("Added " + added.toString());
      }
    }
    return hiddenAdded;
  }

  private void addNode(Automaton abstraction, AutomatonNode node) throws CompilationException {
    AutomatonNode newNode = abstraction.addNode(node.getId() + ".abs");

    newNode.copyProperties(node);
    if (newNode.isStartNode()) {
      abstraction.addRoot(newNode);
    }

  }

  private void addEdge(Automaton abstraction, AutomatonEdge edge) throws CompilationException {



    AutomatonNode from = abstraction.getNode(edge.getFrom().getId() + ".abs");
    AutomatonNode to = abstraction.getNode(edge.getTo().getId() + ".abs");
    abstraction.addEdge(edge.getLabel(), from, to, edge.getGuard());
  }



  /**
   *
   * @param id
   * @param flags
   * @param context
   * @param automata
   * @throws CompilationException
   * prunes any node that is only connected by hidden events
   *    This method solely acts as an accelerator and is only valid for
   *    Testing / Failure  semamtics
   */

  public Automaton  pruneHiddenNodes(String id, Set<String> flags,
  Context context, Automaton... automata)
    throws CompilationException
  {
//System.out.println("pruneHiddenNodes");
  if (automata.length != getNumberArguments()) {
    throw new CompilationException(this.getClass(), null);
  }
    Automaton abstraction = automata[0].copy();

    List<AutomatonNode> nodes = abstraction.getNodes();

    for (AutomatonNode n: nodes ) {
      boolean del = true;
      for (AutomatonEdge e : n.getOutgoingEdges()) {
        if (!e.isHidden()) {
          del = false;
          break;
        }
      }
      if (del) {
        for (AutomatonEdge e : n.getIncomingEdges()) {
          if (!e.isHidden()) {
            del = false;
            break;
          }
        }
        if (del) {
          if (n.isStartNode() || n.getOutgoingEdges().size() == 0) {
            del = false;
          }
        }
      }
      if (del) {
        //System.out.println("Pruning " + n.getId());
        List<AutomatonEdge> old = new ArrayList<AutomatonEdge>(n.getOutgoingEdges());
        old.addAll(n.getIncomingEdges());
        try {
          for (AutomatonEdge second : n.getOutgoingEdges()) {
            for (AutomatonEdge first : n.getIncomingEdges()) {
              abstraction.addEdge(Constant.HIDDEN, first.getFrom(), second.getTo(),
                cbGuards(first, second, context));
              //System.out.println("Adding " + first.getFrom().getId() + "-t->" + second.getTo().getId());
            }
          }

          for (AutomatonEdge e : old) {
            abstraction.removeEdge(e);
          }
          abstraction.removeNode(n);
        } catch (InterruptedException ignored) {
          throw new CompilationException(this.getClass(), null);
        }
      } else {//System.out.println("Skipping "+ n.getId());
      }
    }
  return abstraction;
  }

    private Guard cbGuards(AutomatonEdge from, AutomatonEdge to, Context context)
      throws CompilationException, InterruptedException  {
      Guard outGuard = null;
      Guard fromGuard =   from.getGuard();
      Guard toGuard =   to.getGuard();
      if (fromGuard == null) {
        outGuard = toGuard;
      } else {
        if (toGuard == null) {
          outGuard = fromGuard;
        } else {
          Expression.combineGuards(toGuard, fromGuard, context);
        }
      }
    return outGuard;
  }


}