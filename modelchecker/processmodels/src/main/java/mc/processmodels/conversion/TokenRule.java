package mc.processmodels.conversion;

import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import lombok.SneakyThrows;
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

    Automaton outputAutomaton = new Automaton(convertFrom.getId() + " automata",
        false);
      System.out.println("\nTOKEN RULE  STARTING "+convertFrom.getId());

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
if(j++>50) {System.out.println("\n\nTokenRule Failure Looping = "+j+"\n\n");break;} // second LofC  NEVER Called - looks redundent!
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
      if (satisfiedPostTransitions.size() == 0) {
          markingToNodeMap.get(currentMarking).setTerminal("STOP"); // Error states must be marked with a delta
     /*   boolean stop = true;
        for (PetriNetPlace pl: currentMarking) {
          if (!pl.getTerminal().equals("STOP")) stop = false;
        }
        if (stop)
          markingToNodeMap.get(currentMarking).setTerminal("STOP");
        else
          markingToNodeMap.get(currentMarking).setTerminal("ERROR"); */
      }
 //System.out.println("currentMarking1 "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));

      //System.out.println("satisfiedPostTransitions "+ satisfiedPostTransitions.size());
      for (PetriNetTransition transition : satisfiedPostTransitions) {
        //System.out.println("transition "+transition.myString());
          //System.out.println("outgoing "+transition.getOutgoing().size());
         Multiset<PetriNetPlace> newMarking = HashMultiset.create(currentMarking);
          //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
          //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        // Clear out the places in the current marking which are moving token
        for(PetriNetPlace pl : transition.pre()){
          newMarking.remove(pl);
        }
          //System.out.println("outgoing "+transition.getOutgoing().size());
   //System.out.println("newMarking1 "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
        newMarking.addAll(transition.getOutgoing().stream()
            .map(PetriNetEdge::getTo)
            .map(PetriNetPlace.class::cast)
            .collect(Collectors.toList()));
   //System.out.println("newMarking "+newMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));

        if (!markingToNodeMap.containsKey(newMarking)) {
          AutomatonNode newNode = outputAutomaton.addNode();
          newNode.setLabelNumber(nodesCreated++);
          markingToNodeMap.put(newMarking, newNode);
          nodeToMarkingMap.put(newNode, newMarking);
          if (!toDo.contains(newMarking)) toDo.add(newMarking);
        }
        Set<String> own =  transition.getOwners();
        AutomatonEdge ed =
        outputAutomaton.addEdge(transition.getLabel(), markingToNodeMap.get(currentMarking),
            markingToNodeMap.get(newMarking), null, false);
        ed.setEdgeOwners(own);
        //System.out.println(" automaton now "+outputAutomaton.myString());
      }
 //System.out.println("currentMarking2 "+currentMarking.stream().map(x->x.getId()+", ").collect(Collectors.joining()));

        if (!previouslyVisitedPlaces.contains(currentMarking)) { previouslyVisitedPlaces.add(currentMarking);}
        //System.out.println("todo size "+toDo.size());
  //System.out.println("Add to Previous "+previouslyVisitedPlaces.size()+"  "+currentMarking.stream().map(x->x.getId()+" ").collect(Collectors.joining()));
    }
   //System.out.println("Token Out "+outputAutomaton.myString());
    return outputAutomaton;
  }

  private static Set<PetriNetTransition> satisfiedTransitions(Multiset<PetriNetPlace> currentMarking) {
      Set<PetriNetTransition> out = post(currentMarking).stream() //88
        .filter(transition -> currentMarking.containsAll(transition.pre()))
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
