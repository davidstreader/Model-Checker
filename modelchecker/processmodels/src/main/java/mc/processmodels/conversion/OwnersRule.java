package mc.processmodels.conversion;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


import lombok.SneakyThrows;
import mc.exceptions.CompilationException;

import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.PetrinetParallelFunction;
import mc.processmodels.automata.operations.PetrinetParallelMergeFunction;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.operations.PetrinetReachability;

//import mc.operations.impl.PetrinetParallelFunction;
public class OwnersRule {

  private  static Map<AutomatonNode ,Map<String, PetriNetPlace>> aN2Marking = new HashMap<>();
  private  static todoStack todo  = new todoStack();
  private static Map<String, AutomatonEdge> processed = new HashMap<>(); // No new Peri transition needed

  private static Set<AutomatonNode> ndProcessed;
  private static void clean(){
    aN2Marking = new HashMap<>();
    todo = new todoStack();
    ndProcessed = new HashSet<>();
    processed = new HashMap<>();
  }

  /**
   * This converts a deterministic automata to a petrinet.
   * Works for nondet processes TODO extend to initial nondet. A * B
   * For each owner O1 project the automata to a SLICE automata(Net)
   * i.e. a net built from  edges with owners containing O1
   *
   * Build the final net  as the parallel composition of all the slices!
   *
   * TODO: Make this conversion work for NFA
   *
   * @param ain The automaton to be converted
   * @return a petrinet representation fo the automaton
   */
  @SneakyThrows({CompilationException.class})
  public static  Petrinet ownersRule(Automaton ain) {
    clean();
    Automaton a = ain.copy(); // smaller Ids make debugging easier
    a.tagEvents();
    //Throwable t = new Throwable();
    //t.printStackTrace();
   System.out.println("\nOWNERSRule " + a.myString());
    PetriNetPlace p = null;
    //AutomatonNode root = null;

   //root = a.getRoot().iterator().next();

    Stack<Petrinet> subNets = new Stack<>();
/*
   Build,one for each owner,  projection mappings from nodes to a  SLICE
    */
   //System.out.println("Owners " + a.getOwners());
    for (String own : a.getOwners()) {
     //System.out.println("\n >>Owner "+ own);
     Petrinet petri = new Petrinet(a.getId(), false);
     Stack<AutomatonNode> toDo = new Stack<>();
     Stack<AutomatonNode> processed = new Stack<>();
     toDo.addAll(a.getRoot());
     //BUILD the nd2Pl Mapp
     Map<AutomatonNode, PetriNetPlace> nd2Pl = new HashMap<>();
     boolean first = true;
     while (!toDo.isEmpty()) {
      AutomatonNode nd = toDo.pop();
      //System.out.println("Start nd " + nd.getId());
      if (processed.contains(nd)) continue;
      processed.add(nd);
      if (!nd2Pl.containsKey(nd)) {
       PetriNetPlace added = petri.addPlace();
       if (first) petri.addFirstRoot(added);
       first = false;
       Set<AutomatonNode> clump = reach(ain, nd, own);
       //System.out.println("Clump " + clump.stream().map(x -> x.getId() + ", ").collect(Collectors.joining()));
       for (AutomatonNode n : clump) {
        if (!nd2Pl.containsKey(n)) nd2Pl.put(n, added);
        if (n.isTerminal()&& n.getTerminal().equals("STOP")) added.setTerminal("STOP");
       }
      }
      Set<AutomatonNode> next = nd.getIncomingEdges().stream().map(ed -> ed.getFrom()).collect(Collectors.toSet());
      next.addAll(nd.getOutgoingEdges().stream().map(ed -> ed.getTo()).collect(Collectors.toSet()));
      toDo.addAll(next);
      //System.out.println("Next " + next.stream().map(x -> x.getId() + ", ").collect(Collectors.joining()));
     }
     //Use the nd2Pl Mapp to build the projected automaton
     toDo.clear();
     processed.clear();
     toDo.addAll(a.getRoot());
     while (!toDo.isEmpty()) {
      AutomatonNode nd = toDo.pop();
      //System.out.println("Start 2 nd " + nd.getId());
      if (processed.contains(nd)) continue;
      processed.add(nd);

      for (AutomatonEdge ed : nd.getOutgoingEdges()) {
       toDo.push(ed.getTo());
       //System.out.println("    Start 2 " + ed.myString() + " own " + own);

       if (ed.getOwnerLocation().contains(own)) {
        //System.out.println("Staring " + ed.getId());
        PetriNetTransition tran = petri.addTransition(ed.getLabel());
        petri.addEdge(tran, nd2Pl.get(ed.getFrom()), Collections.singleton(own));
        petri.addEdge(nd2Pl.get(ed.getTo()), tran, Collections.singleton(own));
        //System.out.println("Adding " + tran.myString());
       } else {
        //System.out.println("par edge " + ed.myString());
       }
      }
     }
      System.out.println("Unreach Next "+petri.myString());
      //System.out.println("Slice Net = " + petri.myString());
      petri = PetrinetReachability.removeUnreachableStates(petri);
      System.out.println("Pushing Net "+petri.myString());
      subNets.push(petri.copy());  // Clones
      //System.out.println(subNets.size()+ " Slice Net ");

    }
    System.out.println("\n   OWNERS Rule Stacked "+subNets.size()+"    *********");
    Petrinet build = subNets.pop();
    while(!subNets.isEmpty()) {
    //System.out.println(subNets.size()+" Adding");
    //  build = PetrinetParallelMergeFunction.compose(build, subNets.pop());  //Debuging
      build = PetrinetParallelFunction.compose(build, subNets.pop());
    //  build = subNets.pop();  //for debugging
    System.out.println("  While "+build.myString());
    }
     build.deTagTransitions();

   //System.out.println("OWNERS Rule *END "+build.myString());
    build = PetrinetReachability.removeUnreachableStates(build);
   System.out.println("reach *END "+build.myString());
    return build;
  }


  private static String printVisited(Map<AutomatonNode, PetriNetPlace> v){
    String out = "Visited {";
    for(AutomatonNode nd: v.keySet()){
      out = out +" "+nd.getId()+"->"+v.get(nd).getId()+",";
    }
    return out+"}";
  }
  private static String printPhase2(Map<AutomatonNode, AutomatonNode> v){
    String out = "PHASE2 {";
    for(AutomatonNode nd: v.keySet()){
      out = out +" "+nd.getId()+"->"+v.get(nd).getId()+" T "+v.get(nd).isTerminal()+",";
    }
    return out+"}";
  }




  private static String mark2String(Map<String, PetriNetPlace> mark){
    String x = "{";
    for(String k: mark.keySet()){
      x= x+" "+k+"->"+mark.get(k).getId();
    }
    return x+" }";
  }







  private static  void printaN2Marking() {
   //System.out.println("aN2Marking");
    for(AutomatonNode nd : aN2Marking.keySet()) {
      System.out.println("  "+nd.getId()+" =>> ");
      for (String k:  aN2Marking.get(nd).keySet()){
        System.out.println("    "+k+" own2place "+aN2Marking.get(nd).get(k).myString()); ;
      }
    }
  }


 /**
  * Build the set of nodes reachable by undirected edges
  * edges filtered by not containing owner own
  * @param a
  * @param ndi
  * @param own
  * @return
  */

private static Set<AutomatonNode> reach(Automaton a, AutomatonNode ndi, String own) {
   Set<AutomatonNode> processed = new HashSet<>();

   Stack<AutomatonNode> sofar = new Stack<>();
   sofar.push(ndi);

   while(!sofar.isEmpty()) {
    AutomatonNode nd = sofar.pop();
//System.out.println("reachfrom "+nd.getId());
    if(processed.contains(nd)) continue;
    processed.add(nd);

    Set<AutomatonNode> oneStep = new HashSet<>(nd.getOutgoingEdges().stream().
      filter(ed->!ed.getOwnerLocation().contains(own)).
      map(e->e.getTo()).collect(Collectors.toSet()));

    oneStep.addAll(nd.getIncomingEdges().stream().
      filter(ed->!ed.getOwnerLocation().contains(own)).
      map(e->e.getFrom()).collect(Collectors.toSet()));

 //System.out.println("All "+ union.stream().map(x->x.getId()+", ").collect(Collectors.joining()));
    sofar.addAll(oneStep);
   }

   return processed;
}


  private static class todoStack {
    private  Stack<AutomatonNode> stackit = new Stack<>();

    public void todoPush(AutomatonNode nd) {
      if (stackit.contains(nd)) {
        //System.out.println("stackit Duplicate "+nd.myString());
        return;
      } else {
        stackit.push(nd);
      }
    }
    public int todoSize() {return stackit.size();}
    public AutomatonNode todoPop(){ return stackit.pop();}
    public boolean todoIsEmpty(){ return stackit.isEmpty();}
  }

}
