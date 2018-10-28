package mc.operations;

import com.microsoft.z3.Context;
import lombok.Getter;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
//import mc.operations.functions.AbstractionFunction;
import mc.operations.functions.AbstractionFunction;
import mc.operations.functions.Nfa2dfaWorks;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class TraceWork {
  Nd2NextMap a1Next = new Nd2NextMap();
  Nd2NextMap a2Next = new Nd2NextMap();

  /*
  ONE convert NFA 2 DFA
  TWO compute trace subset on DFA
     HandShake processes never have STOP nor ERROR  as an option they only occur in nodes that
     have NO outgoing edges (delta being the execption fro defined ERROR)
     cong => External state is visable
     not cong ==> start state invisable
               BUT STOP and ERROR visable but not distinguished
     Equality nfa 2 dfa  and bisim
     Refinement nfa 2 dfa and compute recurseive Simulation

  QUIESCENT
     Complete Quiescent traces may end at any quiescent state
     Currently these are all complete traces!

     QuiescentTrace abstract all .t! and .t? -  introduced by Galois

     ******  MUST drop listening from ready set? ******
     Quiesent Trace equality is stronger than complete trace equality as matching nodes have
     to both be Quiesent or not
     Quiescent trace subset is stronger than trace subset similarly.

     First add listening loops and Quiescent events then follow the trace subset algorithm

     PARAMETERISED subSetParam
   */
  public boolean evaluateOLD(Set<String> flags, Context context, Collection<ProcessModel> processModels, TraceType tt) throws CompilationException {
    SubSetDataConstructor op = (x, y) -> new ArrayList<>();
    SubSetEval yes = (x, y, z) -> true;
    return evaluate(flags, context, processModels, tt, op, yes);
  }

  public boolean evaluate(Set<String> flags, Context context,
                          Collection<ProcessModel> processModels,
                          TraceType tt,
                          SubSetDataConstructor buildData,
                          SubSetEval eval) throws CompilationException {
//Called directly from Trace Refinement and with some preprocessing from Quiescent Refinement
    boolean cong = flags.contains(Constant.CONGURENT);
    boolean complete = tt.equals(TraceType.CompleteTrace) || tt.equals(TraceType.QuiescentTrace);
    if (processModels.iterator().next() instanceof Automaton) {
      Nfa2dfaWorks nfa2dfaworks = new Nfa2dfaWorks();
      ArrayList<ProcessModel> dfas = new ArrayList<>();

      for (ProcessModel pm : processModels) {
        Automaton a;
        if (flags.contains(Constant.FAIR) || flags.contains(Constant.UNFAIR)) {
          AbstractionFunction absFun = new AbstractionFunction();
          a = absFun.compose(pm.getId(), flags, context, (Automaton) pm);
        } else {
          a = (Automaton) pm;
        }

        try {
          //System.out.println("TraceWorks "+a.myString());
          Automaton newdfa; //BUILD DFA

          newdfa = nfa2dfaworks.compose(a.getId(),
            new HashSet<>(), null,
            TraceType.CompleteTrace,
            buildData,  //lambda to build data for later comparision
            a);
          System.out.println("DFA " + newdfa.readySets2String(cong));
          System.out.println("DFA " + newdfa.getId() + "  \n" + newdfa.node2ReadySets2String());

          dfas.add(newdfa);
        } catch (CompilationException e) {
          //System.out.println("PINGO" + e.toString());
        }
      }
      Automaton a1 = (Automaton) dfas.get(1);
      Automaton a2 = (Automaton) dfas.get(0);
      //System.out.println("Trace Refinement type " + tt + " flags " + flags + " " + a1.getId() + "<<" + a2.getId());
//Both dfas are built
      a1Next = build_readyMap(a1, tt, cong); //needed to control the simulation
      a2Next = build_readyMap(a2, tt, cong);
      //The nfas are anotated with Ready sets
      //System.out.println(a2.myString());
      AutomatonNode r1 = (AutomatonNode) a1.getRoot().toArray()[0];
      AutomatonNode r2 = (AutomatonNode) a2.getRoot().toArray()[0];

      //System.out.println("?   " + a1.readySets2String(cong) + " " + a2.readySets2String(cong));
      boolean b;
      //Recursive  Algorithm - a2Next.getMap() is BOTH the readset to be checked and where to go next
      b = traceSubset(a2, a1, new NodePair(r2, r1), a2Next.getMap(), a1Next.getMap(),
        new ArrayList<>(), cong, complete, tt, eval);

      return b;
    }
    System.out.print("\nTrace semantics not defined for type " + processModels.iterator().next().getClass() + "\n");
    return false;
  }

  private String ready2String(Map<AutomatonNode, NextMap> a1Next) {
    StringBuilder sb = new StringBuilder();
    for (AutomatonNode nd : a1Next.keySet()) {
      sb.append(nd.getId() + "->" + a1Next.get(nd).myString() + "\n");
    }
    return sb.toString();
  }

  //List<NodePair> p = new ArrayList<>();


  /*
     Main part of algorithm RECURSIVE on a DFA
      it builds the relation between the nodes of the
     two processes and tests if the ready sets are subsets
     Handshake has delta events that differentiate STOP from ERROR
     Quiescent  needs similar!
   */
  private boolean traceSubset(Automaton dfa1, Automaton dfa2,
                              NodePair np,
                              Map<AutomatonNode, NextMap> a1N,
         // BOTH isSubset AND where to go next
                              Map<AutomatonNode, NextMap> a2N,
                              List<NodePair> processed,
                              boolean cong,
                              boolean complete,
                              TraceType tt,
                              SubSetEval evalSubset) {

    System.out.println("a1N\n" + ready2String(a1N));
    System.out.println("a2N\n" + ready2String(a2N));
    System.out.println("traceSubset start " + np.myString());
    for (NodePair n : processed) {
      if (n.getFirst().getId().equals(np.getFirst().getId()) &&
        n.getSecond().getId().equals(np.getSecond().getId())) {
        System.out.println(np.myString() + "  Already processed Returns true");
        return true;
      }
    }
    //System.out.println(a2N.get(np.second).labels() + " in " + a1N.get(np.first).labels());
    Set<String> small;
    if (cong) {
      small = a2N.get(np.second).labels();
    } else if (complete) {
      small = a2N.get(np.second).labels().stream().filter(x -> !Constant.start(x)).collect(Collectors.toSet());
    } else {
      small = a2N.get(np.second).labels().stream().filter(x -> !Constant.external(x)).collect(Collectors.toSet());
    }
    Set<String> large = a1N.get(np.first).labels().stream().collect(Collectors.toSet());
    //large = large.stream().filter(x->small.contains(x)).collect(Collectors.toSet());
    //Set<String> intersect = large.stream().filter(x->small.contains(x)).collect(Collectors.toSet());
    System.out.println(" is   " + small + " a Subset of " + large + " cong " + cong);

    for (String lab : small) {
      if (cong && Constant.external(lab) && !large.contains(lab)) {
        System.out.println(np.myString() + " returns false " + small + " " + lab + " " + large);
        return false;
      }
    }
// apply function List<Set<String>> ->List<Set<String>> -> boolean
    System.out.println("large contains! small " + large.containsAll(small));
    System.out.print("large 1 " + dfa1.getNode2ReadySets().get(np.first) + "  ");
    System.out.print("small 2 " + dfa2.getNode2ReadySets().get(np.second) + "  ");
    System.out.println(evalSubset.op(dfa2.getNode2ReadySets().get(np.second),
      dfa1.getNode2ReadySets().get(np.first), cong));

    if ((tt.equals(TraceType.QuiescentTrace) && containsAllOutput(large, small) ||
      (tt.equals(TraceType.CompleteTrace) && //large.containsAll(small)
        evalSubset.op(dfa2.getNode2ReadySets().get(np.second),
                      dfa1.getNode2ReadySets().get(np.first), cong)))) {
      processed.add(np);
      for (String lab : small) {
        if (Constant.external(lab)|| lab.equals(Constant.Quiescent)) continue;  // dose not include Quiescent
        System.out.println(" from " + np.myString() + " lab = " + lab);
        AutomatonNode nd1 = a1N.get(np.first).getNcs().get(lab);

        AutomatonNode nd2 = a2N.get(np.second).getNcs().get(lab);
        if (nd1==null) { //implicit reading loops
          nd1 = np.first;
          if (nd2==null) continue;  //both implicit so forget them
        } else if (nd2==null) {
          nd2=np.second;
        }
        System.out.println("nd1 = " + nd1.getId() + " nd2 = " + nd2.getId());
        if (traceSubset(dfa1, dfa2, new NodePair(nd1, nd2), a1N, a2N, processed, cong, complete, tt, evalSubset) == false)
          return false;
      }
    } else {
      System.out.println("Failing "+small+ " << "+large);
      System.out.println(np.myString() + " returns false " + small + " NOTsubset " + large);
      return false;
    }
    System.out.println(np.myString() + " traceSubset " + np.myString() + " returns true");
    return true;
  }

  private boolean containsAllOutput(Set<String> large, Set<String> small) {
    boolean out = true;
    for (String smallelm : small) {
      if (smallelm.endsWith(Constant.BROADCASTSoutput)) {
        if (!large.contains(smallelm)) { out = false; break;}
      }
    }
    System.out.println("containsAllOutput "+out);
    return true;
  }

  /*
    Build the ready set for each node (used as test in recursive traceSubset)
    This is NOT used to take the next step
    This will call the recursive quiescentNext for tt QuiescentTrace
    For non congurance STOP is add recursivly in quiescentNext
   */
  private Nd2NextMap build_readyMap(Automaton a, TraceType tt, boolean cong) {
    System.out.println("Build Ready Map " + tt + " cong= " + cong);
    //System.out.println("Automaton " + a.myString());
    //System.out.println(a.myString());
    Nd2NextMap nfanode2ASet = new Nd2NextMap();
    for (AutomatonNode n : a.getNodes()) {
      //System.out.println("node "+n.myString());
      NextMap as;
      as = new NextMap(n.getOutgoingEdges().stream().
        distinct().
        //  filter(x->(!(x.getLabel().endsWith("?")&& tt.equals(TraceType.QuiescentTrace)) )).
          map(x -> new NextComponent(x.getLabel(), x.getTo())).
          collect(Collectors.toSet()));
//dfa Node could be BOTH STOP and ERROR
      //System.out.println(n.myString());
      if (tt.equals(TraceType.CompleteTrace) || tt.equals(TraceType.QuiescentTrace)) {
        if (n.isSTOP()) {
          as.ncs.put(Constant.STOP, n);
          //System.out.println("added "+Constant.STOP);
        }
        if (n.isERROR()) {
          as.ncs.put(Constant.ERROR, n);  //needed for failure testing
          //System.out.println("\nSETTING " + n.getId() + "  TO ERROR\n");
        }
      }
      //System.out.println("cong "+cong+" SOfar "+ n.getId()+" > "+as.myString());
      if (cong)
        if (n.isStartNode()) {
          as.ncs.put(Constant.Start, n);
        }
      //System.out.println("Using readySet on node " + n.readySet(cong));
      //System.out.println("Next " + n.getId() + " -> " + as.myString());
      nfanode2ASet.getMap().put(n, as);
    }
    System.out.println(nfanode2ASet.myString());
    return nfanode2ASet;
  }


  @Getter
  public static class NodePair {
    AutomatonNode first;
    AutomatonNode second;

    public NodePair(AutomatonNode n1, AutomatonNode n2) {
      first = n1;
      second = n2;
    }

    String myString() {
      return first.getId() + " " + second.getId();
    }

    //@Override
    boolean equ(NodePair np) {
      return np.getFirst().getId().equals(this.getFirst().getId()) &&
        np.getSecond().getId().equals(this.getSecond().getId());
    }
  }

  @Getter
  public static class NextComponent implements Comparable<NextComponent> {
    public AutomatonNode to;
    public String action;

    public NextComponent(String actionin, AutomatonNode toin) {
      to = toin;
      action = actionin;
    }

    public static int compareTo(NextComponent c1, NextComponent c2) {
      return c1.compareTo(c2);
    }

    public int compareTo(NextComponent col) {

      if (action.compareTo(col.action) != 0) return action.compareTo(col.action);
      else {
        return to.getId().compareTo(col.to.getId());
      }
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof NextComponent &&
        ((NextComponent) obj).getTo().getId().equals(this.getTo().getId()) &&
        ((NextComponent) obj).getAction().equals(this.getAction())
      );
    }


    public String myString() {
      return action + " " + to.getId();
    }

  }

  public static class Nd2NextMap {  //Usefull debugging mystring
    Map<AutomatonNode, NextMap> nd2Nextmap;

    public Map<AutomatonNode, NextMap> getMap() {
      return nd2Nextmap;
    }

    public Nd2NextMap(Map<AutomatonNode, NextMap> in) {
      Map<AutomatonNode, NextMap> nd2nextmap = new TreeMap<AutomatonNode, NextMap>();
      for (AutomatonNode nd : in.keySet()) {
        nd2nextmap.put(nd, in.get(nd));
      }
      this.nd2Nextmap = in;
    }

    public String myString() {
      StringBuilder sb = new StringBuilder();
      for (AutomatonNode key : nd2Nextmap.keySet()) {
        if (nd2Nextmap.get(key) == null) sb.append(key + "->null");
        else sb.append(key.getId() + "=> (" + nd2Nextmap.get(key).myString() + "); ");
      }
      return sb.toString();
    }

    public Nd2NextMap() {
      //Map<AutomatonNode, NextMap> in = new  TreeMap<AutomatonNode, NextMap> ();
      this(new TreeMap<AutomatonNode, NextMap>());
    }
  }

  /*     Works ONLY for nfa
    NextMap maps a label to the Automaton node its leads to
    OR STOP to itself
   */
  @Getter
  public static class NextMap {

    Map<String, AutomatonNode> ncs = new TreeMap<>();

    public NextMap(Set<NextComponent> in) {
      Map<String, AutomatonNode> out = new TreeMap<>();
      for (NextComponent nc : in) {
        out.put(nc.action, nc.getTo());
      }
      ncs = out;
    }

    public void addSTOP(AutomatonNode nd) {
      ncs.put(Constant.STOP, nd);
    }

    public Set<String> labels() {
      return this.ncs.keySet();
    }

    public boolean equalLabels(NextMap ns) {
      return this.labels().equals(ns.labels());
    }

    public boolean subsetLabels(NextMap ns) {
      return ns.labels().containsAll(this.labels());
    }

    public String myString() {
      StringBuilder sb = new StringBuilder();
      for (String key : ncs.keySet()) {
        if (ncs.get(key) == null) sb.append(key + "->null");
        else sb.append(key + "->" + ncs.get(key).getId() + ", ");
      }
      return sb.toString();
    }
  }
}
