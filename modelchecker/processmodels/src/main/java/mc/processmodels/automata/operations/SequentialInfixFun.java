package mc.processmodels.automata.operations;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.*;
import java.util.stream.Collectors;

import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.operations.PetrinetReachability;

public class SequentialInfixFun {


  /**
   * Execute the function.
   *
   * @param id         the id of the resulting automaton
   * @param automaton1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
   * @param automaton2 the second automaton in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting automaton of the operation
   */
  public Automaton compose(String id, Automaton automaton1, Automaton automaton2) throws CompilationException {
    System.out.println("\n *********************\n");
    System.out.println("a1 "+automaton1.myString());
    System.out.println("a2 "+automaton2.myString());
    Automaton sequence = new Automaton(id, !Automaton.CONSTRUCT_ROOT);

    Multimap<String, String> setOfOwners = Automaton.ownerProduct(automaton1, automaton2);

    //System.out.println("setOfOwners "+setOfOwners.toString());
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
          if (stopNode.getIncomingEdges().size() == 0) {// If automaton 1 is only a stop node
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

    copyAutomataEdges(sequence, automaton2, automata2nodes, setOfOwners);

    //  System.out.println("End Seq   "+sequence.toString());
    return sequence;
  }

  /**
   * A=>B   where B has roots Br1,Br2,.. Must build copies  A1,A2,..
   * The net A=>B is the union of B,A1,A2,...  and has roots A1r,A2r, ..
   * (the roots can not be glued together because of the case when Ar overlaps with Ae)
   *
   * @param id   the id of the resulting petrinet
   * @param net1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
   * @param net2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
   * @return the resulting petrinet of the operation
   */

  public Petrinet compose(String id, Petrinet net1, Petrinet net2)
    throws CompilationException {
    String name = net1.getId() + "=>" + net2.getId();
   //System.out.println("=>PETRI1 "+net1.myString());
    net1.validatePNet();
   //System.out.println("=>PETRI2 "+net2.myString());
    net2.validatePNet();
    Stack<Petrinet> netStack = new Stack<>();
    ///LOOP
    List<Set<String>> p2roots = net2.getRoots();
    int i = p2roots.size();
   //System.out.println("ROOTS " + i);
    Petrinet sequential;
    if (net1.terminates()) {
      for (Set<String> rt : net2.getRoots()) {
        //System.out.println("\n "+i+" rt = "+rt);
        Petrinet petrinet1 = net1.copy();
        Petrinet petrinet2 = net2.copy();
        //System.out.println("In1=> " + petrinet1.myString());
        //System.out.println("=>In2 " + petrinet2.myString());
        if (petrinet1 == petrinet2) {
          System.out.println("\n SAME NETS PROBLEM\n");
        }
        for (PetriNetPlace pl1 : petrinet1.getPlaces().values()) {
          for (PetriNetPlace pl2 : petrinet2.getPlaces().values()) {
            if (pl1 == pl2) System.out.println("\n SAME PLACES PROBLEM\n");
          }
        }
        Petrinet composition = new Petrinet(id, false);
        composition.getOwners().clear();
        composition.getOwners().addAll(petrinet1.getOwners());


        Set<String> stopNodes = new HashSet<>();

        for (PetriNetPlace pl : petrinet2.getPlaces().values()) {
          pl.setStart(false);
        }
        petrinet2.clearRoots();
        //System.out.println("SHOULD HAVE NO START "+petrinet2.myString());

        composition.getOwners().addAll(petrinet2.getOwners());

        //System.out.println("Root names " + p2roots);
        String tag = "*S" + i;
        //add the first petrinet to the sequential composition
        composition.addPetrinetNoOwner(petrinet1, tag);
        Set<String> nextEnd = composition.getPlaces().values().stream()
          .filter(x -> x.isSTOP()).map(x -> x.getId()).collect(Collectors.toSet());
        //System.out.println("nextEnd from "+composition.myString());
        composition.getPlaces().values().stream().forEach(x -> x.setTerminal(""));
        composition.setRootFromStart();


        //System.out.println("Loop rt = " + rt);
        //System.out.println("ROOTS " + tag);
        i--;
        Set<String> taggedrt = rt.stream().map(x -> x + tag).collect(Collectors.toSet());

        composition.addPetrinetNoOwner(petrinet2, tag); //replace objects tags names IGNORE root
        //System.out.println("composition " + i + " = " + composition.myString());
        //System.out.println(i + " ROOT = " + taggedrt + "  END = " + nextEnd);

        composition.glueNames(nextEnd, taggedrt);
        //System.out.println("\nGlue OVER \n"+ composition.myString()+"\n");
        composition.setRootFromStart();
        //System.out.println("\nTO STACK \n"+ composition.myString()+"\n");
        netStack.push(composition.copy());
      }
      //now we have a stack of i nets   Ai=>Bi
      //  we need to compose these with internal choice
      InternalChoiceInfixFun internalChoice = new InternalChoiceInfixFun();
      sequential = netStack.pop();
      int pi = 1;
      while (!netStack.empty()) {


        sequential = internalChoice.compose("=>" + pi++, sequential, netStack.pop());
        //System.out.println("SEQing " + sequential.myString());
        //System.out.println("SEQing OVER");
      }
      sequential = PetrinetReachability.removeUnreachableStates(sequential);
      sequential.setRootFromStart();
      //Petrinet seq = new Petrinet(id, false);
      //seq.addPetrinet(sequential);  // renumbers the ids
      //System.out.println("FINAL " +sequential.myString());
      sequential.validatePNet();
      return sequential;
    } else {
    // net1 dose not terminate so returne only net1
      return net1.copy();
    }

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
                                 Multimap<String, String> edgeOwnersMap) throws CompilationException {


    for (AutomatonEdge readEdge : readAutomaton.getEdges()) {
      AutomatonNode fromNode = nodeMap.get(readEdge.getFrom().getId());
      AutomatonNode toNode = nodeMap.get(readEdge.getTo().getId());
      writeAutomaton.addOwnersToEdge(
        writeAutomaton.addEdge(readEdge.getLabel(), fromNode, toNode, readEdge.getGuard(), false),
        getEdgeOwnersFromProduct(readEdge.getOwnerLocation(), edgeOwnersMap)
      );
    }
  }

  private Set<String> getEdgeOwnersFromProduct(Set<String> edgeOwners,
                                               Multimap<String, String> productSpace) {
    return edgeOwners.stream().map(productSpace::get)
      .flatMap(Collection::stream)
      .collect(Collectors.toSet());
  }

}

/*
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
 */