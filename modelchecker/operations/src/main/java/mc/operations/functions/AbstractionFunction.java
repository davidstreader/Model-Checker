package mc.operations.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;
import mc.Constant;
import mc.compiler.Guard;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
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
   * Execute the abstraction on automata.  tau loops in input expected and removed
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
    Set<AutomatonEdge> processesed = new HashSet<>();
    Automaton startA = automata[0].copy();
    String Aname = startA.getId();
    System.out.println("start "+ startA.toString());

    Automaton abstraction = pruneHiddenNodes(context, startA);

    System.out.println("pruned "+ abstraction.toString());

    mergeloopsOfSize2(context, abstraction);
    System.out.println("merged "+ abstraction.toString());

    boolean isFair = flags.contains("fair") || !flags.contains("unfair");

    // retrieve the unobservable edges from the specified automaton
    List<AutomatonEdge> hiddenEdges = abstraction.getEdges().stream()
        .filter(AutomatonEdge::isHidden)
        .collect(Collectors.toList());

    //Construct  edges to replace the unobservable edges
    while (!hiddenEdges.isEmpty()) {
      AutomatonEdge hiddenEdge = hiddenEdges.get(0);
      if (processesed.contains(hiddenEdge)) {
        System.out.println("WHY "+ hiddenEdge.myString());
        hiddenEdges.remove(hiddenEdge);
        continue;
      }
      processesed.add(hiddenEdge); // ensures termination

      hiddenEdges.remove(hiddenEdge);
      abstraction.removeEdge(hiddenEdge);
      //System.out.println("Removing "+ hiddenEdge.myString());
      List<AutomatonEdge> temp = new ArrayList<AutomatonEdge>();
      if (hiddenEdge.getFrom().equals(hiddenEdge.getTo())) {
        if (!isFair) {
          AutomatonNode deadlockNode = abstraction.addNode();
          deadlockNode.setTerminal("ERROR");

          abstraction.addEdge(Constant.DEADLOCK, hiddenEdge.getFrom(),
              deadlockNode, null, true);
        } else {
          if (hiddenEdge.getFrom().getOutgoingEdges().size() == 0) {
            if (!hiddenEdge.getFrom().isTerminal())
                 hiddenEdge.getFrom().setTerminal("ERROR");
          }
        }
        hiddenEdges.remove(hiddenEdge);
//System.out.println("Removed loop "+hiddenEdge.myString()) ;
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
        mergeloopsOfSize2(context, abstraction);
//need to rebuild hiddenEdges as nodes may have been merged
        hiddenEdges = abstraction.getEdges().stream()
            .filter(AutomatonEdge::isHidden)
            .collect(Collectors.toList());
//    System.out.println("New total taus "+hiddenEdges.size());
        //System.in.read();

      } catch (InterruptedException ignored) {
        throw new CompilationException(this.getClass(), null);
        //}  catch ( IOException ig) {
        //  throw new CompilationException(this.getClass(), null);
      }
    }
    System.out.println("final "+ abstraction.toString());

    return abstraction;
  }

  /**
   * @param abstraction automaton
   * @param hiddenEdge  to be removed
   * @param context     Symbolic
   * @return list of new hidden edges
   * @throws CompilationException
   * @throws InterruptedException
   *
   * adds  n-a->m when  n-a->x and x-tau->m to abstraction
   * note if n-a->m and m-tau->n  then n-a->n will be added
   *    and m-a->n is added
   */
  private List<AutomatonEdge> constructOutgoingEdges(Automaton abstraction, AutomatonEdge hiddenEdge,
                                                     Context context)
      throws CompilationException, InterruptedException {
    boolean symbolic = abstraction.isSymbolic();
    Guard hiddenGuard = hiddenEdge.getGuard();
    List<AutomatonEdge> incomingEdges = hiddenEdge.getFrom().getIncomingEdges();
    List<AutomatonEdge> hiddenAdded = new ArrayList<>();
System.out.println(abstraction.getId()+ " "+ hiddenEdge.getId()+" "+
             "incoming "+incomingEdges.size());
    AutomatonNode to = hiddenEdge.getTo();
    for (AutomatonEdge edge : incomingEdges) {

      System.out.println("\t"+edge.myString());
      AutomatonNode from = edge.getFrom();

      Guard fromGuard = edge.getGuard();
      //System.out.println("Edge Guard "+ from.getGuard());
      Guard outGuard;

      AutomatonEdge added = new AutomatonEdge("fake", "fake", from, to);
      if (symbolic) {
        Guard newAbstractionEdgeGuard;
        if (fromGuard != null && hiddenGuard != null) {
          outGuard = Expression.combineGuards(fromGuard, hiddenGuard, context);
        } else if (fromGuard != null) {
          outGuard = fromGuard;
        } else {
          outGuard = hiddenGuard;
        }
        added = abstraction.addEdge(edge.getLabel(), from, to, outGuard, true);
      } else { // Atomic automaton
        if (edge.isHidden()&& edge.getFrom().getId().equals(edge.getTo().getId())) {
          continue;
        }
        //if new edge doesn't exist and is not the hidden edge then add it
        if (abstraction.getEdge(edge.getLabel(), from, to) == null ||
          (hiddenEdge.getFrom().getId().equals( from.getId()) &&
            hiddenEdge.getTo().getId().equals(to.getId()) &&
            edge.isHidden())) {
          added = abstraction.addEdge(edge.getLabel(), from, to, null, true);
          System.out.println("Outgoing add "+added.myString());
        if (from.getId().equals(to.getId()) && !edge.isHidden()) {
          abstraction.addEdge(edge.getLabel(), hiddenEdge.getFrom(), hiddenEdge.getTo(), null, true);
        }
        }
      }
      //System.out.println("symb = "+symbolic+" a->tau-> added " + added.myString());
      if (added.isHidden()) {
        //System.out.println("\tHidden a->tau-> added " + added.myString());
        hiddenAdded.add(added);
      }
    }
    System.out.println("endof Outgoing "+hiddenAdded.toString());
    return hiddenAdded;
  }

  /**
   * @param abstraction automaton   IN+OUT
   * @param hiddenEdge  to be removed
   * @param context     to do with symbolic events
   * @return list of new hidden edges
   * @throws CompilationException
   * @throws InterruptedException adds  n-a->m when  n-tau->x and x-a->m  to abstraction
   */
  private List<AutomatonEdge> constructIncomingEdges(Automaton abstraction,
                                                     AutomatonEdge hiddenEdge,
                                                     Context context)
      throws CompilationException, InterruptedException {
    boolean symbolic = abstraction.isSymbolic();
    Guard hiddenGuard = hiddenEdge.getGuard();
    List<AutomatonEdge> outgoingEdges = hiddenEdge.getTo().getOutgoingEdges();
System.out.println(abstraction.getId()+ " "+ hiddenEdge.getId()+" "+
     " outgoing "+outgoingEdges.size());
    List<AutomatonEdge> hiddenAdded = new ArrayList<>();
    AutomatonNode from = hiddenEdge.getFrom();
    for (AutomatonEdge edge : outgoingEdges) {

      System.out.println("\t"+edge.myString());
      AutomatonNode to = edge.getTo();
      Guard toGuard = edge.getGuard();
//.println("Edge Guard "+ edge.getGuard());

      AutomatonEdge added = new AutomatonEdge("fake", "fake", from, to);
      if (symbolic) {
        Guard newAbstractionEdgeGuard;
        if (toGuard != null && hiddenGuard != null) {
          newAbstractionEdgeGuard = Expression.combineGuards(hiddenGuard, toGuard, context);
        } else if (toGuard != null) {
          newAbstractionEdgeGuard = toGuard;
        } else {
          newAbstractionEdgeGuard = hiddenGuard;
        }
        added = abstraction.addEdge(edge.getLabel(), from, to, newAbstractionEdgeGuard, true);

      } else {  // Atomic edge
        // if loop do nothing
        if (edge.isHidden()&& edge.getFrom().getId().equals(edge.getTo().getId())) {
          continue;
        }
        // if not already there add edge
        if (abstraction.getEdge(edge.getLabel(), from, to) == null||
          (hiddenEdge.getFrom().getId().equals( from.getId()) &&
           hiddenEdge.getTo().getId().equals(to.getId()) &&
          edge.isHidden())) {
          added = abstraction.addEdge(edge.getLabel(), from, to, null, true);
          System.out.println("Incoming add "+added.myString());
          if (from.getId().equals(to.getId()) && !edge.isHidden()) {
            abstraction.addEdge(edge.getLabel(), hiddenEdge.getFrom(), hiddenEdge.getTo(), null, true);
          }
        }
      }
      System.out.println("symb = "+symbolic+" tau->a-> added " + added.myString());
      if (added.isHidden()) {
        //System.out.println("\tHidden tau->a-> added "+added.myString());
        hiddenAdded.add(added);
      }
    }
    String x = hiddenAdded.stream().map(e->e.myString()).collect(Collectors.joining());
    System.out.println("endof Incoming "+x);

    return hiddenAdded;
  }

  /**
   * @param context The structure linking to z3
   * @param autoIN  The automaton to prune
   * @throws CompilationException prunes any node that is only connected by hidden events
   *                              This method solely acts as an accelerator and is only valid for
   *                              Testing / Failure  semamtics
   */

  private Automaton pruneHiddenNodes(Context context, Automaton autoIN)
      throws CompilationException {


    Automaton abstraction = autoIN.copy();
//System.out.println("prune "+ abstraction.toString());
    List<AutomatonNode> nodes = abstraction.getNodes();


    for (AutomatonNode n : nodes) {
      boolean del = true;
      for (AutomatonEdge e : Iterables.concat(n.getIncomingEdges(), n.getOutgoingEdges())) {
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
                  cbGuards(first, second, context), true);
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

  /**
   * @param context
   * @param autoIN  In and Out
   * @throws CompilationException
   */
  private void mergeloopsOfSize2(Context context, Automaton autoIN)
      throws CompilationException {
    // System.out.println("start mloops 2");
    boolean go = true;
    while (go) {
      go = mergeloop(context, autoIN);
    }
    // System.out.println("end mloops 2");
  }

  /**
   * @param context Z3
   * @param autoIN  In and Out
   * @return
   * @throws CompilationException Used  to ensure termination of abstraction algorithm and ignores tau-loops
   *                              But caused problems with symbolic transitions
   */
  private boolean mergeloop(Context context, Automaton autoIN)
      throws CompilationException {
    List<AutomatonEdge> edges = autoIN.getEdges();

    for (AutomatonEdge edge : edges) {
      //  System.out.println("edge "+edge.myString());
      try {
        if (!edge.getTo().equals(edge.getFrom())) {
          for (AutomatonEdge e : autoIN.getEdges()) {
            //if(e.getFrom().equals(edge.getTo())) {
            //  System.out.println("try "+ e.myString());
            //}
            if (e.getLabel().equals(Constant.HIDDEN) &&
                edge.getLabel().equals(Constant.HIDDEN) &&
                e.getFrom().equals(edge.getTo()) &&
                e.getTo().equals(edge.getFrom())) {
              //System.out.println("Combining " + edge.getFrom() + " " + edge.getTo());
              autoIN.combineNodes(edge.getFrom(), edge.getTo(), context);
              //System.out.print("merging ");
              //System.in.read();
              return true;
            }
          }
        } else {
//     System.out.println("Edge loop ignored in mergeloop() "+edge.myString());
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

      throws CompilationException, InterruptedException {
    Guard outGuard;
    Guard fromGuard = from.getGuard();
    Guard toGuard = to.getGuard();
    if (context == null) {
// System.out.println("Context = null");
      return null;
    }
    if (fromGuard != null && toGuard != null) {
      outGuard = Expression.combineGuards(toGuard, fromGuard, context);
    } else if (toGuard != null) {
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

  private PetriNetTransition mergeTransitions(Petrinet toModify, PetriNetTransition t1, PetriNetTransition t2) throws CompilationException{
    PetriNetTransition output = toModify.addTransition(t1.getId() + " merge " + t2.getId(), t1.getId() + "," + t2.getLabel());


    Set<PetriNetPlace> inputPlaces = new HashSet<>(t1.pre());
    Set<PetriNetPlace> outputPlaces = new HashSet<>(t2.post());

    Set<PetriNetPlace> t2InputPlaces = new HashSet<>(t2.pre());


    //If t1 post place isnt one of t2s inputs then add it to output set
    outputPlaces.addAll(t1.post().stream().filter(postPlace -> !t2InputPlaces.contains(postPlace)).collect(Collectors.toList()));

    t2InputPlaces.removeAll(t1.post());

    inputPlaces.addAll(t2InputPlaces);


    for(PetriNetPlace inputPlace : inputPlaces) {
      toModify.addEdge(output, inputPlace, new HashSet<>(Collections.singleton(Petrinet.DEFAULT_OWNER)));
    }

    for(PetriNetPlace outputPlace : outputPlaces) {
      toModify.addEdge(outputPlace, output, new HashSet<>(Collections.singleton(Petrinet.DEFAULT_OWNER)));
    }


    return output;
  }
}
