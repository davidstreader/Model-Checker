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
  alphabet of abstract may be a subset of the alphabet of the concrete But this requires
  that the overall alphabet be known

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

     Add listening loops and call methods from trace refinement
     First add listening loops and Quiescent events then follow the trace subset algorithm

     PARAMETERISED subSetParam
   */


  public boolean evaluate(Set<String> flags,
                          Context context,
                          Collection<ProcessModel> processModels,
                          TraceType tt,  // only controls the fake events added in nfa2dfa
                          Stack<String> trace,
                          SubSetDataConstructor buildData, //lambda to build data for comparision
                          SubSetEval eval  // lambda for evaluation
      ) throws CompilationException {
//Called directly from Trace Refinement and with some preprocessing from Quiescent Refinement
    boolean cong = flags.contains(Constant.CONGURENT);
    //System.out.println("TraceWorks START flags "+flags+" "+tt);
    if (processModels.iterator().next() instanceof Automaton) {
      Nfa2dfaWorks nfa2dfaworks = new Nfa2dfaWorks();
      ArrayList<ProcessModel> dfas = new ArrayList<>();
      int i = 1;
      for (ProcessModel pm : processModels) { i++;//display only
        Automaton a;
        if (flags.contains(Constant.FAIR) || flags.contains(Constant.UNFAIR)) {
          AbstractionFunction absFun = new AbstractionFunction();
          a = absFun.compose(pm.getId(), flags, context, (Automaton) pm);
        } else {
          a = (Automaton) pm;
        }

        try {
          //System.out.println("TraceWorks nfa "+a.myString());
          Automaton newdfa; //BUILD DFA
          newdfa = nfa2dfaworks.compose(  a.getId(),  flags,  context,  tt,
            buildData,  //lambda to build data for later comparision
            a);
          //System.out.println("DFA " + i + " " +flags+" "+tt.toString());
          //System.out.println("DFA " + i + " " + newdfa.myString());
          dfas.add(newdfa);
        } catch (CompilationException e) {
          System.out.println("PINGO" + e.toString());
          throw e;
        }
      }
      Automaton a1 = (Automaton) dfas.get(0);
      Automaton a2 = (Automaton) dfas.get(1);
      System.out.println("TwX dfa a1 "+a1.myString());
      System.out.println("TwX dfa a2 "+a2.myString());

      //System.out.println("Q Refinement type " + tt + " flags " + flags + " 1 = " + a1.getId() + " 2 = " + a2.getId());
//Both dfas are built
      a1Next = build_readyMap(a1, tt, cong); //needed to control the simulation
      a2Next = build_readyMap(a2, tt, cong);
      System.out.println("a1Next "+a1Next.myString());
      System.out.println("a2Next "+a2Next.myString());
      //The nfas are anotated with Ready sets
      //System.out.println(a2.myString());
      AutomatonNode r1 = (AutomatonNode) a1.getRoot().toArray()[0];
      AutomatonNode r2 = (AutomatonNode) a2.getRoot().toArray()[0];

      //System.out.println("?   " + a1.readySets2String(cong) + " " + a2.readySets2String(cong));
      boolean b;
      //System.out.println("a1N\n" + ready2String(a2Next.getMap()));
      //System.out.println("a2N\n" + ready2String(a1Next.getMap()));
      //Recursive  Algorithm - a2Next.getMap() is BOTH the readset to be checked and where to go next
      Stack<String> tr = new Stack<>();

        System.out.println("Tw dfa a1 "+a1.myString());
        System.out.println("Tw dfa a2 "+a2.myString());
      b = traceSubset(a1, a2, new NodePair(r1, r2), a1Next.getMap(), a2Next.getMap(),
        new ArrayList<>(), cong, trace, tt, eval);
      System.out.println("top traceSubset returns "+b+ "  trace "+trace);
      return b;
    }
    //System.out.print("\nTrace semantics not defined for type " + processModels.iterator().next().getClass() + "\n");
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
  private boolean traceSubset(Automaton dfa1, Automaton dfa2,  // getNode2ReadySets() for is subset
                              NodePair np,
                              Map<AutomatonNode, NextMap> a1N, // ONLY where to go next
                              Map<AutomatonNode, NextMap> a2N,
                              List<NodePair> processed,
                              boolean cong,
                              Stack<String> trace,  //output trace investigating for error messges
                              TraceType tt,
                              SubSetEval evalSubset  //look at TraceRefinment, QuiescentRefinement
     ) throws CompilationException {
    boolean ok = true;
    System.out.println("traceSubset start with nodePair " + np.myString() + "  tt " + tt);
    for (NodePair n : processed) {
      if (n.getFirst().getId().equals(np.getFirst().getId()) &&
        n.getSecond().getId().equals(np.getSecond().getId())) {
        //System.out.println(" Already processed but trace = "+trace);
        return true;
      }
    }
    //System.out.println(a2N.get(np.second).labels() + " in " + a1N.get(np.first).labels());
    // List<Set<String>> sm = dfa2.getNode2ReadySets().get(np.second);

    /*System.out.println(np.second.getId() + " small= " + dfa2.getNode2ReadySets().get(np.second) + " AND " +
                       np.first.getId()  + " large= " + dfa1.getNode2ReadySets().get(np.first) + " ");
    */
     /*System.out.println(evalSubset.op(dfa2.getNode2ReadySets().get(np.second),
      dfa1.getNode2ReadySets().get(np.first), cong));*/
    //2 a sub(tract,failure,..) of 1
    ErrorMessage error=  new ErrorMessage("");
    if (evalSubset.op(dfa1.getNode2ReadySets().get(np.first),
                      dfa2.getNode2ReadySets().get(np.second),
                      cong, error)) {
      //System.out.println("Trace Works TRUE next " + a1N.get(np.first).labels());
      processed.add(np);
      for (String lab : a2N.get(np.second).labels()) {  // not small
        //System.out.println("with " +np.first.getId() + " exploring  " + np.second.getId() + "->" + lab);
       // if (Constant.external(lab) || lab.equals(Constant.Quiescent)) continue;  // dose not include Quiescent
        if (lab.equals(Constant.Quiescent)) continue;  // dose not include Quiescent
        //see returning to root in cribsheet
        //System.out.println(" starting " + np.myString() + " lab = " + lab);
        AutomatonNode nd1 = a1N.get(np.first).getNcs().get(lab);
        AutomatonNode nd2 = a2N.get(np.second).getNcs().get(lab);
        //System.out.println();
        if (nd2 == null) {
          //System.out.println("ERROR ERROR ");
          Throwable t = new Throwable();
          t.printStackTrace();
          throw new CompilationException(this.getClass(),"ERROR in TRACEWORKS");
          //return false;
        } else if (nd1 == null) {
          //System.out.println(dfa1.getId() + " 1 cannot match event " + lab + " that " + dfa2.getId() + " 2 performs");
          trace.push(lab);
          ok = false;
          //System.out.println("Tw 183 ok "+ok+"  trace " + trace);
          break;
          //return false; // 1 cannot match an event from 2! hence 2 not SUB 1
        }
        if (ok) {
          //System.out.println("next nd1 = " + nd1.getId() + " nd2 = " + nd2.getId());
          ok = traceSubset(dfa1, dfa2, new NodePair(nd1, nd2), a1N, a2N, processed, cong, trace, tt, evalSubset);
          if (!ok)  trace.push(lab);
          //System.out.println("Tw 191 ok "+ok+"  trace " + trace);
        }
        //System.out.println("1ok "+ok);
      } //END of for loop
      //System.out.println("2ok "+ok);
    } else {
      trace.push(error.error);
      //System.out.println(np.myString() + " TRACEWORKS 2 \\subseteq 1 is  false 2 " + dfa2.getNode2ReadySets().get(np.second) +
      //  " NOTsubset 1 " + dfa1.getNode2ReadySets().get(np.first)+"  trace "+trace);
      ok = false;
      //System.out.println("3ok "+ok);
    }
    //System.out.println(np.myString() + "traceSubset end with trace = " + trace + " ok = " + ok);
    return ok;
  }


  /*
    Build the ready set for each node (used as test in recursive traceSubset)
    This is NOT used to take the next step
    This will call the recursive quiescentNext for tt QuiescentTrace
    For non congurance STOP is add recursivly in quiescentNext
   */
  private Nd2NextMap build_readyMap(Automaton a, TraceType tt, boolean cong) {
    //System.out.println("Build Ready Map " + tt + " cong= " + cong);
    //System.out.println("Automaton " + a.myString());
    //System.out.println(a.myString());
    Nd2NextMap nfanode2ASet = new Nd2NextMap();
    for (AutomatonNode n : a.getNodes()) {
      NextMap nm = new NextMap(n.getOutgoingEdges().stream().
        distinct().
        map(x -> new NextComponent(x.getLabel(), x.getTo())).
        collect(Collectors.toSet()));
      nfanode2ASet.getMap().put(n, nm);
    }

    //System.out.println("nfanode2ASet "+ nfanode2ASet.myString());
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


    public Set<String> labels() {
      return this.ncs.keySet();
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
