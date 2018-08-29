package mc.operations;

import lombok.Getter;
import mc.Constant;
import mc.TraceType;
import mc.compiler.ast.AutomataNode;
import mc.exceptions.CompilationException;
import mc.operations.functions.AbstractionFunction;
import mc.operations.functions.NFtoDFconvFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class TraceWork {
/*
   compute trace refinement (subset) for trace, complete trace and quiescent trace
   Note QuiescentTrace calls GaloisBCabs to abstract all .t! and .t?
 */
  public boolean evaluate(Collection<ProcessModel> processModels, TraceType tt) throws CompilationException {
    if (processModels.iterator().next() instanceof Automaton) {
      NFtoDFconvFunction nfa2dfafunc = new NFtoDFconvFunction();
      AbstractionFunction absfunc = new AbstractionFunction();
      ArrayList<ProcessModel> nfas = new ArrayList<>();
      for (ProcessModel pm : processModels) {
        Automaton a = (Automaton) pm;
        try {
          Automaton  temp ;
          if (tt.equals(TraceType.QuiescentTrace)) {
            temp = absfunc.GaloisBCabs(a.getId(), new HashSet<>(), null, a);
            temp = nfa2dfafunc.compose(a.getId(), new HashSet<>(), null, temp);
          }  else {
            temp = nfa2dfafunc.compose(a.getId(), new HashSet<>(), null, a);
          }
          nfas.add( temp );
        } catch (CompilationException e) {
          System.out.println("PINGO" + e.toString());
        }
      }
      Automaton a1 = (Automaton) nfas.get(1);
      Automaton a2 = (Automaton) nfas.get(0);
      System.out.println("Trace Refinement type "+ tt+" "+ a1.getId()+"<<"+a2.getId());

  /*
    Trace subset tr(a1) suset tr(a2) a1,a2
        Assume both a1 and a2 dfa + NextSet is sorted
        Nodepair(r1,r2)
            if NodePair(x1,x2) and x1-a->y1 and x2-a->y2 then NodePair(y1,y2)
        If NodePair(nd1,nd2) implies Ready(n1) subset Ready(n2)
     */

      Map<AutomatonNode, NextMap > a1Next = build_readyMap( a1,tt);
      //System.out.println(a1.myString());
      // printit(a1Next);

      Map<AutomatonNode, NextMap > a2Next = build_readyMap( a2,tt);
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
      System.out.println("<t or <q" + r1.getId()+ " "+ r2.getId());
      boolean b = traceSubset(new NodePair(r1,r2),a1Next,a2Next,p);
      System.out.println("Trace Refinement type "+ tt+" "+ a1.getId()+"<<"+a2.getId()+" "+b);
      return b;
    }
    System.out.printf("\nTrace semantics not defined for type " + processModels.iterator().next().getClass()+"\n");
    return false;
  }

  private void printit(Map<AutomatonNode, NextMap > a1Next){
    System.out.println(a1Next.keySet().stream().map(x->(x.getId()+"=>"+a1Next.get(x).myString()))
      .reduce((x,y)->x+y+"\n,"));
  }
  List<NodePair> p = new ArrayList<>();
/*
   Main part of algorithm RECURSIVE  it builds the relation between the nodes of the
   two processes and tests if they have the same ready sets
 */
  private boolean traceSubset(NodePair np,Map<AutomatonNode, NextMap > a1N,Map<AutomatonNode, NextMap > a2N, List<NodePair> processed) {
    //System.out.println("traceSubset "+ np.first.getId()+" "+np.second.getId()+" ");
    //System.out.println("Processed size "+ processed.size());
    //System.out.println("Processed "+processed.stream().map(x->x.getFirst().getId()+" "+x.getSecond().getId()+", ").reduce("",(x,y)->x+y+" "));

    //processed only used to stop algorithm running for ever with cyclic automata
    for(NodePair n: processed){
      if (n.getFirst().getId().equals(np.getFirst().getId()) &&
        n.getSecond().getId().equals(np.getSecond().getId())) {
        return true;
      }
    }
    processed.add(np);
    //if (processed.size()> 10) return false;
    if (a1N.get(np.first).labels().containsAll(a2N.get(np.second).labels())) {
     // boolean sofar = true; //int i = 0;
      for(String lab: a2N.get(np.second).labels()){
        //System.out.print(lab+" ");
        AutomatonNode nd1 = a1N.get(np.first).getNcs().get(lab);
        AutomatonNode nd2 = a2N.get(np.second).getNcs().get(lab);
        if (lab.equals("Start")) continue;
        if (lab.equals("STOP")) continue;
        if (traceSubset(new NodePair(nd1,nd2),a1N,a2N, processed) == false) return false;
       // i++; if (i>9) break;
      }

    } else {

      System.out.println(a1N.get(np.first).labels()+ " NOTsubset "+a2N.get(np.second).labels());
      return false;
    }
    return true;
  }

  /*
    Build the ready set for each node (used as test in recursive traceSubset)
    This is NOT used to take the next step
    This will call the recursive quiescentNext for tt QuiescentTrace
   */
  private Map<AutomatonNode, NextMap > build_readyMap(Automaton a, TraceType tt){
    System.out.println("Build Ready Map");
    Map<AutomatonNode, NextMap > nfanode2ASet = new HashMap< >();
    for(AutomatonNode n : a.getNodes()) {
      NextMap as;
      if (tt.equals(TraceType.QuiescentTrace)) {
        as= new NextMap(quiescentNext(n,new TreeSet<>()));
      } else {
        as = new NextMap(n.getOutgoingEdges().stream().
          distinct().
          map(x -> new NextComponent(x.getLabel(), x.getTo())).
          collect(Collectors.toSet()));
      }

      if ((tt.equals(TraceType.CompleteTrace)
       // || tt.equals(TraceType.QuiescentTrace)
      ) && n.isTerminal()) {as.ncs.put("STOP",n);}  //EEEck
      if (n.isStartNode()) {as.ncs.put("Start",n);}
      System.out.println(n.getId()+" > "+as.myString());
      nfanode2ASet.put(n,as);
    }
    return nfanode2ASet;
  }
  /*
      recurse down input events and accumulate the out going output events
   */
  private Set<NextComponent> quiescentNext(AutomatonNode node,Set<AutomatonNode> processed){
     Set<NextComponent> sofar = new TreeSet<>();
     if (node.isSTOP()) sofar.add(new NextComponent("Stop",node));
    //System.out.println("quiescentNext "+node.getId());
    for (AutomatonEdge ed: node.getOutgoingEdges()){
      if (!ed.getLabel().endsWith("?"))
        sofar.add(new NextComponent(ed.getLabel(),ed.getTo()));
    }
    processed.add(node);
    for (AutomatonEdge ed: node.getOutgoingEdges()){
      if (ed.getLabel().endsWith("?") && !isin(ed.getTo(),processed))
       sofar.addAll(quiescentNext(ed.getTo(),processed));
    }
    //System.out.println(sofar.stream().map(x->x.action+" "+x.getTo().getId()).reduce((x,y)->x+" "+y));
     return sofar;
  }

  private boolean isin(AutomatonNode node, Set<AutomatonNode> processed){

    for(AutomatonNode nd: processed) {
      if (nd.getId().equals(node.getId())) return true;
    }
    return false;
  }
  @Getter
  public static class NodePair {
    AutomatonNode first;
    AutomatonNode second;
    public NodePair(AutomatonNode n1, AutomatonNode n2){
      first = n1;
      second = n2;
    }
    String myString(){
      return first.getId()+" "+second.getId();
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



    public String myString() {
      return action + " " + to.getId();
    }

  }
  /*     Works ONLY for nfa
    NextMap maps a label to the Automaton node its leads to
   */
  @Getter
  public static class NextMap  {

    Map<String,AutomatonNode> ncs = new TreeMap<>();
    public NextMap(Set<NextComponent> in) {
      Map<String,AutomatonNode> out = new TreeMap<>();
      for(NextComponent nc: in){
        out.put(nc.action,nc.getTo());
      }
      ncs = out;
    }
    public Set<String> labels(){
      return this.ncs.keySet();
    }
    public boolean equalLabels(NextMap ns) {
      return this.labels().equals(ns.labels());
    }

    public boolean subsetLabels(NextMap ns){
      return ns.labels().containsAll(this.labels());
    }
    public String myString(){
      StringBuilder sb = new StringBuilder();
      for(String key: ncs.keySet()){
        sb.append(key+"->"+ncs.get(key).getId()+", ");
      }
      return sb.toString();
    }
  }
}
