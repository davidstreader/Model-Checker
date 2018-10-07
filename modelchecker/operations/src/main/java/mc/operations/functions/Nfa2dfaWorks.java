package mc.operations.functions;

import com.microsoft.z3.Context;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class Nfa2dfaWorks {
  /**
   * Build a dfa from a nfa
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the z3 context
   * @param tt
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   * @throws CompilationException when the function fails
   */

  public Automaton compose(String id, Set<String> flags, Context context, TraceType tt, Automaton... automata)
    throws CompilationException {
    //BuildAcceptanceGraphs ba = new BuildAcceptanceGraphs();
    //ba.compose(id,flags,context,automata);
    //System.out.println("Pingo2");
    assert automata.length == 1;
    //System.out.println("nfa2dfa in "+ automata[0].myString());
    Automaton nfa = automata[0].copy();
    for (AutomatonNode ndn :nfa.getNodes()){ // NOT for Galois expaned automata
      if (ndn.isSTOP()) {
        nfa.addEdge(Constant.STOP, ndn, nfa.deadNode(), null, true,false);
        System.out.println("adding STOP to nfa "+ndn.myString());
      }
      if (ndn.isStartNode()) {
        nfa.addEdge(Constant.Start, ndn, nfa.deadNode(), null, true,false);
      }
      if (ndn.isERROR()) {
        nfa.addEdge(Constant.ERROR, ndn, nfa.deadNode(), null, true,false);
      }
      if (ndn.isQuiescent()) {
        nfa.addEdge(Constant.Quiescent, ndn, nfa.deadNode(), null, true,false);
      }
    }
    Automaton dfa = new Automaton(id, !Automaton.CONSTRUCT_ROOT);

    //  maps internal representation to actual set of nodes
    Map<Set<String>, List<AutomatonNode>> stateMap = new HashMap<>();
    Map<String, AutomatonNode> nodeMap = new HashMap<>();
    Set<String> visited = new HashSet<>();

    Stack<Set<String>> toDoList = new Stack<>();

//build set of nfa nodes used to constuct a the single dfa root node
    toDoList.push(constructClosure(nfa.getRoot(), stateMap));

    Set<String> alphabet = nfa.getAlphabet();
    //alphabet.remove(Constant.HIDDEN);

    boolean processedRoot = false;
    while (!toDoList.isEmpty()) {  // starts with the set of root nodes
      //System.out.println("toDo = "+toDoList);
      Set<String> states = toDoList.pop();
      String idNode = constructNodeId(stateMap.get(states), nfa.getId());
      //System.out.println("idNode = "+idNode+"  states = "+states);
      if (visited.contains(idNode)) { continue;  }  // process dfa node only once
      if (!nodeMap.containsKey(idNode)) { //set up nodeMap and add node to dfa
        AutomatonNode n = dfa.addNode(idNode);
        nodeMap.put(idNode, n);
      }
      AutomatonNode node = nodeMap.get(idNode);  //build new dfa node

      if (!processedRoot) { // if first time through then this  node is the root
        dfa.getRoot().clear();
        dfa.addRoot(node);
        node.setStartNode(true);
        processedRoot = true;
        //if one root nfa STOPS so is the dfa root
        if (nfa.getRoot().stream().anyMatch(e -> e.isSTOP()) ) {
          dfa.getNode(idNode).setStopNode(true);
          System.out.println("adding STOP to dfa "+dfa.getNode(idNode).myString());
        }
        //if one root nfa is ERROR so is the dfa root
        if (nfa.getRoot().stream().anyMatch(e ->(e.isERROR() || e.getOutgoingEdges().size()==0)) ) {
          dfa.getNode(idNode).setErrorNode(true);
        }
        //System.out.println("Root "+node.myString());
      }


      for (String action : alphabet) {
        Set<String> nextStates = constructStateSet(stateMap.get(states), action, stateMap);
        if (nextStates.isEmpty()) {  continue;  }
        String nextId = constructNodeId(stateMap.get(nextStates), nfa.getId());
    // stateMap.get(nextStates).stream().forEach(x->System.out.println("next "+x.myString()));
        if (!nodeMap.containsKey(nextId)) {
          AutomatonNode nd = dfa.addNode(nextId);
          nodeMap.put(nextId, nd);
        }
        AutomatonNode nextNode = nodeMap.get(nextId);
        /*for (AutomatonNode ndn :nfa.getNodes()){
          if (ndn.isSTOP()) {
            dfa.getNode(nextId).setStopNode(true);
            System.out.println("adding STOP 2 dfa "+dfa.getNode(idNode).myString());
          }
          if (ndn.isStartNode()) {
            dfa.getNode(nextId).setStartNode(true);
          }
          if (ndn.isERROR()) {
            dfa.getNode(nextId).setErrorNode(true);
          }
          if (ndn.isQuiescent()) {
            dfa.getNode(nextId).setQuiescent(true);
          }
        }*/
        dfa.addEdge(action, node, nextNode, null, true,false);

        toDoList.push(nextStates);
      }

      visited.add(idNode);
    }

  /*  dfa.getNodes().stream()
        .filter(node -> node.getOutgoingEdges().isEmpty())
        .forEach(node -> node.setTerminal(Constant.STOP)); */
    //System.out.println("\n built dfa " +dfa.myString()+"\n");
    return dfa;
  }

  /**
   *
   * @param node  roots
   * @param stateMap  input output  map from STATE to node
   * @return  the state OR internal reprentation of set of nfa nodes
   */
  private Set<String> constructClosure(Collection<AutomatonNode> node,
                                       Map<Set<String>,List<AutomatonNode>> stateMap) {
    Set<String> states = new HashSet<>();
    List<AutomatonNode> nodes = new ArrayList<>();

    Stack<AutomatonNode> fringe = new Stack<>();
    node.forEach(fringe::push);

    while (!fringe.isEmpty()) {
      AutomatonNode current = fringe.pop();
      if (states.contains(current.getId())) { continue; }
      states.add(current.getId());
      nodes.add(current);

      List<AutomatonEdge> edges = current.getOutgoingEdges().stream()
        .filter(AutomatonEdge::isHidden)
        .collect(Collectors.toList());

      edges.forEach(edge -> fringe.push(edge.getTo()));
      //System.out.println(fringe.size());
    }

    if (!stateMap.containsKey(states)) {
      stateMap.put(states, nodes);
    }

    return states;
  }

  /**
   *
   * @param nodes  set of nodes that become new node
   * @param action action of events leaving set of nodes and new node
   * @param stateMap  input output
   * @return  set of node ids that represent the  new node
   */
  private Set<String> constructStateSet(List<AutomatonNode> nodes, String action, Map<Set<String>,
    List<AutomatonNode>> stateMap) {
    Set<String> states = new HashSet<>();
    List<AutomatonNode> nextNodes = new ArrayList<>();
    Set<String> visited = new HashSet<>();

    Stack<AutomatonNode> fringe = new Stack<>();
    nodes.forEach(fringe::push);

    while (!fringe.isEmpty()) {
      AutomatonNode current = fringe.pop();

      if (visited.contains(current.getId())) {
        continue;
      }

      List<AutomatonEdge> edges = current.getOutgoingEdges();
      for (AutomatonEdge edge : edges) {
        if (action.equals(edge.getLabel())) {
          states.add(edge.getTo().getId());
          nextNodes.add(edge.getTo());
        }
      }

      visited.add(current.getId());
    }

    if (!stateMap.containsKey(states)) {
      stateMap.put(states, nextNodes);
    }

    return states;
  }

  /**
   *
   * @param nodes  input
   * @param identifier  input
   * @return
   */
  private String constructNodeId(List<AutomatonNode> nodes, String identifier) {
    return identifier + constructLabel(nodes);
  }

  /**
   *
   * @param nodes set of nodes n1,n2
   * @return  a string of the set of nodes "{n1,n1}"
   */
  private String constructLabel(List<AutomatonNode> nodes) {
    Set<String> labelSet = new HashSet<>();
    for (AutomatonNode node : nodes) {
      labelSet.add(Integer.toString(node.getLabelNumber()));
    }

    List<String> labels = new ArrayList<>(labelSet);
    Collections.sort(labels);

    StringBuilder builder = new StringBuilder();
    builder.append("{");
    for (int i = 0; i < labels.size(); i++) {
      builder.append(labels.get(i));
      if (i < labels.size() - 1) {
        builder.append(",");
      }
    }
    builder.append("}");

    return builder.toString();
  }

}
