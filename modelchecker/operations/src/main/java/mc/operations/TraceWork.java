package mc.operations;

import lombok.Getter;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.operations.functions.AbstractionFunction;
import mc.operations.functions.Nfa2dfaWorks;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class TraceWork {
  Nd2NextMap a1Next = new Nd2NextMap();
  Nd2NextMap a2Next = new Nd2NextMap();

  /*
     Currently these are all complete traces!
     cong => External state is visable not cong all state invisable
     Equality nfa 2 dfa  and bisim
     Refinement nfa 2 dfa and compute recurseive Subset
     Quiescent trace treats Quiescence of state as visable - in ready set
     QuiescentTrace abstracts all .t! and .t? - only be introduced by Galois
     need to pass cong through

     Quiesent Trace equality is stringer than complete trace equality as matching nodes have
     to both be Quiesent or not
     Quiescent trace subset is stronger than trace subset similarly.

     First add listening loops then follow the trace subset algorithm
   */
  public boolean evaluate(Set<String> flags, Collection<ProcessModel> processModels, TraceType tt) throws CompilationException {

    boolean cong = flags.contains(Constant.CONGURENT);
    if (processModels.iterator().next() instanceof Automaton) {

      Nfa2dfaWorks nfa2dfaworks = new Nfa2dfaWorks();

      AbstractionFunction absfunc = new AbstractionFunction();
      ArrayList<ProcessModel> dfas = new ArrayList<>();
      for (ProcessModel pm : processModels) {
        Automaton a = (Automaton) pm;
        try {
          Automaton temp;
          if (tt.equals(TraceType.QuiescentTrace)) {
            temp = absfunc.GaloisBCabs(a.getId(), flags, null, a);
            temp = nfa2dfaworks.compose(a.getId(), new HashSet<>(), null, TraceType.QuiescentTrace, temp);
          } else {
            temp = nfa2dfaworks.compose(a.getId(), new HashSet<>(), null, TraceType.CompleteTrace, a);
          }
          System.out.println("DFA "+temp.myString());
          dfas.add(temp);
        } catch (CompilationException e) {
          System.out.println("PINGO" + e.toString());
        }
      }
      Automaton a1 = (Automaton) dfas.get(1);
      Automaton a2 = (Automaton) dfas.get(0);
      System.out.println(a1.myString());
      System.out.println(a2.myString());
      System.out.println("Trace Refinement type " + tt + " flags " + flags + " " + a1.getId() + "<<" + a2.getId());
//Both dfas are built
  /*
    Trace subset tr(a1) suset tr(a2) a1,a2
        Assume both a1 and a2 dfa + NextSet is sorted
        Nodepair(r1,r2)
            if NodePair(x1,x2) and x1-a->y1 and x2-a->y2 then NodePair(y1,y2)
        If NodePair(nd1,nd2) implies Ready(n1) subset Ready(n2)
     */

      a1Next = build_readyMap(a1, tt, cong);
      a2Next = build_readyMap(a2, tt, cong);

      //The nfas are anotated with Ready sets
      //System.out.println(a2.myString());
      // printit(a2Next);
      AutomatonNode r1 = (AutomatonNode) a1.getRoot().toArray()[0];
      AutomatonNode r2 = (AutomatonNode) a2.getRoot().toArray()[0];

   /*
    Trace subset tr(a1) suset tr(a2) a1,a2
        Assume both a1 and a2 dfa + NextSet is sorted
        Nodepair(r1,r2)
            if NodePair(x1,x2) and x1-a->y1 and x2-a->y2 then NodePair(y1,y2)
        If NodePair(nd1,nd2) implies Ready(n1) subset Ready(n2)
     */
      System.out.println("<t or <q" + r1.getId() + " " + r2.getId());
      boolean b;
      if (tt.equals(TraceType.QuiescentTrace)) {
        b = quiescentTraceSubset(new NodePair(r1, r2), a1Next, a2Next, p, cong);
      } else {
        b = traceSubset(new NodePair(r1, r2), a1Next.getMap(), a2Next.getMap(), p, cong);
      }
      System.out.println("Trace Refinement type " + tt + " " + a1.getId() + "<<" + a2.getId() + " " + b);
      return b;
    }
    System.out.printf("\nTrace semantics not defined for type " + processModels.iterator().next().getClass() + "\n");
    return false;
  }

  private void printit(Map<AutomatonNode, NextMap> a1Next) {
    System.out.println(a1Next.keySet().stream().map(x -> (x.getId() + "=>" + a1Next.get(x).myString()))
      .reduce((x, y) -> x + y + "\n,"));
  }

  List<NodePair> p = new ArrayList<>();

  /*
     Main part of algorithm RECURSIVE on a DFA
      it builds the relation between the nodes of the
     two processes and tests if the ready sets are subsets
     NOT QUIESCENT
   */
  private boolean traceSubset(NodePair np, Map<AutomatonNode, NextMap> a1N,
                              Map<AutomatonNode, NextMap> a2N,
                              List<NodePair> processed,
                              boolean cong) {
    System.out.println("traceSubset " + np.first.getId() + " " + np.second.getId() + " ");

    //processed only used to stop algorithm running for ever with cyclic automata
    for (NodePair n : processed) {
      if (n.getFirst().getId().equals(np.getFirst().getId()) &&
        n.getSecond().getId().equals(np.getSecond().getId())) {
        return true;
      }
    }


    System.out.println(a2N.get(np.second).labels() + " in " + a1N.get(np.first).labels());

    //strip delta if not cong
    Set<String> small;
    if (cong) {
      small = a2N.get(np.second).labels();
    } else {
      small = a2N.get(np.second).labels().stream().filter(x -> !external(x)).collect(Collectors.toSet());
    }
    System.out.println(small + " in " + a1N.get(np.first).labels());
    if (a1N.get(np.first).labels().containsAll(small)) {
      processed.add(np);
      // b? might not be in in the ready labels but is in the next step label
      for (String lab : small) {
        //   for(String lab: a2N.get(np.second).labels()){
        System.out.print(lab + " ");
        if (!a1N.get(np.first).labels().contains(lab))
          return false;
        if (external(lab)) continue;
        AutomatonNode nd1 = a1N.get(np.first).getNcs().get(lab);
        AutomatonNode nd2 = a2N.get(np.second).getNcs().get(lab);
        if (traceSubset(new NodePair(nd1, nd2), a1N, a2N, processed, cong) == false) return false;
        // i++; if (i>9) break;
      }
    } else {
      System.out.println(small + " NOTsubset " + a1N.get(np.first).labels());
      return false;
    }
    return true;
  }

  /*
     QUIESCENT Main part of algorithm RECURSIVE  it builds the relation between the nodes of the
     two processes and tests if the ready sets are subsets
      NextMaps show what outputs could occur next NOT what event could occur Next
   */
  private boolean quiescentTraceSubset(NodePair np, Nd2NextMap aa1N,
                                       Nd2NextMap aa2N,
                                       List<NodePair> processed,
                                       boolean cong) {
    Map<AutomatonNode, NextMap> a1N = aa1N.getMap();
    Map<AutomatonNode, NextMap> a2N = aa2N.getMap();
    System.out.println("***quiescentTraceSubset " + np.first.getId() + " " + np.second.getId() + " ");
    System.out.println("a1N "+ aa1N.myString());

    System.out.println("a2N "+ aa2N.myString());

    System.out.println("***processed " + processed.stream().
      map(x -> x.myString()).reduce("", (x, y) -> x + " " + y));
    //processed only used to stop algorithm running for ever with cyclic automata
    for (NodePair n : processed) {
      if (n.getFirst().getId().equals(np.getFirst().getId()) &&
        n.getSecond().getId().equals(np.getSecond().getId())) {
        System.out.println(np.myString()+ " PROCESSED");
        return true;
      }
    }
    Set<String> firstReady = np.first.getOutgoingEdges().stream().map(x -> x.getLabel()).collect(Collectors.toSet());
    System.out.println(np.first.getId()+" firstReady " + firstReady); //actual outgoing
    Set<String> secondReady = np.second.getOutgoingEdges().stream().map(x -> x.getLabel()).collect(Collectors.toSet());
    System.out.println(np.second.getId()+ " secondReady " + secondReady);
    System.out.println(a2N.get(np.second).labels() + " in " + a1N.get(np.first).labels());

    //strip delta if not cong
    Set<String> extended2;
    if (cong) {
      extended2 = a2N.get(np.second).labels();
    } else {
      extended2 = a2N.get(np.second).labels().stream().filter(x -> !external(x)).collect(Collectors.toSet());
      secondReady = secondReady.stream().filter(x -> !external(x)).collect(Collectors.toSet());
    }
    System.out.println(extended2 + " in " + a1N.get(np.first).labels());
    System.out.println("secondReady " + secondReady);
    if (a1N.get(np.first).labels().containsAll(extended2)) {
      processed.add(np);
      System.out.println("add to processed " + np.myString());
      // b? might not be in in the ready labels but is in the next step label
      for (String lab : secondReady)  //lab is in extended2
      {
        //   for(String lab: a2N.get(np.second).labels()){
        System.out.println(lab + " from secondReady");
        AutomatonNode nd2 = a2N.get(np.second).getNcs().get(lab);  //next node2
        AutomatonNode nd1;
        //Recurse if first has same label or if listening step same color
        if (firstReady.contains(lab)) { //lab is in extended1
          System.out.println("firstReady "+firstReady+" lab "+lab);
          System.out.println(np.first.getId()+" and "+lab+" in "+ aa1N.myString());
          NextMap nmap = a1N.get(np.first);
          System.out.println("nmap "+nmap.myString());
          nd1 = nmap.getNcs().get(lab);  //next node1
          System.out.println("next 1 "+nd1.getId());
          if (quiescentTraceSubset(new NodePair(nd1, nd2), aa1N, aa2N, processed, cong) == false) return false;
        } else if  (lab.endsWith(Constant.BROADCASTSinput)) {

        }
            a2Next.getMap().get(np.second).labels().equals(a2Next.getMap().get(next(np.second, lab)).labels());
          nd1 = np.first;  //use past node1
          System.out.println(nd1.myString());
          System.out.println(nd2.myString());

          if (quiescentTraceSubset(new NodePair(nd1, nd2), aa1N, aa2N, processed, cong) == false) return false;
        }
        // i++; if (i>9) break;

    } else {
      System.out.println("P");
      System.out.println(extended2 + " NOTsubset ");
      System.out.println(extended2 + " NOTsubset " + a1N.get(np.first).labels());
      return false;
    }
    return true;
  }

  private static boolean external(String lab) {

    return lab.equals(Constant.DEADLOCK) ||
      lab.equals(Constant.STOP) ||
      lab.equals(Constant.Start) ||
      lab.equals(Constant.ERROR);
  }

  /*
    Build the ready set for each node (used as test in recursive traceSubset)
    This is NOT used to take the next step
    This will call the recursive quiescentNext for tt QuiescentTrace
    For non congurance STOP is add recursivly in quiescentNext
   */
  private Nd2NextMap build_readyMap(Automaton a, TraceType tt, boolean cong) {
    System.out.println("Build Ready Map "+tt+" cong= " + cong);
    Nd2NextMap nfanode2ASet = new Nd2NextMap();
    for (AutomatonNode n : a.getNodes()) {
      NextMap as;
      if (tt.equals(TraceType.QuiescentTrace)) {
        as = new NextMap(quiescentNext(n, cong));
      } else {
        as = new NextMap(n.getOutgoingEdges().stream().
          distinct().
          map(x -> new NextComponent(x.getLabel(), x.getTo())).
          collect(Collectors.toSet()));
      }
      //System.out.println("cong "+cong+" SOfar "+ n.getId()+" > "+as.myString());
      if (cong && n.isSTOP()
        && (tt.equals(TraceType.CompleteTrace)
        || tt.equals(TraceType.QuiescentTrace))
        ) {
        as.ncs.put(Constant.STOP, n);
        //System.out.println("added "+Constant.STOP);
      }  //EEEck
      if (tt.equals(TraceType.QuiescentTrace) &&
        n.isQuiescent()) {
        as.ncs.put(Constant.Quiescent, n);
      }
      if (cong && n.isStartNode()) {
        as.ncs.put(Constant.Start, n);
      }
      System.out.println("Next "+n.getId() + " -> " + as.myString());
      nfanode2ASet.getMap().put(n, as);
    }
    return nfanode2ASet;
  }

  /*
      recurse down the out going  input events and accumulate
      the out going output events
   */
  private Set<String> quiescentReady(AutomatonNode node, boolean cong) {
    return quiescentNext(node, new TreeSet<>(), cong).stream().map(x -> x.getAction()).collect(Collectors.toSet());
  }

  private Set<NextComponent> quiescentNext(AutomatonNode node, boolean cong) {
    return quiescentNext(node, new TreeSet<>(), cong);
  }

  private Set<NextComponent> quiescentNext(AutomatonNode node, Set<AutomatonNode> processed, boolean cong) {
    Set<NextComponent> sofar = new TreeSet<>();
    if (cong && node.isSTOP()) sofar.add(new NextComponent(Constant.STOP, node));
    //System.out.println("quiescentNext "+node.getId());
    for (AutomatonEdge ed : node.getOutgoingEdges()) {
      if (!ed.getLabel().endsWith("?")) {
        sofar.add(new NextComponent(ed.getLabel(), ed.getTo()));
      }
    }
    processed.add(node);
    for (AutomatonEdge ed : node.getOutgoingEdges()) {
      if (ed.getLabel().endsWith("?") && !isin(ed.getTo(), processed))
        sofar.addAll(quiescentNext(ed.getTo(), processed, cong));
    }
    //System.out.println(sofar.stream().map(x->x.action+" "+x.getTo().getId()).reduce((x,y)->x+" "+y));
    return sofar;
  }

  private boolean isin(AutomatonNode node, Set<AutomatonNode> processed) {

    for (AutomatonNode nd : processed) {
      if (nd.getId().equals(node.getId())) return true;
    }
    return false;
  }

  private AutomatonNode next(AutomatonNode nd, String lab) {
    for (AutomatonEdge ed : nd.getOutgoingEdges()) {
      if (ed.getLabel().equals(lab)) return ed.getTo();
    }
    return null;
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

    public Map<AutomatonNode, NextMap> getMap(){return nd2Nextmap; }
    public Nd2NextMap(Map<AutomatonNode, NextMap> in ){
        Map<AutomatonNode, NextMap> nd2nextmap = new TreeMap<AutomatonNode, NextMap>();
      for (AutomatonNode nd : in.keySet()){
        nd2nextmap.put(nd,in.get(nd));
      }
      this.nd2Nextmap = in;
    }
    public String myString(){
      StringBuilder sb = new StringBuilder();
      for (AutomatonNode key : nd2Nextmap.keySet()) {
        if (nd2Nextmap.get(key)== null) sb.append(key+"->null");
        else sb.append(key.getId() + "=> (" + nd2Nextmap.get(key).myString() + "); ");
      }
      return sb.toString();
    }

  public Nd2NextMap(){
    //Map<AutomatonNode, NextMap> in = new  TreeMap<AutomatonNode, NextMap> ();
    this(new  TreeMap<AutomatonNode, NextMap> ()) ;
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
        if (ncs.get(key)== null) sb.append(key+"->null");
        else sb.append(key + "->" + ncs.get(key).getId() + ", ");
      }
      return sb.toString();
    }
  }
}
