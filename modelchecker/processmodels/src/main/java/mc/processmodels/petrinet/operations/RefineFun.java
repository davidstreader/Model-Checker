package mc.processmodels.petrinet.operations;

import com.google.common.collect.Multimap;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;

import java.util.*;
import java.util.stream.Collectors;

public class RefineFun {



  /**
   * clone the nets
   * remove STOP from net1 Places and Start from net2 Places
   * glue
   * reset root from start.
   *
   * @param id        the id of the resulting petrinet
   * @param act  the action label to be refined
   * @param net1 the first  petrinet in the function (e.g. {@code A} in {@code A/{B/act}})
   * @param net2 the second petrinet in the function (e.g. {@code B} in {@code A/{B/act}})
   * @return the resulting petrinet of the operation
   */

  public Petrinet compose(String id, String act, Petrinet net1, Petrinet net2)
    throws CompilationException {
   System.out.println("id = "+id+" act = "+act);
    System.out.println("/{/}PETRI1 "+net1.myString());
   net1.validatePNet();
   System.out.println("/{/}PETRI2 "+net2.myString());
   net2.validatePNet();

   //clone nets
   Petrinet petrinet1 = net1.copy();
   Set<PetriNetTransition> trs = petrinet1.getTransitions().values().stream().
     filter(t->t.getLabel()!= null && t.getLabel().equals(act)).
     collect(Collectors.toSet());
   System.out.println("Trans = "+trs.stream().map(x->x.myString()+"/n").collect(Collectors.joining()));
   //create an empty petrinet
   Petrinet composition = new Petrinet(id, false);
   composition.joinPetrinet(petrinet1);

   for(PetriNetTransition tr: trs) {
    System.out.println("Tran "+tr.myString());
    Petrinet petrinet2 = net2.copy();  // need new copy each event
// build set for gluing (leave the start ON
    Set<PetriNetPlace> preEvents =  tr.pre();
    Set<PetriNetPlace> postEvents =  tr.post();
 System.out.println("pre "+preEvents.stream().map(x->x.getId()+" ").collect(Collectors.joining())+
                "  post "+postEvents.stream().map(x->x.getId()+" ").collect(Collectors.joining()));

    Set<PetriNetPlace> startOfP2 = new HashSet<>();
    for (PetriNetPlace pl2 : petrinet2.getPlaces().values()) {
     if (pl2.isStart()) {
      startOfP2.add(pl2);
      pl2.setStart(false);
     }
    }
    petrinet2.setRoot(Collections.emptySet());

    /*Do the owners
    if (petrinet1.getOwners().contains(Petrinet.DEFAULT_OWNER)) {
     petrinet1.getOwners().clear();
     for (String eId : petrinet1.getEdges().keySet()) {
      Set<String> owner = new HashSet<>();
      owner.add(petrinet1.getId());
      petrinet1.getEdges().get(eId).setOwners(owner);
     }
    }

    if (petrinet2.getOwners().contains(Petrinet.DEFAULT_OWNER)) {
     petrinet2.getOwners().clear();
     for (String eId : petrinet2.getEdges().keySet()) {
      Set<String> owner = new HashSet<>();
      owner.add(petrinet2.getId());
      petrinet2.getEdges().get(eId).setOwners(owner);
     }
    } */

    composition.getOwners().clear();
    composition.getOwners().addAll(petrinet1.getOwners());
    composition.getOwners().addAll(petrinet2.getOwners());

    //prior to Gluing join the nets (do not add as that changes the ids)
    //composition.joinPetrinet(petrinet2);   FAILS with A/{A/x}

    Map<PetriNetPlace, PetriNetPlace> mapping = composition.addPetrinetNoOwner(petrinet2);
    Set<PetriNetPlace> newstart2 = startOfP2.stream().map(x->mapping.get(x)).collect(Collectors.toSet());


    //merge the end of petri1 with the start of petri2
System.out.println("PRE   "+ tr.pre().stream().map(x->x.getId()+" ").collect(Collectors.joining())
               +" \n start "+ newstart2.stream().map(x->x.getId()+" ").collect(Collectors.joining()));

    composition.gluePlaces(preEvents, newstart2, true);

    Set<PetriNetPlace> end2 = petrinet2.getPlaces().values().stream()
      .filter(x -> x.isTerminal()).collect(Collectors.toSet());
    Set<PetriNetPlace> newend2 = end2.stream().map(x->mapping.get(x)).collect(Collectors.toSet());

    for (PetriNetPlace pl2 : petrinet2.getPlaces().values()) {
      pl2.setTerminal("");

    }
    postEvents =  tr.post();
    System.out.println("POST  " +
      tr.post().stream().map(x->x.getId()+" ").collect(Collectors.joining()) +
                    " \n end2 " +
           end2.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
    composition.gluePlaces(postEvents, newend2, false);

    composition.setRoot2Start();

    composition.removeTransition(tr);
    composition.validatePNet();
   }

   System.out.println("ADD OUT "+ composition.myString()+"\n");

   return composition;
  }


 /**
   * Copies the edges from one automata to another.
   *
   * @param writeAutomaton the automata that will have the edges copied to it
   * @param readAutomaton  the automata that will have the edges copied from it
   * @param nodeMap        the mapping of the ids to AutomatonNodes
   */
  private void copyAutomataEdges(Automaton writeAutomaton, Automaton readAutomaton,
                                 Map<String, AutomatonNode> nodeMap,
                                 Multimap<String,String> edgeOwnersMap) throws CompilationException{


    for(AutomatonEdge readEdge : readAutomaton.getEdges()) {
      AutomatonNode fromNode = nodeMap.get(readEdge.getFrom().getId());
      AutomatonNode toNode = nodeMap.get(readEdge.getTo().getId());
        writeAutomaton.addOwnersToEdge(
                writeAutomaton.addEdge(readEdge.getLabel(), fromNode, toNode, readEdge.getGuard(), false),
                getEdgeOwnersFromProduct(readEdge.getOwnerLocation(), edgeOwnersMap)
        );
    }
  }

  private Set<String> getEdgeOwnersFromProduct(Set<String> edgeOwners,
                                               Multimap<String,String> productSpace) {
    return edgeOwners.stream().map(productSpace::get)
        .flatMap(Collection::stream)
        .collect(Collectors.toSet());
  }

}
