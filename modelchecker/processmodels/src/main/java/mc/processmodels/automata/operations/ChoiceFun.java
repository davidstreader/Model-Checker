package mc.processmodels.automata.operations;

import com.google.common.collect.Multimap;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;

import java.util.*;
import java.util.stream.Collectors;

public class ChoiceFun {


  /**
   * Execute the function.
   *
   * @param id         the id of the resulting automaton
   * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
   * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting automaton of the operation
   */
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2) throws CompilationException {
    //System.out.println("\n *********************\n");
    //System.out.println("a1 "+automaton1.myString());
    //System.out.println("a2 "+automaton2.myString());
    Automaton sequence = new Automaton(id, !Automaton.CONSTRUCT_ROOT);

    Multimap<String,String> setOfOwners = AutomatonEdge.createIntersection(automaton1, automaton2);

    //store a map to the nodes so id can be ignored
    Map<String, AutomatonNode> automata1nodes = new HashMap<>();
    Map<String, AutomatonNode> automata2nodes = new HashMap<>();

  //  System.out.println("Sequence aut1 "+ automaton1.toString());
  //  System.out.println("Sequence aut2 "+ automaton2.toString());
    //copy node1 nodes across
    AutomataReachability.removeUnreachableNodes(automaton1).getNodes().forEach(node -> {
      try {
        AutomatonNode newNode = sequence.addNode();

        newNode.copyProperties(node);
        automata1nodes.put(node.getId(), newNode);

        if (newNode.isStartNode()) {
          sequence.addRoot(newNode);
        }

      } catch (CompilationException e) {
        e.printStackTrace();
      }
    });

  //  System.out.println("Sequence 1 "+sequence.toString());

    copyAutomataEdges(sequence, automaton1, automata1nodes, setOfOwners);


    //get the stop nodes such that they can be replaced
    Collection<AutomatonNode> stopNodes = sequence.getNodes().stream()
        .filter(n -> "STOP".equals(n.getTerminal()))
        .collect(Collectors.toList());

    //if there are no stop nodes, we cannot glue them together
    if (stopNodes.isEmpty()) {
      return sequence;
    }
    /*System.out.print("stopNodes "+stopNodes.stream().
         map(x->x.getId()).reduce("{",(x,y)->x=x+" "+y)+"}"); */

//below copies the automaton hence renames the nodes
    AutomataReachability.removeUnreachableNodes(automaton2).getNodes().forEach(node -> {
      AutomatonNode newNode = sequence.addNode();
      newNode.copyProperties(node);

      automata2nodes.put(node.getId(), newNode);

      if (newNode.isStartNode()) {
        newNode.setStartNode(false);
        // for every stop node of automata1, get the edges that go into it
        // replace it with the start node of automata2
        for (AutomatonNode stopNode : stopNodes) {
          if(stopNode.getIncomingEdges().size() == 0) {// If automaton 1 is only a stop node
            newNode.setStartNode(true);
          }


          for (AutomatonEdge edge : stopNode.getIncomingEdges()) {
            AutomatonNode origin = edge.getFrom();
     //System.out.println("last "+edge.myString());
            try {
              sequence.addOwnersToEdge(
                  sequence.addEdge(edge.getLabel(), origin, newNode,
                      edge.getGuard() == null ? null : edge.getGuard().copy(),
                      false), edge.getOwnerLocation());
            } catch (CompilationException e) {
              e.printStackTrace();
            }
          }
        }
      }
    });

    stopNodes.stream().map(AutomatonNode::getIncomingEdges)
        .flatMap(List::stream)
        .forEach(sequence::removeEdge);
    stopNodes.forEach(sequence::removeNode);
   // System.out.println("Sequence 2 "+ automaton2.toString());

    copyAutomataEdges(sequence, automaton2, automata2nodes,setOfOwners);
  //  System.out.println("End Seq   "+sequence.toString());
    return sequence;
  }

  /**
   * clone the nets
   * remove STOP from net1 Places and Start from net2 Places
   * glue
   * reset root from start.
   *
   * @param id        the id of the resulting petrinet
   * @param net1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param net2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting petrinet of the operation
   */

  public Petrinet compose(String id, Petrinet net1, Petrinet net2)
    throws CompilationException {
   /* System.out.println("[]PETRI1 "+net1.myString());
   net1.validatePNet();
   System.out.println("[]PETRI2 "+net2.myString());
   net2.validatePNet();*/
   //clone nets
   Petrinet petrinet1 = net1.copy();
   Petrinet petrinet2 = net2.copy();
// build set for gluing (leave the start ON
   Set<PetriNetPlace> startOfP1 = new HashSet<>();
   for(PetriNetPlace pl2: petrinet1.getPlaces().values()){
    if (pl2.isStart()) {
     startOfP1.add(pl2);
    }
   }
   Set<PetriNetPlace> startOfP2 = new HashSet<>();
   for(PetriNetPlace pl2: petrinet2.getPlaces().values()){
    if (pl2.isStart()) {
     startOfP2.add(pl2);
    }
   }
   //Do the owners
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
   }
   //create an empty petrinet
   Petrinet composition = new Petrinet(id, false);

   composition.getOwners().clear();
   composition.getOwners().addAll(petrinet1.getOwners());
   composition.getOwners().addAll(petrinet2.getOwners());

   //prior to Gluing join the nets (do not add as that changes the names

   composition.joinPetrinet(petrinet1);


   //add the second petrinet;
   //System.out.println("start1 "+ Petrinet.marking2String(startOfP1));
   //System.out.println("start2 "+ Petrinet.marking2String(startOfP2));
   composition.joinPetrinet(petrinet2);


   //merge the end of petri1 with the start of petri2
   composition.gluePlaces(startOfP1, startOfP2 );
   composition.setRoot2Start();

   Set<PetriNetPlace> end1 = petrinet1.getPlaces().values().stream()
     .filter(x->x.isTerminal()).collect(Collectors.toSet());
   Set<PetriNetPlace> end2 = petrinet2.getPlaces().values().stream()
     .filter(x->x.isTerminal()).collect(Collectors.toSet());
 System.out.println("end1 "+end1 + " end2 "+end2);
   composition.gluePlaces(end1, end2 );

   //System.out.println("Add OUT "+ composition.myString()+"\n");
   composition.validatePNet();
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
