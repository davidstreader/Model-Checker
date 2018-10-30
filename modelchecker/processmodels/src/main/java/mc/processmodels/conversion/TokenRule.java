package mc.processmodels.conversion;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import lombok.SneakyThrows;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetEdge;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;


/**
 * This holds static methods related to the generation of automata from petrinets.
 * <p>
 * This is to be used such that the conversion between the two can be completed, and for being able
 * to make petrinets and automata interoperable within process definitions.
 *
 * For hand shake events the token rule is:
 *    A transition t is fired, and produces an automata event, only when its pre places, *t, are all marked. When a transition
 *    is fired the tokens on the preplaces *t are removed and places on the post places t* are added.
 * For brodcast events the behaviour of a send event b! can not be blocked by the lack of a synchronising receive event b?
 *    Synchronised send-receive transitions are labeled b! and the edges added because of the synchronising b? event are marked as "optional"
 *    Thus Let *t = {1,2} where edge 2->t is optional and  t* = {3,4} where edge t->4 is optional behaves:
 *       If {1,2} is marked then after t is fired {3,4} is marked.
 *       Elseif {1} is marked then after t is fired {3} is marked.
 *
 * @author Jordan Smith
 * @author David Streader
 * @author Jacob Beal
 * @see <a href="http://doi.org/10.1006/inco.2002.3117">
 * The Box Algebra = Petri Nets + Process Expressions
 * </a>
 * @see Petrinet
 * @see PetriNetPlace
 * @see Automaton
 * @see AutomatonNode
 */
public class TokenRule {

  private static int stateSizeBound = 100;

  public static Automaton tokenRule(Petrinet convertFrom){
    return tokenRule(convertFrom, new HashMap<>(), new HashMap<>());
  }
  /**
   * This method statically converts from a Petrinet to an Automaton visualisation of a given
   * process.
   *
   * @param convertFrom the petrinet that is converted from.
   * @param markingToNodeMap the mapping from the marking to an automaton node, used for display
   * @param nodeToMarkingMap the mapping from node to marking
   * @return The automaton that is equivalent to {@code convertFrom} petrinet.
   */
  @SneakyThrows(value = {CompilationException.class})
  public static Automaton tokenRule(Petrinet convertFrom,
                                    Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNodeMap,
                                    Map<AutomatonNode, Multiset<PetriNetPlace> > nodeToMarkingMap) {
 String  nameOnly = convertFrom.getId().replaceAll("[0-9]*$", "");
 //keeps the id numbers low - op_eval resets Automaton.tagid
    Automaton outputAutomaton = new Automaton(convertFrom.getId() ,false);
    //Automaton outputAutomaton = new Automaton(nameOnly+Automaton.tagid  ,false);
    //System.out.println("TOKEN RULE  STARTING "+convertFrom.getId());

      assert convertFrom.validatePNet(): "Token precondition";
   outputAutomaton.setOwners(convertFrom.getOwners());
   //System.out.println(convertFrom.getRoots());
    List<Set<PetriNetPlace>> rootsPlaces = new ArrayList<Set<PetriNetPlace>>();

    for(Set<String> rnames: convertFrom.getRoots()) {
    //System.out.println("rnames "+ rnames);
      AutomatonNode root = outputAutomaton.addNode();
      //System.out.println("root "+root.myString());
      root.setStartNode(true);
      outputAutomaton.addRoot(root);
      Set<PetriNetPlace> rts = rnames.stream().
        map(x-> convertFrom.getPlaces().get(x)).collect(Collectors.toSet());
      rootsPlaces.add(rts);
      Multiset<PetriNetPlace> rtms = HashMultiset.create(rts);
      markingToNodeMap.put(rtms, root);
      nodeToMarkingMap.put(root, rtms);
      //System.out.println("root "+root.myString());
    }

  //System.out.println("rootsPlaces "+ rootsPlaces);
    Stack<Multiset<PetriNetPlace>> toDo = new Stack<>();
    for(Set<PetriNetPlace> rs: rootsPlaces) {
      toDo.add(HashMultiset.create(rs));
    }

    Set<Multiset<PetriNetPlace>> previouslyVisitedPlaces = new HashSet<>();
    int nodesCreated = 1;
int j = 0; //without these 2 LofC loop never terminates!
    while (!toDo.isEmpty()) {
if(j++> stateSizeBound) {System.out.println("\n\nTokenRule Failure Looping = "+j+"\n\n");break;} // second LofC  NEVER Called - looks redundent!
        Multiset<PetriNetPlace> currentMarking = toDo.pop();
  //System.out.println("currentMarking "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));

        //System.out.print("\nStarting  prev " +previouslyVisitedPlaces.size()+" todo "+toDo.size()+  " \n");
    /*  for(PetriNetPlace pl :currentMarking){
         //System.out.print(pl.getId()+" ");
      } */
       //System.out.println("");

      if (previouslyVisitedPlaces.contains(currentMarking)) {
        //System.out.println("Visted!");
        continue;
      }

        //System.out.println("currentMarking "+currentMarking);
      Set<PetriNetTransition> satisfiedPostTransitions = satisfiedTransitions(currentMarking); //88
      //System.out.println("Processing "+Petrinet.marking2String(currentMarking)+
      //" trans "+satisfiedPostTransitions.size());
          if (currentMarking.stream().map(x->x.isSTOP()).reduce(true,(x,y)->x&&y)) {
            markingToNodeMap.get(currentMarking).setStopNode(true);
            //System.out.println("Mark as STOP "+markingToNodeMap.get(currentMarking).getId());
           // outputAutomaton.getEndList().add(markingToNodeMap.get(currentMarking).getId());
          }
          else if (currentMarking.stream().map(x->x.isERROR()).reduce(false,(x,y)->x || y)) {
            markingToNodeMap.get(currentMarking).setErrorNode(true);
          }

 //System.out.println("currentMarking1 "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));

      //System.out.println("satisfiedPostTransitions "+ satisfiedPostTransitions.size());
      for (PetriNetTransition transition : satisfiedPostTransitions) {
        //System.out.println("  Satisfied transition "+transition.myString());
          //System.out.println("outgoing "+transition.getOutgoing().size());
         Multiset<PetriNetPlace> newMarking = HashMultiset.create(currentMarking);
          //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
          //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        // Clear out the places in the current marking which are moving token

          /* get owners of the optional Places not marked*/
          Multiset<PetriNetPlace> opunMarked = HashMultiset.create(transition.pre());
          opunMarked.removeAll(currentMarking);
          Set<String> opOwn = opunMarked.stream()
                   .map(x->x.getOwners())
                   .flatMap(x->x.stream())
                   .collect(Collectors.toSet());
          //System.out.println("  opOwn "+opOwn);
        for(PetriNetPlace pl : transition.pre()){  // includes optional preplaces
          newMarking.remove(pl);
        }
          //System.out.println("newMarking "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));

          /* filter out the post places of owners with  unmarked preplaces*/

          //System.out.println("outgoing "+transition.getOutgoing().size());
   //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        newMarking.addAll(transition.getOutgoing().stream()
                .filter(ed->!((ed.getOptional() &&
                        opOwn.containsAll(((PetriNetPlace)ed.getTo()).getOwners())  )))    //
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .collect(Collectors.toList()));
   //System.out.println("newMarking "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));

        if (!markingToNodeMap.containsKey(newMarking)) {
          AutomatonNode newNode = outputAutomaton.addNode();
          newNode.setLabelNumber(nodesCreated++);
          //System.out.println(" add "+ Petrinet.marking2String(newMarking)+"->"+newNode.getId());
          markingToNodeMap.put(newMarking, newNode);
          nodeToMarkingMap.put(newNode, newMarking);
          if (!toDo.contains(newMarking)) {
              toDo.add(newMarking);
              //System.out.println("  toDo Add Marking "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
              }
        }
        Set<String> own =  transition.getOwners();
        AutomatonEdge ed =
        outputAutomaton.addEdge(transition.getLabel(), markingToNodeMap.get(currentMarking),
            markingToNodeMap.get(newMarking), null, false,false);
        ed.setEdgeOwners(own);
        //System.out.println("  adding edge "+ed.myString());
      }
 //System.out.println("currentMarking2 "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));

        if (!previouslyVisitedPlaces.contains(currentMarking)) { previouslyVisitedPlaces.add(currentMarking);}
        //System.out.println("todo size "+toDo.size());
  //System.out.println("Add to Previous "+previouslyVisitedPlaces.size()+"  "+currentMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
    }  //Built  Automata now set End list to be the same as the Petri EndList
    //Map<Multiset<PetriNetPlace>, AutomatonNode> markingToNodeMap

  for (Set<String> mark: convertFrom.getEnds()) {
    Set<PetriNetPlace> mk = mark.stream().map(x->convertFrom.getPlaces().get(x)).collect(Collectors.toSet());

    Multiset<PetriNetPlace> mkm = HashMultiset.create(mk);
    //System.out.print("\n** End Multiset ");mkm.stream().forEach(x->System.out.print(x.getId()+", "));System.out.println("");
    if (!markingToNodeMap.containsKey(mkm)) {
      //System.out.println(" Stop not reached "+ mark+ " NOT found ");
    } else {
      outputAutomaton.addEnd(markingToNodeMap.get(mkm).getId());
    }
  }
  //added 20Sept 2018 for Failure Testing
   for (AutomatonNode nd : outputAutomaton.getNodes()) {
      if (nd.getOutgoingEdges().size()==0 && !nd.isSTOP()) {
        nd.setErrorNode(true);
     }
   }
   //System.out.println("Token Rule END"); //Out "+outputAutomaton.myString());
    return outputAutomaton;
  }


  public static Set<PetriNetTransition> satisfiedTransitions(Multiset<PetriNetPlace> currentMarking) {
      Set<PetriNetTransition> out = post(currentMarking).stream() //88
            //  .filter(transition -> currentMarking.containsAll(transition.pre()))
              .filter(transition -> currentMarking.containsAll(transition.preNonBlocking())) // drops the optional preplaces
              .distinct()
        .collect(Collectors.toSet());
      //System.out.println(out.stream().map(x->x.getId()).reduce("satisfied ",(x,y)->x+y+" "));
      return out;
  }


  private static Set<PetriNetTransition> post(Multiset<PetriNetPlace> currentMarking) {
    if (currentMarking == null) return Collections.EMPTY_SET;
    Set<PetriNetTransition> out = new HashSet<>();
    for (PetriNetPlace pl : currentMarking){
      //System.out.println(pl);
      //System.out.println(pl.post());
      if (pl.post()!= null && pl.post().size()>0)
          out.addAll(pl.post().stream().collect(Collectors.toSet()));
    }
    if (out == null) return Collections.EMPTY_SET;
    else  return out;
  }

}
