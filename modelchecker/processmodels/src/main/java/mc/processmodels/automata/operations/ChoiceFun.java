package mc.processmodels.automata.operations;

import com.google.common.collect.Multimap;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.operations.PetrinetReachability;

import java.util.*;
import java.util.stream.Collectors;

public class ChoiceFun {

  public static final String MAPLET = "^";
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

  //System.out.println("Sequence aut1 "+ automaton1.toString());
  //System.out.println("Sequence aut2 "+ automaton2.toString());
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

  //System.out.println("Sequence 1 "+sequence.toString());

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
              AutomatonEdge e = sequence.addEdge(edge.getLabel(), origin, newNode,
                      edge.getGuard() == null ? null : edge.getGuard().copy(),
                      false,edge.getOptionalEdge());
              sequence.addOwnersToEdge(e, edge.getOwnerLocation());
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
   //System.out.println("Sequence 2 "+ automaton2.toString());

    copyAutomataEdges(sequence, automaton2, automata2nodes,setOfOwners);
  //System.out.println("End Seq   "+sequence.toString());
    return sequence;
  }

  /**
   * * Multi Roots changes the high level structure!
   * Let Roots = List of Set of Places Ra = Sa1,Sa2,...
   * Ra [] Rb = Sa1Sb1, Sa2Sb1, ...;  Sa2Sb1,Sa2Sb2, ...; ..... where
   *   SaiSbj  =  Sai TIMES Sbj
   * When A and B have only one root   RA [] RB =  Sa1 TIMES Sb1
   *  Sai and Saj are not always disjoint when i!=j see  a||(x+y)
      for each pair of roots Ri, Rj build a Ri[]Rj processes
   *  (x+y)[]c has two c events one offered with an x evnent the other with the y event
   *  (x+y)[](a+b) had four root nodes and 8 events 2 of each x,y,a,b
   *
   * @param id        the id of the resulting petrinet
   * @param n1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param n2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting petrinet of the operation
   */


  public Petrinet compose(String id, Petrinet n1, Petrinet n2)
    throws CompilationException {
    System.out.println("[] n1 = "+n1.myString());
    Petrinet net1 = n1.reId("1");
    Set<String> own1 = net1.getOwners();
    List<Set<String>> oneEnd = net1.getEnds();
    System.out.println("[] function Start");
    System.out.println("oneEnd "+oneEnd);

    Petrinet net2 = n2.reId("2");
    Set<String> own2 = net2.getOwners();
    //System.out.println("[]PETRI1 "+net1.myString());
    assert net1.validatePNet():"choice precondition net1";
    //System.out.println("[]PETRI2 "+net2.myString());
    assert net2.validatePNet():"choice precondition net2";
    List<Set<String>> twoRoots =  new ArrayList<>();
     for(Set<String> rs: net2.getRoots()) {
       Set<String > newrs = new HashSet<>();
       for(String r:rs) {
         newrs.add(r);
       }
       twoRoots.add(newrs);
     }
    System.out.println("twoRoot "+twoRoots);
    List<Set<String>> twoEnd = net2.getEnds();

    System.out.println("twoEnd "+twoEnd);

    net2.joinNet(net1);  // concats end2 and end1
    net2.setEndFromNet();

    List<Set<String>> oneRoots =  new ArrayList<>();
    for(Set<String> rs: net1.getRoots()) {
      Set<String > newrs = new HashSet<>();
      for(String r:rs) {
        newrs.add(r);
      }
      oneRoots.add(newrs);
    } System.out.println("oneRoot " +oneRoots);


    net2.glueOwners(own1,own2);

    //for each pair of roots Ri, Rj build a Ri[]Rj root and copy the root post transition
    List<Set<String>> newRoots = new ArrayList<>();
    for (Set<String> r1: oneRoots) {
      for (Set<String> r2: twoRoots) {
        //Copy both roots and then Glue
        //composition.glueOwners(own1,own2);
        Set<PetriNetPlace> newr2 = new HashSet<PetriNetPlace>();
        for(String rt2: r2) {
          //Copy the root and post root transitions
          //System.out.println("*************to Copy "+net2.getPlace(rt2).myString());
          newr2.add(net2.copyRootOrEnd(net2.getPlace(rt2),"X"));
        }
        Set<PetriNetPlace> newr1 = new HashSet<PetriNetPlace>();
        for(String rt1: r1) {
          newr1.add(net2.copyRootOrEnd(net2.getPlace(rt1),"X"));
        }
        Map<String, String> s2s =net2.gluePlaces(newr1, newr2, false);

        System.out.println("\n glueMapping \n" +
          s2s.keySet().stream().map(x -> x + "->" + s2s.get(x) + "\n").collect(Collectors.joining()));
        Set<String> newr = s2s.values().stream().collect(Collectors.toSet());
        newRoots.add(newr);
        System.out.println("newRoots "+newRoots);
      }

        net2.setRoots(newRoots);
        net2.setStartFromRoot();
        System.out.println("[]after Glueing start  " + net2.myString() + "\n");
    }
    List<Set<String>> newList = new ArrayList<Set<String>>(twoRoots);
    newList.addAll(oneRoots);
    Set<String> toGo = new HashSet<>();
    for(Set<String> rs: newList){
      for(String r:rs){
        toGo.add(r);
      }
    }
    System.out.println(net2.myString());
    System.out.println("Removing "+toGo);
    for(String pl: toGo) {
        net2.removePlace(net2.getPlace(pl));

    }


    System.out.println("[]Add OUT "+ net2.myString("edges")+"\n");
    net2 = PetrinetReachability.removeUnreachableStates(net2);
    System.out.println("\n[] OUT "+ net2.myString()+"\n");
    //net2.reId("");
    assert net2.validatePNet(): "choice post condition";
    return net2;
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
                writeAutomaton.addEdge(readEdge.getLabel(), fromNode, toNode, readEdge.getGuard(), false,readEdge.getOptionalEdge()),
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

  /**
   *
   * @param net1
   * @param net2
   * @return the multiRoot for parallel composition of the nets
   */
  private static List<Set<String>> buildRoots(Petrinet net1,Petrinet net2) {
    //System.out.println("Building Roots");
    List<Set<String>> out = new ArrayList<>();
    for(Set<String> m1: net1.getRoots()) {
      for(Set<String> m2: net2.getRoots()) {
        out.add(buildMark(m1,m2));
      }
    }
    //System.out.println("New Roots "+out);
    return out;
  }

  private static Set<String> buildMark(Set<String> m1, Set<String> m2){
    //System.out.println("buildMark " +m1+" "+m2);
    Set<String> out = new HashSet<>();
    for(String s1:m1){
      for(String s2:m2){
        out.add(s1+MAPLET+s2);
      }
    }
    //System.out.println("Next root "+out);
    return out;
  }
}
