package mc.processmodels.automata.operations;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.rits.cloning.Cloner;
import java.util.*;
import java.util.stream.Collectors;

import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;
import mc.processmodels.petrinet.components.PetriNetPlace;
import mc.processmodels.petrinet.components.PetriNetTransition;
import mc.processmodels.petrinet.operations.PetrinetReachability;

public class SequentialInfixFun {


    /**
     * Execute the function.
     *
     * @param id         the id of the resulting automaton
     * @param a1 the first  automaton in the function (e.g. {@code A} in {@code A||B})
     * @param a2 the second automaton in the function (e.g. {@code B} in {@code A||B})
     * @return the resulting automaton of the operation
     */
    public Automaton compose(String id, Automaton a1, Automaton a2) throws CompilationException {
        //System.out.println("\n *********************");
        //System.out.println("a1 "+automaton1.myString());
        //System.out.println("a2 "+automaton2.myString());
        Automaton sequence = new Automaton(id, !Automaton.CONSTRUCT_ROOT);
        Cloner cloner=new Cloner();
        Automaton automaton1 = cloner.deepClone(a1);
        Automaton automaton2 = cloner.deepClone(a2);
        Multimap<String, String> setOfOwners = Automaton.ownerProduct(automaton1, automaton2);

        //System.out.println("setOfOwners "+setOfOwners.toString());
        //store a map to the nodes so id can be ignored
        Map<String, AutomatonNode> automata1nodes = new HashMap<>();
        Map<String, AutomatonNode> automata2nodes = new HashMap<>();

        // //System.out.println("Sequence aut1 "+ automaton1.toString());
        // //System.out.println("Sequence aut2 "+ automaton2.toString());
        //copy node1 nodes across
        AutomataReachability.removeUnreachableNodes(automaton1).getNodes().forEach(node -> {

            try {
                //System.out.println("1 adding "+node.myString());
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

        //System.out.println("Sequence 1 "+sequence.myString());

        copyAutomataEdges(sequence, automaton1, automata1nodes, setOfOwners);


        //get the stop nodes such that they can be replaced
        Collection<AutomatonNode> stopNodes = sequence.getNodes().stream()
          .filter(n -> "STOP".equals(n.getTerminal()))
          .collect(Collectors.toList());
   /*System.out.print("stopNodes "+stopNodes.stream().
      map(x->x.getId()).reduce("{",(x,y)->x=x+" "+y)+"}"); */
        //if there are no stop nodes, we cannot glue them together
        if (stopNodes.isEmpty()) {
            //System.out.println("EMPTY STOP!");
            return sequence;
        }


//below copies the automaton hence renames the nodes
        AutomataReachability.removeUnreachableNodes(automaton2).getNodes().forEach(node -> {
            //System.out.println("2 adding "+node.myString());
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
                                false,edge.getOptionalEdge()), edge.getOwnerLocation());
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
        //System.out.println("automaton2  "+ automaton2.myString());

        copyAutomataEdges(sequence, automaton2, automata2nodes, setOfOwners);

        //System.out.println("End Seq   "+sequence.myString());
        return sequence;
    }

    /**
     * Assumes Single End marking
     * A=>B   where B has roots Br1,Br2,.. Must build copies  A1,A2,..
     * The net A=>B is the union of B,A1,A2,...  and has roots A1r,A2r, ..
     * (the roots can not be glued together because of the case when Ar overlaps with Ae)
     *
     * @param id   the id of the resulting petrinet
     * @param n1 the first  petrinet in the function (e.g. {@code A} in {@code A||B})
     * @param n2 the second petrinet in the function (e.g. {@code B} in {@code A||B})
     * @return the resulting petrinet of the operation
     */

    public Petrinet compose(String id, Petrinet n1, Petrinet n2)
      throws CompilationException {
        //System.out.println("=>PETRI1 "+ id+" "+n1.getId()+"=>"+n2.getId());
        n1.validatePNet();
        n2.validatePNet();

        Petrinet net1 = n1.reId("1");
        Petrinet net2=  n2.reId("2"); // the tag "2" ensures unique ids in nets
        //System.out.println("=>PETRI1 "+ id+" "+net1.myString());
        //System.out.println("=>PETRI2 "+ id+" "+net2.myString());

        Set<String> own1 = new HashSet<>();
        Set<String> own2 = new HashSet<>();

        own1.addAll(net1.getOwners());
        own2.addAll(net2.getOwners());
        System.out.println("In1=> " + net1.myString());
        System.out.println("=>In2 " + net2.myString());
        if (net1.getId().equals(net2.getId())) {
            System.out.println("\n SAME NETS PROBLEM\n");
        }
        Petrinet composition = new Petrinet(id, false);
        //List<Set<String>> p2roots = net2.getRoots();
        //int i = net2.getRoots().size();
        //System.out.println("ROOTS " + i);
        for (PetriNetPlace pl : net2.getPlaces().values()) {
            pl.setStart(false);
        }
        for (PetriNetPlace pl : net1.getPlaces().values()) {
            pl.setEndNos(new HashSet<>());
            pl.setTerminal("");
        }
        List<Set<String>> oneEnd = net1.copyEnds();
        int i =1;
        //net2.clearRoots();
        composition.addPetrinetNoOwner(net2, "");
        composition.addPetrinetNoOwner(net1, "");
        Petrinet sequential;
        System.out.println("\n ***Seq "+ net1.getEnds() +"  "+net2.getRoots());
        System.out.println("comp "+composition.myString());
        for(Set<String> ed: oneEnd) {
            for (Set<String> rt : net2.getRoots()) {
                System.out.println("***SEQ   START " + i++ + " end "+ed+ " root = " + rt);
                composition.setOwners(new HashSet<>());

                net1.getPlaces().values().stream().forEach(x -> x.setTerminal(""));
                composition.setRootFromStart();
                Set<PetriNetPlace> newroot = new HashSet<>();
                for (String r:rt) {
                    newroot.add(composition.copyRootOrEnd(composition.getPlaces().get(r), ""));
                }
                Set<PetriNetPlace> newend = new HashSet<>();
                for (String e:ed) {
                    newend.add(composition.copyRootOrEnd(composition.getPlaces().get(e), ""));
                }



                composition.glueOwners(own1, own2);

                composition.gluePlaces(newroot,newend);

                System.out.println("\n ***SEQ Glue places OVER \n" + composition.myString("edge") + "\n");


            }
        }
        //remove old ends
        for(Set<String> eds: oneEnd) {
            System.out.println("ed " + eds);
            for (String ed : eds) {
                PetriNetPlace pl = composition.getPlaces().get(ed);
                System.out.println(pl.myString());
                Set<PetriNetTransition> trs = pl.pre();
                System.out.println("trs " + trs.size());
                for (PetriNetTransition tr : trs) {
                    System.out.println("tr " + tr.getId());
                    composition.removeTransition(tr);
                }
            }
        }
        composition.setRootFromStart();
        composition = PetrinetReachability.removeUnreachableStates(composition);

        System.out.println("=> part "+composition.myString());
        sequential = PetrinetReachability.removeUnreachableStates(composition);
        sequential.setRootFromStart();
        sequential.setEndFromPlace();
        //Petrinet seq = new Petrinet(id, false);
        //seq.addPetrinet(sequential);  // renumbers the ids
        //System.out.println("FINAL " +sequential.myString());
        sequential.validatePNet();
        System.out.println("=> END "+sequential.myString());
        return sequential;


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
              writeAutomaton.addEdge(readEdge.getLabel(), fromNode, toNode, readEdge.getGuard(), false,readEdge.getOptionalEdge()),
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