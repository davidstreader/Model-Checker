
package mc.operations;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Context;
//import mc.BuildAcceptanceGraphs;
import mc.AcceptanceGraph;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.operations.functions.AbstractionFunction;
import mc.operations.functions.Nfa2dfaWorks;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.operations.TraceWork.NodePair;
import mc.operations.TraceWork.NextMap;
import mc.operations.TraceWork.Nd2NextMap;
import mc.operations.TraceWork.NextComponent;

/**
 * Failure refinement differs from failure equality only in the initial coloring used
 */
public class FailureRefinement implements IOperationInfixFunction {


  /**
   * A method of tracking the function.
   *
   * @return The Human-Readable form of the function name
   */
  @Override
  public String getFunctionName() {
    return "FailureRefinement";
  }

  /**
   * The form which the function will appear when composed in the text.
   *
   * @return the textual notation of the infix function
   */
  @Override
  public String getNotation() {
    return "<f";
  }

  @Override
  public Collection<String> getValidFlags() {
    return ImmutableSet.of(Constant.UNFAIR, Constant.FAIR, Constant.CONGURENT);
  }

  @Override
  public String getOperationType() {
    return "automata";
  }

  Nd2NextMap a1Next = new Nd2NextMap();
  Nd2NextMap a2Next = new Nd2NextMap();

  /**
   * Evaluate failure Refinement.
   *
   * @param alpha
   * @param processModels the list of automata being compared
   * @return the resulting automaton of the operation
   * <p>
   * Failure equality is II-bisimulation of acceptance graphs
   * 1. build the acceptance graphs for each automata
   * a dfa + node to set of acceptance sets map
   * 2. Color the nodes of the dfa acording to acceptance set equality
   * initialise bisimulation coloring with the newly built coloring
   */
  @Override
  public boolean evaluate(Set<String> alpha, Set<String> flags, Context context, Collection<ProcessModel> processModels) throws CompilationException {
    boolean cong = flags.contains(Constant.CONGURENT);
    if (processModels.iterator().next() instanceof Automaton) {
      ArrayList<AcceptanceGraph> acctgrs = new ArrayList<>();

      for (ProcessModel pm : processModels) {
        Automaton a = (Automaton) pm;
        try {
          //Automaton temp;
          AcceptanceGraph ag = new AcceptanceGraph("dfa-" + a.getId(), a, cong);
          //temp = nfa2dfaworks.compose(a.getId(), new HashSet<>(), null, TraceType.CompleteTrace, a);
          System.out.println("ACC " + ag.toString());
          acctgrs.add(ag);
        } catch (CompilationException e) {
          //System.out.println("PINGO" + e.toString());
        }
      }


      Automaton a1 = acctgrs.get(0).getA();
      Automaton a2 = acctgrs.get(1).getA();
      //System.out.println(a1.myString());
      //System.out.println(a2.myString());
      //System.out.println("Trace Refinement type " + tt + " flags " + flags + " " + a1.getId() + "<<" + a2.getId());
//Both dfas are built
      a1Next = build_readyMap(a1, TraceType.CompleteTrace, cong); //Controls simulation
      a2Next = build_readyMap(a2, TraceType.CompleteTrace, cong);
      //The nfas are anotated with Ready sets
      //System.out.println(a2.myString());
      // printit(a2Next);
      AutomatonNode r1 = (AutomatonNode) a1.getRoot().toArray()[0];
      AutomatonNode r2 = (AutomatonNode) a2.getRoot().toArray()[0];
      //System.out.println("\nare the Failures of  "+ a1.getId()+" a subset of "+a2.getId());
      //System.out.println("roots " + r1.getId() + " " + r2.getId());
      boolean b;
      b = traceAccSubset(new NodePair(r2, r1), a2Next.getMap(), a1Next.getMap(),
        new ArrayList<>(), cong, acctgrs.get(1), acctgrs.get(0));
      //
      //System.out.println("Failure Refinement " +  " " + a2.getId() + " <f " + a1.getId() + " " + b);
//ALERT ALERT  trace equality  fails  T = a->a->STOP   X =  a->(a->X|A->STOP).
      TraceEquivalentOperation teo = new TraceEquivalentOperation();
      TraceRefinement tref = new TraceRefinement();
      boolean traceme;
      traceme = tref.evaluate(alpha, flags, context, processModels);
      //System.out.println("Trace refinement = "+traceme);
      return b && traceme;
    }
    System.out.printf("\nFailure semantics not defined for type " + processModels.iterator().next().getClass() + "\n");
    return false;
  }

  /*
  Main part of algorithm RECURSIVE on a Acceptnce Graph
  AG is a dfa with nodes anotated with sets of Ready sets
   it builds the relation between the nodes of the
  two processes and tests if the sets of ready sets are subsets
  NOT QUIESCENT
*/
  private boolean traceAccSubset(NodePair np,
                                 Map<AutomatonNode, NextMap> a1N,
                                 Map<AutomatonNode, NextMap> a2N,
                                 List<NodePair> processed,
                                 boolean cong,
                                 AcceptanceGraph ag1,
                                 AcceptanceGraph ag2) {
    //System.out.println("traceAccSubset " + np.first.getId() + " " + np.second.getId() + " ");
    /*
       test if acceptance sets are subset
     */
    //System.out.print(np.first.getId()+" "+np.second.getId()+ " ");
    boolean accb = AcceptanceGraph.AcceptanceSubSet(ag1.getNode2AcceptanceSets().get(np.first),
      ag2.getNode2AcceptanceSets().get(np.second));

    if (!accb) {
      return false;
    }
    //processed only used to stop algorithm running for ever with cyclic automata
    for (NodePair n : processed) {
      if (n.getFirst().getId().equals(np.getFirst().getId()) &&
        n.getSecond().getId().equals(np.getSecond().getId())) {
        //System.out.println("processed " + "  " + np.myString());
        return true;
      }
    }
    //System.out.println(a2N.get(np.second).labels() + " in " + a1N.get(np.first).labels());
    //strip delta if not cong
    Set<String> small;
    if (cong) {
      small = a2N.get(np.second).labels();
    } else {
      small = a2N.get(np.second).labels().stream().filter(x -> !Constant.external(x)).collect(Collectors.toSet());
    }
    Set<String> large = a1N.get(np.first).labels();
    //System.out.println("is " + np.second.getId() + " " + small + " in " + np.first.getId() + " " + large);
    if (large.containsAll(small)) {
      processed.add(np);
      // b? might not be in in the ready labels but is in the next step label
      for (String lab : small) {
        //   for(String lab: a2N.get(np.second).labels()){
        //System.out.print(lab + " ");
        if (!large.contains(lab)) {
          //System.out.println(lab + " NOTin  " + large + "  " + np.myString());
          return false;
        }
        if (Constant.external(lab)) continue;
        AutomatonNode nd1 = a1N.get(np.first).getNcs().get(lab);
        AutomatonNode nd2 = a2N.get(np.second).getNcs().get(lab);
        if (traceAccSubset(new NodePair(nd1, nd2), a1N, a2N, processed, cong, ag1, ag2) == false) return false;
        // i++; if (i>9) break;
      }
    } else {
      //System.out.println(small + " NOTsubset " + large + "  " + np.myString());
      return false;
    }
    //System.out.println("subSet true " + np.myString());
    return true;
  }

  /*
   Build the ready set for each node (used as test in recursive traceSubset)
   This is NOT used to take the next step
   This will call the recursive quiescentNext for tt QuiescentTrace
   For non congurance STOP is add recursivly in quiescentNext
  */
  private Nd2NextMap build_readyMap(Automaton a, TraceType tt, boolean cong) {
    //System.out.println("Build Ready Map "+tt+" cong= " + cong);
    Nd2NextMap nfanode2ASet = new Nd2NextMap();
    for (AutomatonNode n : a.getNodes()) {
      NextMap as;
      as = new NextMap(n.getOutgoingEdges().stream().
        distinct().
        map(x -> new NextComponent(x.getLabel(), x.getTo())).
        collect(Collectors.toSet()));

      //System.out.println("cong "+cong+" SOfar "+ n.getId()+" > "+as.myString());
      if (cong && n.isSTOP()
        && (tt.equals(TraceType.CompleteTrace))
        ) {
        as.ncs.put(Constant.STOP, n);
        //System.out.println("added "+Constant.STOP);
      }  //EEEck

      if (cong && n.isStartNode()) {
        as.ncs.put(Constant.Start, n);
      }
      //System.out.println("Next "+n.getId() + " -> " + as.myString());
      nfanode2ASet.getMap().put(n, as);
    }
    return nfanode2ASet;
  }
}
