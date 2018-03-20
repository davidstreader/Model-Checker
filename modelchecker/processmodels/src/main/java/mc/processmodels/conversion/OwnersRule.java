package mc.processmodels.conversion;

import java.util.*;
import java.util.function.Predicate;


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

    Throwable t = new Throwable();
    t.printStackTrace();
    System.out.println("OWNERSRule " + a.myString());
    Petrinet petri = new Petrinet(a.getId(), false);
    PetriNetPlace p = null;
    AutomatonNode root = null;

//       Setup = for all roots nodes rnd
//             + rnd->newMarking + aToDo
    for(AutomatonNode r : a.getRoot()) {
      for(String o: a.getOwners()) {
        root = r;
        break;
      }
    }
    Stack<Petrinet> subNets = new Stack<>();
/*
   Build,one for each owner,  projection mappings from nodes to a  SLICE
    */

    for (String own : a.getOwners()) {
      Automaton small = new Automaton("Small",false);
      Stack<AutomatonNode> toDo = new Stack<>();
      Set<AutomatonNode> done = new HashSet<>();
      /*
      Build the projection onto each SLICE
       */
      Map<AutomatonNode, AutomatonNode> phase2 = new TreeMap<>();
      //System.out.println("\n  OWNER 1 = " + own);
      toDo.add(root);
      //first build a one step mapping towards the slice
      while (!toDo.isEmpty()) {
        AutomatonNode nd = toDo.pop();
        //System.out.println("WHILE "+nd.getId());

        if (done.contains(nd)) continue;
        done.add(nd);

        for (AutomatonEdge ed : nd.getOutgoingEdges()) {
          toDo.push(ed.getTo());
          //System.out.println("    EDGE "+ed.myString()+" own "+own);

          if (ed.getOwnerLocation().contains(own)) {
            //System.out.println("    State change "+ ed.getTo().getId());
            //small.addEdge(ed.getLabel(),ed.getFrom(),ed.getTo(),ed.getGuard(),true);
          } else {
            // In Slice so no change in Place
            phase2.putIfAbsent(nd, ed.getTo());
            System.out.println("phase2 "+nd.getId()+"->"+ed.getTo().getId());
          }
        }
        toDo.remove(nd);
      }
      System.out.println("***********");
      System.out.println(printPhase2(phase2));
      //TODO construct the transitive closure ending at the slice
      System.out.println("STACKIT rewrite rewrites");

      toDo = new Stack<>();
      done = new HashSet<>();
      Set<AutomatonEdge> edgeDone = new HashSet<>();
      //System.out.println("  OWNER 2 = " + own);
      Map<AutomatonNode,PetriNetPlace> nd2Pl = new HashMap<>();
      toDo.add(root);
      /*  if (nd2Pl.containsKey(root)) {
          next = nd2Pl.get(root);
        }*/
      boolean first = true;
      while (!toDo.isEmpty()) {
        AutomatonNode nd = toDo.pop();
        //System.out.println("WHILE 2 "+nd.getId()+" "+printVisited(nd2Pl));
        if (done.contains(nd)) continue;
        done.add(nd);

        for (AutomatonEdge ed : nd.getOutgoingEdges()) {

          toDo.push(ed.getTo());
          System.out.println("    Start 2 "+ed.myString()+" own "+own);

          if (ed.getOwnerLocation().contains(own)) {
            System.out.println("Staring "+ ed.getId());
            AutomatonNode nd1 = new AutomatonNode("1");
            AutomatonNode nd2 = new AutomatonNode("2");

            if (phase2.containsKey(ed.getFrom())) nd1 = phase2.get(ed.getFrom());
            else nd1 = ed.getFrom();
            if (phase2.containsKey(ed.getTo())) nd2 = phase2.get(ed.getTo());
            else nd2 = ed.getTo();
     System.out.println("nd1 -> "+nd1.getId()+"  nd2 -> "+nd2.getId());
            AutomatonEdge temp = new AutomatonEdge(ed.getId(), ed.getLabel(), nd1,nd2);
            //System.out.println("     STATE |> "+ temp.myString());
            //if not processed add transition
            if (!edgeDone.contains(temp)) {
              edgeDone.add(temp);
              PetriNetPlace fromPlace = null;
              if (!nd2Pl.containsKey(nd1)) {
                fromPlace = petri.addPlace();
                nd2Pl.putIfAbsent(nd1, fromPlace);
                if (phase2.containsKey(nd1)) {
                  nd2Pl.putIfAbsent(phase2.get(nd1), fromPlace);
                  System.out.println("ADDing "+phase2.get(nd1).getId()+"->"+fromPlace.getId());
                }
                System.out.println("ADDing "+nd1.getId()+"->"+fromPlace.getId());
                if (nd1.isTerminal()) {
                  fromPlace.setTerminal(nd1.getTerminal());
                  //System.out.println("1 SET "+nd1.getId()+"TERMINAL ->"+next.myString());
                }
              } else {
                fromPlace = nd2Pl.get(nd1);
              }
              if (first) {
                petri.addRoot(nd2Pl.get(nd1));
                first = false;
              }
              PetriNetPlace toPlace = null;
              if (!nd2Pl.containsKey(nd2)) {
                toPlace = petri.addPlace();
                nd2Pl.putIfAbsent(nd2, toPlace);
                if (phase2.containsKey(nd2)) {
                  nd2Pl.putIfAbsent(phase2.get(nd2), toPlace);
                  System.out.println("ADDing "+phase2.get(nd2).getId()+"->"+toPlace.getId());
                }
                System.out.println("ADDing "+nd2.getId()+"->"+toPlace.getId());
                if (nd2.isTerminal())  {
                  toPlace.setTerminal(nd2.getTerminal());
                  //System.out.println("2 SET "+nd2.getId()+"TERMINAL ->"+toNode.myString());
                }
              } else {
                toPlace = nd2Pl.get(nd2);
              }

              PetriNetTransition tran = petri.addTransition(ed.getLabel());
              petri.addEdge(tran,fromPlace,Collections.singleton(own));
              petri.addEdge(toPlace,tran,Collections.singleton(own));
              System.out.println("Adding "+ tran.myString());
            } else {
              System.out.println("Done "+ ed.myString());
            }
          } else {
            System.out.println("Skip "+ ed.myString());
          }
        }
        toDo.remove(nd);
      }
      //System.out.println(petri.myString());
      System.out.println("END of OWNER valid = "+petri.myString());
      petri = PetrinetReachability.removeUnreachableStates(petri);
      subNets.push(petri.copy());  // Clones
      petri = new Petrinet(a.getId(), false);
    }
    System.out.println("\n   OWNERS Rule Stacked "+subNets.size()+"    *********");
    Petrinet build = new Petrinet(a.getId(), false);
    if (!subNets.isEmpty()) { build = subNets.pop(); }
    while(!subNets.isEmpty()) {
     // build = PetrinetParallelMergeFunction.compose(build, subNets.pop());  //Debuging
      build = PetrinetParallelFunction.compose(build, subNets.pop());
    //  build = subNets.pop();  //for debugging
    }


    System.out.println("  OWNERS Rule *END "+build.myString());
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

  private static  void markStopPlaces(Petrinet p) {
    p.getPlaces().values().stream()
      .filter(pl -> pl.getOutgoing().size() == 0)
      .filter(((Predicate<PetriNetPlace>) PetriNetPlace::isTerminal).negate())
      .forEach(pl -> pl.setTerminal("STOP"));
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
