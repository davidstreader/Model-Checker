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
    AutomatonNode root = null;

//       Setup = for all roots nodes rnd
//             + rnd->newMarking + aToDo
   root = a.getRoot().iterator().next();

    Stack<Petrinet> subNets = new Stack<>();
/*
   Build,one for each owner,  projection mappings from nodes to a  SLICE
    */
   System.out.println("Owners " + a.getOwners());
    for (String own : a.getOwners()) {
     System.out.println("\n >>Owner "+ own);
     Petrinet petri = new Petrinet(a.getId(), false);
     Stack<AutomatonNode> toDo = new Stack<>();
     Stack<AutomatonNode> processed = new Stack<>();
     toDo.add(root);
     //BUILD the nd2Pl Mapp
     Map<AutomatonNode, PetriNetPlace> nd2Pl = new HashMap<>();
     boolean first = true;
     while (!toDo.isEmpty()) {
      AutomatonNode nd = toDo.pop();
      System.out.println("Start nd " + nd.getId());
      if (processed.contains(nd)) continue;
      processed.add(nd);
      if (!nd2Pl.containsKey(nd)) {
       PetriNetPlace added = petri.addPlace();
       if (first) petri.addRoot(added);
       first = false;
       Set<AutomatonNode> clump = reach(ain, nd, own);
       System.out.println("Clump " + clump.stream().map(x -> x.getId() + ", ").collect(Collectors.joining()));
       for (AutomatonNode n : clump) {
        if (!nd2Pl.containsKey(n)) nd2Pl.put(n, added);
        if (n.isTerminal()&& n.getTerminal().equals("STOP")) added.setTerminal("STOP");
       }
      }
      Set<AutomatonNode> next = nd.getIncomingEdges().stream().map(ed -> ed.getFrom()).collect(Collectors.toSet());
      next.addAll(nd.getOutgoingEdges().stream().map(ed -> ed.getTo()).collect(Collectors.toSet()));
      toDo.addAll(next);
      System.out.println("Next " + next.stream().map(x -> x.getId() + ", ").collect(Collectors.joining()));
     }
     //Use the nd2Pl Mapp to build the projected automaton
     toDo.clear();
     processed.clear();
     toDo.push(root);
     while (!toDo.isEmpty()) {
      AutomatonNode nd = toDo.pop();
      System.out.println("Start 2 nd " + nd.getId());
      if (processed.contains(nd)) continue;
      processed.add(nd);

      for (AutomatonEdge ed : nd.getOutgoingEdges()) {
       toDo.push(ed.getTo());
       System.out.println("    Start 2 " + ed.myString() + " own " + own);

       if (ed.getOwnerLocation().contains(own)) {
        System.out.println("Staring " + ed.getId());
        PetriNetTransition tran = petri.addTransition(ed.getLabel());
        petri.addEdge(tran, nd2Pl.get(ed.getFrom()), Collections.singleton(own));
        petri.addEdge(nd2Pl.get(ed.getTo()), tran, Collections.singleton(own));
        System.out.println("Adding " + tran.myString());
       } else {
        System.out.println("par edge " + ed.myString());
       }
      }
     }
      //System.out.println(petri.myString());
      System.out.println("Slice Net = " + petri.myString());
      petri = PetrinetReachability.removeUnreachableStates(petri);
      subNets.push(petri.copy());  // Clones
      System.out.println(subNets.size()+ " Slice Net ");

    }
    System.out.println("\n   OWNERS Rule Stacked "+subNets.size()+"    *********");
    Petrinet build = new Petrinet(a.getId(), false);
    while(!subNets.isEmpty()) {
     System.out.println(subNets.size()+" Adding");
     // build = PetrinetParallelMergeFunction.compose(build, subNets.pop());  //Debuging
      build = PetrinetParallelFunction.compose(build, subNets.pop());
    //  build = subNets.pop();  //for debugging
    }
     build.deTagTransitions();

    System.out.println("  OWNERS Rule *END "+build.myString());
    build = PetrinetReachability.removeUnreachableStates(build);
    //System.out.println("reach *END "+build.myString());
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


  //* build a new marking and add to the nd2Mark mapping and to the aToDo list
//  deltaMarking(oldnd, newnd, owner)
  private static  Map<String, PetriNetPlace> deltaMarking( AutomatonEdge edge,
                                                           Petrinet petri){
    //printaN2Marking();

    //System.out.println("delta edge "+edge.myString());
    AutomatonNode oldnd = edge.getFrom();
    AutomatonNode newnd = edge.getTo();
    Set<String> owner = edge.getOwnerLocation();
    if (aN2Marking.containsKey(newnd))  {
      return aN2Marking.get(newnd);
    }

    aN2Marking.putIfAbsent(newnd,buildFrom(aN2Marking.get(oldnd),owner,petri));
    todo.todoPush(newnd);
    //System.out.println("MARK delta"+ newnd.getId()+"->"+aN2Marking.get(newnd).toString());

    /* System.out.print(" |>  New "+newnd.getId()+"->{");
     for(PetriNetPlace pl:aN2Marking.get(newnd).values() ){
       System.out.print(pl.getId()+" ");
     }System.out.println("}"); */
    return aN2Marking.get(newnd);
  }



//
  /**
   * NOT a pure function buids a marking from an old marking and an owner
   * SIDE effect - adds a Place to the Petri Net!
   * @param oldMark  an OLD Marking
   * @param ownerSet  the owners that need replacing
   * @param petri  the petriNet that the new placed need to be added to
   * @return the new marking
   */
  private static  Map<String, PetriNetPlace> buildFrom(Map<String, PetriNetPlace> oldMark,
                                                       Set<String> ownerSet,
                                                       Petrinet petri){
    //System.out.print("BuildFrom "+mark2String(oldMark));
    Map<String, PetriNetPlace> newMark = new HashMap<>();
    for(String o: oldMark.keySet()){
      if (ownerSet.contains(o)){
        newMark.putIfAbsent(o,petri.addPlace());  // NOT new if square
      } else {
        newMark.putIfAbsent(o,oldMark.get(o));
      }
    }
    //System.out.println(" New "+mark2String(newMark));
    return newMark;
  }

  //
  // Build the marking and set aN2Marking  for the forth node in a square
  private static   void findSquare(AutomatonEdge currentEdge, Automaton a,
                                   Petrinet petri) throws CompilationException {
    Set<AutomatonEdge> fromSet = new HashSet<>(currentEdge.getFrom().getOutgoingEdges());
    //System.out.println("findSquare "+currentEdge.myString());
    for(AutomatonEdge edge: fromSet){
      //System.out.println("  find   edge "+edge.myString());

      Set<String> cap = currentEdge.getOwnerLocation();
      cap.retainAll(edge.getOwnerLocation());
      if (cap.isEmpty()){  //concurrent edges
        deltaMarking(edge,petri);  // third node in square
        //System.out.println(" found "+edge.getTo().getId()+" "+currentEdge.myString());
        setTo(edge.getTo(),currentEdge,petri,a);
        return;
      }
    }
    return;
  }
  /**
   *   Given an edge and a node that is the start of a parallel node
   *   Build the marking of the forth node  and SET aN2Marking
   * @param one
   * @param edge
   * @param a
   * @return
   */
  private static void setTo(AutomatonNode one,
                            AutomatonEdge edge,
                            Petrinet petri,
                            Automaton a) throws CompilationException {
    AutomatonNode two = edge.getTo();
    String label = edge.getLabel();
    Set<String> owner = edge.getOwnerLocation();
    for(AutomatonEdge ed: one.getOutgoingEdges()){
      if (ed.getLabel().equals(label)  && ed.getOwnerLocation().equals(owner)) {
        //forth node in square
        Map<String, PetriNetPlace> newMark = new HashMap<>();
        for (String o : a.getOwners()) {
          if (owner.contains(o)) {
            newMark.putIfAbsent(o, aN2Marking.get(two).get(o));  // NOT new if square
          } else {
            newMark.putIfAbsent(o, aN2Marking.get(one).get(o));
          }
        }
        // may already be defined
        if (aN2Marking.containsKey(edge.getTo())) {
          //System.out.println("MARK Forth node clash "+aN2Marking.get(edge.getTo())+" XX "+newMark.toString());
          //make colletions disjoint
          Set<PetriNetPlace> set1 = new HashSet<>();
          for(PetriNetPlace pl: aN2Marking.get(edge.getTo()).values() ) {
            if(newMark.values().contains(pl)) continue;
            set1.add(pl);
          }
          Set<PetriNetPlace> set2 = new HashSet<>();
          for(PetriNetPlace pl: newMark.values() ) {
            if(aN2Marking.get(edge.getTo()).values().contains(pl)) continue;
            set2.add(pl);
          }
          //System.out.println("Forth node clash "+set1.toString()+" YY "+set2.toString());
          petri.gluePlaces(set1,set2 );
          return;
        } else {
          //System.out.println("MARK Forth node fresh "+aN2Marking.get(ed.getTo())+" XX "+newMark.toString());
          aN2Marking.putIfAbsent(ed.getTo(), newMark);
        }
      }
    }
    return;
  }



  private static  void printaN2Marking() {
    System.out.println("aN2Marking");
    for(AutomatonNode nd : aN2Marking.keySet()) {
      System.out.println("  "+nd.getId()+" =>> ");
      for (String k:  aN2Marking.get(nd).keySet()){
        System.out.println("    "+k+" own2place "+aN2Marking.get(nd).get(k).myString()); ;
      }
    }
  }




private static Set<AutomatonNode> reach(Automaton a, AutomatonNode ndi, String own) {
   Set<AutomatonNode> reached = new HashSet<>();

   Stack<AutomatonNode> sofar = new Stack<>();
   sofar.push(ndi);

   while(!sofar.isEmpty()) {
    AutomatonNode nd = sofar.pop();
//System.out.println("reachfrom "+nd.getId());
    if(reached.contains(nd)) continue;
    reached.add(nd);

    Set<AutomatonNode> union = new HashSet<>();
    for(AutomatonEdge ed: nd.getIncomingEdges()){

     if(!ed.getOwnerLocation().contains(own)){
      if(!reached.contains(ed.getFrom())) {
       union.add(ed.getFrom());
       sofar.push(ed.getFrom());
      }
     }
    }
    for(AutomatonEdge ed: nd.getOutgoingEdges()){

     if(!ed.getOwnerLocation().contains(own)){
      if(!reached.contains(ed.getTo())) {
       union.add(ed.getTo());
       sofar.push(ed.getTo());
      }
     }
    }
/*
    Set<AutomatonNode> union = new HashSet<>(nd.getOutgoingEdges().stream().
      filter(ed->!ed.getOwnerLocation().contains(own)).map(e->e.getTo()).collect(Collectors.toSet()));

    union.addAll(nd.getIncomingEdges().stream().
      filter(ed->!ed.getOwnerLocation().contains(own)).map(e->e.getFrom()).collect(Collectors.toSet()));
    */
 //System.out.println("All "+ union.stream().map(x->x.getId()+", ").collect(Collectors.joining()));
    reached.addAll(union);

   }

   return reached;
}


  private static class todoStack {
    private  Stack<AutomatonNode> stackit = new Stack<>();

    public void todoPush(AutomatonNode nd) {
      if (stackit.contains(nd)) {
        System.out.println("stackit Duplicate "+nd.myString());
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
