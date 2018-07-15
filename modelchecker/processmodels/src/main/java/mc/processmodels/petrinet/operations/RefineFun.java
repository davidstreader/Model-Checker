package mc.processmodels.petrinet.operations;

import com.google.common.collect.ArrayListMultimap;
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
   * Fails with parallel merge of the transition to be refined! (owners Fail)
   *
   * @param id        the id of the resulting petrinet
   * @param act  the action label to be refined
   * @param net1 the first  petrinet in the function (e.g. {@code A} in {@code A/{B/act}})
   * @param net2 the second petrinet in the function (e.g. {@code B} in {@code A/{B/act}})
   * @return the resulting petrinet of the operation
   */

  public Petrinet compose(String id, String act, Petrinet net1, Petrinet net2)
    throws CompilationException {
      //clone nets
      Petrinet composition = net1.reId("1");
    System.out.println("id = "+id+" act = "+act);
    System.out.println("/{/}PETRI1 "+composition.myString());
      composition.validatePNet();
    System.out.println("/{/}PETRI2 "+net2.myString());
    net2.validatePNet();

     Set<PetriNetTransition> trs = composition.getTransitions().values().stream().
      filter(t->t.getLabel()!= null && t.getLabel().equals(act)).
      collect(Collectors.toSet());
    System.out.println("Trans = "+trs.stream().map(x->x.myString()+"\n").collect(Collectors.joining()));
    //create an empty petrinet

      int tagi = 2;
    for(PetriNetTransition tr: trs) {
      //petrinet1.removeTransition(tr);
        String tagit = ""+tagi; tagi++;
      System.out.println("  Tran " + tr.myString());
      Petrinet petrinet2 = net2.reId(tagit);  // need new copy for each event
        System.out.println("**petrinet2 "+petrinet2.myString());
// build set for gluing (leave the start ON
      Set<PetriNetPlace> preEvents = tr.pre();
      Set<PetriNetPlace> prePostEvents = tr.pre();
      Set<PetriNetPlace> postEvents = tr.post();

        prePostEvents.retainAll(postEvents);
      Set<String> prePostOwners = prePostEvents.stream().map(x->x.getOwners()).
          flatMap(Set::stream).collect(Collectors.toSet());
      System.out.println("  prePostOwneres "+prePostOwners);
      System.out.println("  pre " + preEvents.stream().map(x -> x.getId() + " ").collect(Collectors.joining()) +
        "  post " + postEvents.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));




      String tag = "Ref";
        composition.addPetrinet(petrinet2,true);  // The ids have changed
     // composition.addPetrinetNoOwner(petrinet2, tag);
      System.out.println("composition = " + composition.myString());

      composition.glueOwners(tr.getOwners(),petrinet2.getOwners() );
      //Need to rebuild the root and end
        Set<PetriNetPlace> startOfP2 = composition.getPlaces().
                values().stream().filter(x->(x.isStart()&& !x.getId().startsWith("1:") )).collect(Collectors.toSet());
        System.out.println("****startOfP2 "+ startOfP2.stream().
                map(x->(x.getId()+" ")).collect(Collectors.joining()));
        Set<PetriNetPlace> endOfP2 = composition.getPlaces().
                values().stream().filter(x->(x.isTerminal()&& !x.getId().startsWith("1:") )).collect(Collectors.toSet());
        System.out.println("****endOfP2 "+ endOfP2.stream().
                map(x->(x.getId()+" ")).collect(Collectors.joining()));

        composition.gluePlaces(preEvents, startOfP2); //newstart2);
     System.out.println("Ref one Glue above");


      for (PetriNetPlace pl2 : endOfP2){
        pl2.setTerminal("");
      }

      postEvents = tr.post();
      System.out.println("POST  " +
        tr.post().stream().map(x -> x.getId() + " ").collect(Collectors.joining()) +
        " \n endOfP2 " +
        endOfP2.stream().map(x -> x.getId() + " ").collect(Collectors.joining()));

      // for X-a->Y where X and Y disjoint  redo same owners computation
      // need  cross product of owners of intersection of pre(tr) post(tr)
      // (a,b)Cross(a,b) = {aa,ab,bb}
      //else tis the owners from past cross product
      System.out.println("\npetrinet2.getOwners() "+petrinet2.getOwners());

      Set<String> newPet2 = new HashSet<>();
      for(String o2 : petrinet2.getOwners()){
        newPet2.addAll(composition.getCombinationsTable().get(o2));
      }
      System.out.println("newPet2   "+newPet2);

      Set<String> newOwners = new HashSet<>();
      for(String o:newPet2) {
        if(composition.getCombinationsTable().keySet().contains(o) &&
          prePostOwners.contains(o)){ // must be owner of pre and post node
          newOwners.addAll(composition.getCombinationsTable().get(o));
        } else {
          newOwners.add(o);
        }
      }
      petrinet2.setOwners(newOwners);

      System.out.println("\ntr.getOwners() "+tr.getOwners());
      System.out.println("petrinet2.getOwners() "+petrinet2.getOwners()+"\n");
      //composition.glueOwners(tr.getOwners(),petrinet2.getOwners() );
      composition.gluePlaces(postEvents, endOfP2);
      System.out.println("composition.getOwners() "+composition.getOwners());
      composition.setRootFromStart();
    }

    //trs refined
    Set<PetriNetTransition> notAct = composition.getTransitions().values().stream().collect(Collectors.toSet());
    for(PetriNetTransition togo : notAct) {
       if (togo.getLabel().equals(act)) composition.removeTransition(togo);
    }
    composition.validatePNet();

    composition = PetrinetReachability.removeUnreachableStates(composition);
    System.out.println("Refine OUT "+ composition.myString()+"\n");

    return composition;
  }





}
