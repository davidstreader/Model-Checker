package mc.processmodels.conversion;

import java.util.*;
import java.util.function.Predicate;


import lombok.SneakyThrows;
import mc.exceptions.CompilationException;

import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.automata.operations.PetrinetParallelFunction;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
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
   * <p>
   * stackit: Make this conversion work for NFA
   *
   * @param a The automaton to be converted
   * @return a petrinet representation fo the automaton
   */
  @SneakyThrows({CompilationException.class})
  public static  Petrinet ownersRule(Automaton a) {
    clean();
    System.out.println("OWNERSRule " + a.toString());
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
   From one Place for each distinct node we need
   one Place for each set of nodes in a slice!
    */

    for (String own : a.getOwners()) {
      Automaton small = new Automaton("Small",false);
      Stack<AutomatonNode> toDo = new Stack<>();
      Set<AutomatonNode> done = new HashSet<>();
      Map<AutomatonNode, AutomatonNode> phase2 = new TreeMap<>();
      System.out.println("\n  OWNER 1 = " + own);
      toDo.add(root);
      while (!toDo.isEmpty()) {
        AutomatonNode nd = toDo.pop();
        //System.out.println("WHILE "+nd.getId());

        if (done.contains(nd)) continue;
        done.add(nd);

        for (AutomatonEdge ed : nd.getOutgoingEdges()) {
          toDo.push(ed.getTo());
          System.out.println("    EDGE "+ed.myString()+" own "+own);

          if (ed.getOwnerLocation().contains(own)) {
            System.out.println("    State change "+ ed.getTo().getId());
            //small.addEdge(ed.getLabel(),ed.getFrom(),ed.getTo(),ed.getGuard(),true);
          } else {
            // In Slice so no change in Place
            phase2.putIfAbsent(nd, ed.getTo());
            System.out.println("phase2 "+nd.getId()+"->"+ed.getTo().getId());
          }
        }
        toDo.remove(nd);
      }
      System.out.println(printPhase2(phase2));
      //TODO construct the transitive closure for three+ nets
      System.out.println("stackit rewrite rewrites");
      System.out.println("\n");

      toDo = new Stack<>();
      done = new HashSet<>();
      Set<AutomatonEdge> edgeDone = new HashSet<>();
      //System.out.println("  OWNER 2 = " + own);
      Map<AutomatonNode,PetriNetPlace> nd2Pl = new HashMap<>();
      toDo.add(root);
      //PetriNetPlace rp = petri.addPlace();
      //nd2Pl.put(root, rp);
      //petri.addRoot(rp);
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
            //apply state change
            //System.out.println(printPhase2(phase2));
            AutomatonNode nd1 = ed.getFrom();
            AutomatonNode nd2 = ed.getTo();

            if (phase2.containsKey(nd1)) nd1 = phase2.get(nd1);
            if (phase2.containsKey(nd2)) nd2 = phase2.get(nd2);
            AutomatonEdge temp = new AutomatonEdge(ed.getId(), ed.getLabel(), nd1,nd2);
            //System.out.println("     STATE |> "+ temp.myString());
            //if not processed add transition
            if (!edgeDone.contains(temp)) {
              edgeDone.add(temp);
              PetriNetPlace next = null;
              if (!nd2Pl.containsKey(nd1)) {
                next = petri.addPlace();
                nd2Pl.putIfAbsent(nd1, next);
                if (nd1.isTerminal())  next.setTerminal(nd1.getTerminal());
              } else {
                next = nd2Pl.get(nd1);
              }
              if (first) {
                petri.addRoot(nd2Pl.get(nd1));
                first = false;
              }
              PetriNetPlace toNode = null;
              if (!nd2Pl.containsKey(nd2)) {
                toNode = petri.addPlace();
                nd2Pl.putIfAbsent(nd2, toNode);
                if (nd2.isTerminal())  next.setTerminal(nd2.getTerminal());
              } else {
                toNode = nd2Pl.get(nd2);
              }

              PetriNetTransition tran = petri.addTransition(ed.getLabel());
              petri.addEdge(tran,next,Collections.singleton(own));
              petri.addEdge(toNode,tran,Collections.singleton(own));
              System.out.println(tran.myString());
            }
          } else {
            // In Slice so skip
          }
        }
        toDo.remove(nd);
      }
      System.out.println(petri.myString());
      System.out.println("END of OWNER valid = "+petri.validatePNet());
      subNets.push(petri.copy());  // Clones
      petri = new Petrinet(a.getId(), false);
    }
    System.out.println("\n   OWNERS Rule Stacked "+subNets.size()+"    *********");
    Petrinet build = new Petrinet(a.getId(), false);
    if (!subNets.isEmpty()) { build = subNets.pop(); }
    while(!subNets.isEmpty()) {
      build = PetrinetParallelFunction.compose(build, subNets.pop());
    }


    System.out.println("\n  OWNERS Rule END "+build.myString()+"\n  *********\n");
    return build;
  }

  @SneakyThrows({CompilationException.class})
  public static  Petrinet XXownersRule(Automaton a) {
    clean();
    System.out.println("OWNERSRule " + a.toString());
    Petrinet petri = new Petrinet(a.getId(), false);
    PetriNetPlace p = null;
    Stack<AutomatonNode> toDo = new Stack<>();
    AutomatonNode root = null;

//       Setup = for all roots nodes rnd
//             + rnd->newMarking + aToDo
    for(AutomatonNode r : a.getRoot()) {
      for(String o: a.getOwners()) {
        root = r;
        break;
      }
    }
/*
   From one Place for each distinct node we need
   one Place for each set of nodes in a slice!
    */

    for (String own : a.getOwners()) {
      Set<AutomatonNode> done = new HashSet<>();
      Map<AutomatonNode, PetriNetPlace> visited = new TreeMap<>();
      Map<PetriNetPlace, PetriNetPlace> phase2 = new TreeMap<>();
      System.out.println("\n  OWNEROWNER = " + own);
      p = petri.addPlace();
      petri.addRoot(p);
      toDo.add(root);
      visited.putIfAbsent(root,p);
      while (!toDo.isEmpty()) {
        PetriNetPlace toNode = p;
        PetriNetTransition tran;
        AutomatonNode nd = toDo.pop();
        System.out.println("WHILE "+nd.getId());

        if (done.contains(nd)) continue;
        done.add(nd);
        PetriNetPlace next;
        System.out.println("Vis "+printVisited(visited));
        if (visited.containsKey(nd)) {
          next = visited.get(nd);
          System.out.println("  Found "+nd.getId()+"->"+visited.get(nd).getId());
        }  else {
          next = petri.addPlace();
          visited.putIfAbsent(nd,next);
          System.out.println("  Visted "+nd.getId()+"->"+visited.get(nd).getId());
        }

        for (AutomatonEdge ed : nd.getOutgoingEdges()) {
          toDo.push(ed.getTo());

          System.out.println("    EDGE "+ed.myString()+" own "+own);

          if (ed.getOwnerLocation().contains(own)) {
            System.out.println("    State change "+ ed.getTo().getId());
            tran = petri.addTransition(ed.getLabel());
            System.out.println("    vis "+printVisited(visited)+ "  look for "+ ed.getTo().getId());
            if (visited.containsKey(ed.getTo())) {
              toNode = visited.get(ed.getTo());
              System.out.println("Found "+nd.getId()+"->"+toNode.getId() );
            }
            else {
              System.out.println(" NOT Found");
              toNode = petri.addPlace();
              visited.putIfAbsent(ed.getTo(),toNode);
              System.out.println("VISITED 1 "+ed.getTo().getId()+"->"+toNode.getId());
              System.out.println("Vis "+printVisited(visited));
            }
            System.out.println("Trn "+tran.myString());
            System.out.println("next "+next.myString());
            System.out.println("toNode "+toNode.myString());
            petri.addEdge(next,tran,Collections.singleton(own));
            petri.addEdge(tran,toNode,Collections.singleton(own));
            System.out.println("Built "+tran.myString());
          } else {
            // In Slice so no change in Place
            if (toNode == null) {
              System.out.println("ownersRule: toNode null should not be posible!");
            } else {
              visited.putIfAbsent(ed.getTo(),visited.get(nd));
              System.out.println("VISITED 2 "+ed.getTo().getId()+"->"+visited.get(nd).getId());
              System.out.println("Vis "+printVisited(visited));
              //if (visited.containsKey(ed.getTo()))
              phase2.putIfAbsent(visited.get(nd), visited.get(ed.getTo()));
              System.out.println("phase2 ");
            }
          }
        }
        toDo.remove(nd);
      }
      System.out.println("\n OWNER "+own);
      //System.out.println(printPhase2(phase2));
      System.out.println("\n\n");
    }
    System.out.println("OWNERS Rule END "+petri.myString());
    return petri;
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
      out = out +" "+nd.getId()+"->"+v.get(nd).getId()+",";
    }
    return out+"}";
  }

  /*
O2Place == Map(owner->Place), Markings == O2Place.values()
   Setup = for all roots nodes rnd
         + rnd->newMarking + aToDo
   While aTodo  != {}
         curernt = aToDo.top
         if current processed skip
            for each edge from Current
               push edge.getTo to aToDo
               if edge not processed
                  build parSet for edge
                  mark all in parset as processed
                  build transition

with a->c->STOP || b->STOP

to prevent two b transitions being added either all three b events need ot be marked as done
or nd->Marking needs to be constructed to prevent trans being added twice

 */


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

    System.out.println("delta edge "+edge.myString());
    AutomatonNode oldnd = edge.getFrom();
    AutomatonNode newnd = edge.getTo();
    Set<String> owner = edge.getOwnerLocation();
    if (aN2Marking.containsKey(newnd))  {
      return aN2Marking.get(newnd);
    }

    aN2Marking.putIfAbsent(newnd,buildFrom(aN2Marking.get(oldnd),owner,petri));
    todo.todoPush(newnd);
    System.out.println("MARK delta"+ newnd.getId()+"->"+aN2Marking.get(newnd).toString());

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
    System.out.print("BuildFrom "+mark2String(oldMark));
    Map<String, PetriNetPlace> newMark = new HashMap<>();
    for(String o: oldMark.keySet()){
      if (ownerSet.contains(o)){
        newMark.putIfAbsent(o,petri.addPlace());  // NOT new if square
      } else {
        newMark.putIfAbsent(o,oldMark.get(o));
      }
    }
    System.out.println(" New "+mark2String(newMark));
    return newMark;
  }

  //
  // Build the marking and set aN2Marking  for the forth node in a square
  private static   void findSquare(AutomatonEdge currentEdge, Automaton a,
                                   Petrinet petri) throws CompilationException {
    Set<AutomatonEdge> fromSet = new HashSet<>(currentEdge.getFrom().getOutgoingEdges());
    System.out.println("findSquare "+currentEdge.myString());
    for(AutomatonEdge edge: fromSet){
      System.out.println("  find   edge "+edge.myString());

      Set<String> cap = currentEdge.getOwnerLocation();
      cap.retainAll(edge.getOwnerLocation());
      if (cap.isEmpty()){  //concurrent edges
        deltaMarking(edge,petri);  // third node in square
        System.out.println(" found "+edge.getTo().getId()+" "+currentEdge.myString());
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
          System.out.println("MARK Forth node clash "+aN2Marking.get(edge.getTo())+" XX "+newMark.toString());
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
          System.out.println("Forth node clash "+set1.toString()+" YY "+set2.toString());
          petri.gluePlaces(set1,set2 );
          return;
        } else {
          System.out.println("MARK Forth node fresh "+aN2Marking.get(ed.getTo())+" XX "+newMark.toString());
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
