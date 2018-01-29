package mc.operations.functions;

import java.io.IOException;
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

import mc.processmodels.petrinet.Petrinet;
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
    Automaton startA = automata[0].copy();
    //System.out.println("start "+ startA.toString());
 //System.out.println("Starting with "+startA.getNodes().size()+" nodes");
    Automaton abstraction = pruneHiddenNodes(context, startA);
 //System.out.println("Pruned to "+abstraction.getNodes().size()+" nodes");

 //System.out.println("Start merge loops of size 2 "+abstraction.toString());
    mergeloopsOfSize2(context,abstraction);
/*try{
  System.out.print("merge ended");
  System.in.read();
} catch (IOException e) {
  System.out.println(e.toString());
}*/
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
      //System.out.println("Removing "+ hiddenEdge.myString());
      List<AutomatonEdge> temp = new ArrayList<AutomatonEdge>();
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
        hiddenEdges.remove(hiddenEdge);
        continue; // do not add any edges;
      }

/* if hidden edge ends at the terminal noded then the start node is terminal
 * any edge added that is hidden must be added to the list of hidden edges to be removed.
 */
     try {
        if (hiddenEdge.getTo().isTerminal()) {
          hiddenEdge.getFrom().setTerminal(hiddenEdge.getTo().getTerminal());
        }

//System.out.println("tau = "+ hiddenEdge.myString());
        //abstraction is both In and OUT
        temp.addAll(
                constructOutgoingEdges(abstraction, hiddenEdge, context));

        temp.addAll(
                constructIncomingEdges(abstraction, hiddenEdge, context));
// 2 edge loops may have been added.
       mergeloopsOfSize2(context,abstraction);
//need to rebuild hiddenEdges as nodes may have been merged
       hiddenEdges = abstraction.getEdges().stream()
         .filter(AutomatonEdge::isHidden)
         .collect(Collectors.toList());
        //System.out.println("total taus "+hiddenEdges.size());
        //System.in.read();
      } catch (InterruptedException ignored) {
        throw new CompilationException(this.getClass(), null);
      //}  catch ( IOException ig) {
      //  throw new CompilationException(this.getClass(), null);
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
//System.out.println("incoming "+incomingEdges.size());
    AutomatonNode to = hiddenEdge.getTo();
    for (AutomatonEdge edge : incomingEdges) {
      if (edge.getTo().equals(edge.getFrom())) {continue;}
      //System.out.println(edge.myString());
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
   //System.out.println("a->tau-> added " + added.myString());
            hiddenAdded.add(added);
          }
        }
      } //if edge dose exist do nothing.

    return hiddenAdded;
  }

  /**
   *
   * @param abstraction  automaton   IN+OUT
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
//System.out.println("outgoing "+outgoingEdges.size());
    List<AutomatonEdge> hiddenAdded = new ArrayList<>();
    AutomatonNode from = hiddenEdge.getFrom();
    for (AutomatonEdge edge : outgoingEdges) {
      if (edge.getTo().equals(edge.getFrom())) {continue;}
      //System.out.println(edge.myString());
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
          System.out.println("tau->a-> added "+added.myString());
          hiddenAdded.add(added);
        }
      }
    }
    return hiddenAdded;
  }

  /**
   *
   * @param context The structure linking to z3
   * @param autoIN  The automaton to prune
   * @throws CompilationException
   * prunes any node that is only connected by hidden events
   *    This method solely acts as an accelerator and is only valid for
   *    Testing / Failure  semamtics
   */

  private Automaton  pruneHiddenNodes( Context context, Automaton autoIN)
    throws CompilationException
  {

    Automaton abstraction = autoIN.copy();
//System.out.println("prune "+ abstraction.toString());
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
/*
if (edge.isHidden() &&
         abstraction.getEdge(Constant.HIDDEN, to, from) == null) {
         System.out.println("combining "+ edge.getTo()+" and "+edge.getFrom());
         abstraction.combineNodes(edge.getFrom(), edge.getTo(), context);
       } else {
 */
  private void mergeloopsOfSize2 ( Context context, Automaton autoIN)
    throws CompilationException {
    //System.out.println("start");
     boolean go = true;
     while (go) {
       go = mergeloop(context, autoIN);
     }

  }
    private boolean  mergeloop( Context context, Automaton autoIN)
    throws CompilationException
    {

    List<AutomatonEdge> edges = autoIN.getEdges();


    for(AutomatonEdge edge: edges) {
      //System.out.println("edge "+edge.myString());
      try {
          if (!edge.getTo().equals(edge.getFrom())) {
            boolean merge = false;
            for (AutomatonEdge e : autoIN.getEdges()) {
              //if(e.getFrom().equals(edge.getTo())) {
              //  System.out.println("try "+ e.myString());
              //}
              if (e.getLabel().equals(Constant.HIDDEN) &&
                edge.getLabel().equals(Constant.HIDDEN) &&
                e.getFrom().equals(edge.getTo()) &&
                e.getTo().equals(edge.getFrom())) {
                merge = true;
                break;
              }
            }
            if (merge) {
              //System.out.println("Combining " + edge.getFrom() + " " + edge.getTo());
              autoIN.combineNodes(edge.getFrom(), edge.getTo(), context);
              //System.out.print("merging ");
              //System.in.read();
              return true;  //try again
            }
          }
       } catch (InterruptedException e) {
        //System.out.println(e);
        throw new CompilationException(this.getClass(), null);
      }
      //catch (IOException e) {
      //  System.out.println(e);
      //  throw new CompilationException(this.getClass(), null);
      //}
    }

    return false;  //nothing more to change
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

}