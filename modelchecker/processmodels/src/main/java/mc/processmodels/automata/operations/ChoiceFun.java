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
   * @param net1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param net2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting petrinet of the operation
   */

  public Petrinet compose(String id, Petrinet net1, Petrinet net2)
    throws CompilationException {
    System.out.println("[]PETRI1 "+net1.myString());
    assert net1.validatePNet():"choice precondition net1";
    System.out.println("[]PETRI2 "+net2.myString());
    assert net2.validatePNet():"choice precondition net2";
    Stack<Set<String> > endStack = new Stack<>(); // for the end markings of each Ri[]Rj
    List<Set<String>> newRoots = new ArrayList<>();
    //System.out.println("[]CHOICE " +net1.getRoots().size() +" X " +net2.getRoots().size());
    Petrinet composition = new Petrinet(id, false);
    composition.getOwners().clear();
    //for each pair of roots Ri, Rj build a Ri[]Rj processes
    for (int i = 0; i < net1.getRoots().size(); i++) {
      for (int j = 0; j < net2.getRoots().size(); j++) {
        Petrinet petrinet1 = new Petrinet(id, false);
        Petrinet petrinet2 = new Petrinet(id, false);
        //clone nets
        petrinet1.addPetrinet(net1, true); //Roots to be rebuilt
        petrinet2.addPetrinet(net2,true);  // needed tempotarly
    //System.out.println("petrinet1 " +petrinet1.myString());
        Set<String> startOfP1 = petrinet1.getRoots().get(i);
        Set<String> startOfP2 = petrinet2.getRoots().get(j);
   //System.out.println( "\nstartOfP1 "+ startOfP1 +"  startOfP2 "+ startOfP2+"\n");
       /* if (!petrinet1.terminates()) {
          petrinet1.getPlaces().values().stream().filter(PetriNetPlace::isTerminal).
            forEach(x -> x.setTerminal(""));
        }
        if (!petrinet2.terminates()) {
          petrinet2.getPlaces().values().stream().filter(PetriNetPlace::isTerminal).
            forEach(x -> x.setTerminal(""));
        } */
        Set<String> own1 = petrinet1.getOwners();
        Set<String> own2 = petrinet2.getOwners();
        //System.out.println("\n own1 "+own1+ " own2 "+own2);
        //prior to Gluing join the nets (do not add as that changes the names
        //System.out.println("\n P1.owners "+petrinet1.getOwners());
        composition.joinPetrinet(petrinet1);
        //System.out.println("comp.owners "+composition.getOwners());
        //add the second petrinet;
        composition.joinPetrinet(petrinet2);
        //System.out.println("comp.owners "+composition.getOwners());
        composition.setRoots(new ArrayList<>());
        //System.out.println("\n[]after Join " + composition.myString() + "\n");
        //merge the end of petri1 with the start of petri2

          composition.glueOwners(own1,own2);
        Map<String, String> s2s =composition.glueNames(startOfP1, startOfP2);

        //System.out.println("\n glueMapping \n" +
        //  s2s.keySet().stream().map(x -> x + "->" + s2s.get(x) + "\n").collect(Collectors.joining()) + "\n");


        newRoots.add((buildMark(startOfP1,startOfP2).stream().
                   map(x->s2s.get(x)).collect(Collectors.toSet())));

        //System.out.println("newRoots " + newRoots);


        // composition.setStartFromRoot();
        //composition = PetrinetReachability.removeUnreachableStates(composition);
        //System.out.println("\n[]before Glue END " +net1.terminates()+" " +net2.terminates() + " "+composition.myString() + "\n");
        if (net1.terminates() && net2.terminates()) {//Sugar  end1 or end2 will be set to size 0
          Set<PetriNetPlace> end1 = petrinet1.getPlaces().values().stream()
            .filter(x -> x.isTerminal()).collect(Collectors.toSet());
          Set<PetriNetPlace> end2 = petrinet2.getPlaces().values().stream()
            .filter(x -> x.isTerminal()).collect(Collectors.toSet());
  System.out.println("\nend1 "+end1.stream().map(x->x.getId()+" ").collect(Collectors.joining()) +
                     " end2 "+end2.stream().map(x->x.getId()+" ").collect(Collectors.joining())+"\n");
          if (end1.size() > 0 && end2.size() > 0) {
            //glue the Ri, Ri end markings
            Map<String,String>  endn = composition.gluePlaces(end1, end2);
            Set<String>  endSet = endn.values().stream().distinct().collect(Collectors.toSet());
            /*System.out.println(endn.values().stream().
               map(x->x+"->"+endn.get(x)+" ").collect(Collectors.joining()));
            Set<String> endSet = endn.values().stream().collect(Collectors.toSet()); */
            endStack.push(endSet);
            System.out.println("PUSHED endSet "+endSet);
          }
          //System.out.println("\n[]after Glue End  " + composition.myString() + "\n");
        }
      }
    }

    // glue each Ri[]Rj end together
    //System.out.println("endStack "+ endStack.toString());
    if (endStack.size() > 1) {
      Set<String> base = endStack.pop();
      //System.out.println("base "+base);
      while (endStack.size() > 0) {
        Set<String> next = endStack.pop();
        //System.out.println("next "+next);
         composition.glueNames(base,next);
      }
    }
    //System.out.println("\n[]FINAL newRoots " + newRoots);
    //System.out.println("before setting root "+ composition.myString()+"\n");
    composition.setRoots(newRoots);
    composition.setStartFromRoot();

    //System.out.println("[]Add OUT "+ composition.myString()+"\n");
    composition = PetrinetReachability.removeUnreachableStates(composition);
    System.out.println("\n[] OUT "+ composition.myString()+"\n");
    composition.reown();
    assert composition.validatePNet(): "choice post condition";
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
