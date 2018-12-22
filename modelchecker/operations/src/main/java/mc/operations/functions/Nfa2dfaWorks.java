package mc.operations.functions;

import com.microsoft.z3.Context;
import mc.Constant;
import mc.TraceType;
import mc.exceptions.CompilationException;
import mc.operations.SubSetDataConstructor;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class Nfa2dfaWorks {
  /**
   * Build a dfa from a nfa  Add Quiescent loop edge to the dfa where needed
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the z3 context
   * @param tt
   * @param automata a variable number of automata taken in by the function
   *                 ADD LAMBDA  Set<node> -> List<Set<String>>
   * @return the resulting automaton of the operation
   * @throws CompilationException when the function fails
   */


  public Automaton compose(String id, Set<String> flags, Context context,
                           TraceType tt, SubSetDataConstructor dataConstructor, Automaton... automata)
    throws CompilationException {
    boolean cong = flags.contains(Constant.CONGURENT);

    //System.out.println("Nfa2dfa " + id + " " + flags + " " + tt);
    assert automata.length == 1;
    int dfaNodeLabel = 0;
    Automaton nfa = automata[0].copy();// copies nodeId
    nfa = nfa.reId("p");
    //System.out.println("nfa2dfa START "+nfa.myString());
    for (AutomatonNode ndn : nfa.getNodes()) {
      // NOT for Galois expaned automata  OK SMART ASS why?

      if (cong) {
        AutomatonNode deadNd = nfa.deadNode();
        if (tt.equals(TraceType.QuiescentTrace)) {
          if (ndn.isSTOP()) {
            AutomatonNode zomNd1 = nfa.getDeadNode("_zom1");
            nfa.addEdge(Constant.STOP, ndn, zomNd1, null, true, false);
         //   nfa.addEdge(Constant.STOP + "!", zomNd1, deadNd, null, true, false);
          }
          if (ndn.isStartNode()) {
            AutomatonNode zomNd2 = nfa.getDeadNode("_zom2");
            nfa.addEdge(Constant.Start, ndn, zomNd2, null, true, false);
        //    nfa.addEdge(Constant.Start + "!", zomNd2, deadNd, null, true, false);
            //System.out.println("pingo " + nfa.myString());
          }
        } else {  //NOT Quiescent
          if (ndn.isSTOP()) {
            nfa.addEdge(Constant.STOP, ndn, deadNd, null, true, false);
          }
          if (ndn.isSTOP()) {
            nfa.addEdge(Constant.STOP, ndn, deadNd, null, true, false);
          }
        }

        //if (tt.equals(TraceType.CompleteTrace) && ndn.isERROR()) {
        //  nfa.addEdge(Constant.ERROR, ndn, nfa.deadNode(), null, true, false);
          //  //  nfa.addEdge(Constant.END, ndn, nfa.deadNode(), null, true, false);
        //}

      } else //NOT cong
        if (tt.equals(TraceType.CompleteTrace) && (ndn.isSTOP() || ndn.isERROR())) {
          nfa.addEdge(Constant.END, ndn, nfa.deadNode(), null, true, false);
          //System.out.println("adding edge to nfa "+ed.myString());
        }  //END is dummy edge to enforce complete traces

      if (tt.equals(TraceType.QuiescentTrace) && ndn.isQuiescent()) {
        nfa.addEdge(Constant.Quiescent, ndn, nfa.deadNode(), null, true, false);
        //System.out.println("adding edge to nfa "+ed.myString());
      }
// add empty trace
      if (!tt.equals(TraceType.Trace) &&
        ndn.isStartNode() &&
        (ndn.isSTOP() || ndn.isERROR())) {
        nfa.addEdge(Constant.EPSILON, ndn, nfa.deadNode(), null, true, false);
      }
    }
     //System.out.println("ANOTATED nfa  " + nfa.myString());

    Automaton dfa = new Automaton(id + "_dfa", !Automaton.CONSTRUCT_ROOT);

    //  maps internal representation to actual set of nodes
    Map<Set<String>, List<AutomatonNode>> stateMap = new HashMap<>();
    Map<String, AutomatonNode> nodeMap = new HashMap<>();
    Set<String> visited = new HashSet<>();

    Stack<Set<String>> toDoList = new Stack<>(); //Stack of sets of reachable node ids

//build set of nfa nodes used to constuct a the single dfa root node
    toDoList.push(constructClosure(nfa.getRoot(), stateMap));
     //System.out.println("nfa Root "+nfa.getRoot().stream().map(x->x.getId()).collect(Collectors.joining()));
     //System.out.println("dfa Root "+ toDoList.peek());
    Set<String> alphabet = nfa.getAlphabet();
    //alphabet.remove(Constant.HIDDEN);
    //System.out.println("dfa alphabet "+alphabet);
    boolean processedRoot = false;
    while (!toDoList.isEmpty()) {  // starts with the set of nfa root node ids
      //System.out.println("toDo = "+toDoList);
      Set<String> states = toDoList.pop();
      //System.out.println("stateMap.get("+states+")"+" , "+nfa.getId());
      String idNode = constructNodeId(stateMap.get(states), nfa.getId());
      //System.out.println(" output from constructNodeId, idNode = "+idNode+"  states = "+states);
      if (visited.contains(idNode)) continue;
      //System.out.println("Processing "+idNode);
      // process dfa node only once
      if (!nodeMap.containsKey(idNode)) { //set up nodeMap and add node to dfa
        AutomatonNode n = dfa.addNode(idNode);
        n.setLabelNumber(dfaNodeLabel++);
        nodeMap.put(idNode, n);
      }
      AutomatonNode node = nodeMap.get(idNode);  //build new dfa node

      if (!processedRoot) { // if first time through then this  node is the root
        dfa.getRoot().clear();
        dfa.addRoot(node);
        node.setStartNode(true);
        processedRoot = true;
        //if one root nfa STOPS so is the dfa root
        if (nfa.getRoot().stream().anyMatch(e -> e.isSTOP())) {
          dfa.getNode(idNode).setStopNode(true);
          //System.out.println("adding STOP to dfa "+dfa.getNode(idNode).myString());
        }
        //if one root nfa is ERROR so is the dfa root
        if (nfa.getRoot().stream().anyMatch(e -> (e.isERROR() || e.getOutgoingEdges().size() == 0))) {
          dfa.getNode(idNode).setErrorNode(true);
        }
        //System.out.println("Root "+node.myString());
        dfa.getNode2ReadySets().put(node,
          //dataConstructor is semantic dependent must be applied to every node
          dataConstructor.op(stateMap.get(states).stream().collect(Collectors.toSet()),
            flags.contains(Constant.CONGURENT)));
      }


      for (String action : alphabet) {
        //System.out.println("action "+action);
        Set<String> nextStates = constructStateSet(stateMap.get(states), action, stateMap);
        if (nextStates.isEmpty()) {
          //System.out.println("Empty -"+ action+"->  not added");
          continue;
        }
        String nextId = constructNodeId(stateMap.get(nextStates), nfa.getId());
        //stateMap.get(nextStates).stream().forEach(x->System.out.println("next "+x.myString()));
        if (!nodeMap.containsKey(nextId)) {
          //System.out.println(" nextId "+nextId);
          AutomatonNode nd = dfa.addNode(nextId);
          nd.setLabelNumber(dfaNodeLabel++);
          nodeMap.put(nextId, nd);
          //Apply function to add data (Fail -> set of acceptance sets,...)
          //System.out.println();
          dfa.getNode2ReadySets().put(nd,
            //dataConstructor is semantic dependent must be applied to every node
            dataConstructor.op(stateMap.get(nextStates).stream().collect(Collectors.toSet()),
              flags.contains(Constant.CONGURENT)));
          //System.out.println("nfa2dfaWorks "+nd.getId()+" -> "+dfa.getNode2ReadySets().get(nd));
        }
        AutomatonNode nextNode = nodeMap.get(nextId);

        dfa.addEdge(action, node, nextNode, null, true, false);

        if (!toDoList.contains(nextStates)) toDoList.push(nextStates);
      }

      visited.add(idNode);
    }

  /*  dfa.getNodes().stream()
        .filter(node -> node.getOutgoingEdges().isEmpty())
        .forEach(node -> node.setTerminal(Constant.STOP)); */
    //System.out.println("\n built dfa " + dfa.myString() + "\n");
    return dfa;
  }

  /**
   * @param node     roots
   * @param stateMap input output  map from STATE to node
   * @return set if node ids, the internal reprentation of set of nfa nodes
   */
  private Set<String> constructClosure(Collection<AutomatonNode> node,
                                       Map<Set<String>, List<AutomatonNode>> stateMap) {
    Set<String> states = new HashSet<>();
    List<AutomatonNode> nodes = new ArrayList<>();
    Stack<AutomatonNode> fringe = new Stack<>();
    node.forEach(fringe::push);  // the fringe has all the root nodes

    while (!fringe.isEmpty()) {
      AutomatonNode current = fringe.pop();
      //System.out.println("Root  "+current.getId());
      if (states.contains(current.getId())) continue;
      states.add(current.getId());
      nodes.add(current);
    }

    if (!stateMap.containsKey(states)) {
      stateMap.put(states, nodes);
      //System.out.println("ADDING 2 StateMap "+ states + "->"+nodes.stream().map(AutomatonNode::getId).collect(Collectors.joining()));
    }
    //System.out.println("construct Root "+states);
    return states;
  }

  /**
   * @param nodes    input ONL set of initial nodes
   * @param action   action of events leaving a node in nodes
   * @param stateMap input output
   * @return set of node ids that represent the  new node  after  nodes-action->
   */
  private Set<String> constructStateSet(List<AutomatonNode> nodes,
                                        String action,
                                        Map<Set<String>, List<AutomatonNode>> stateMap) {
    //System.out.println("construct "+ nodes.stream().map(AutomatonNode::getId).collect(Collectors.joining())+ "->"+action+"  to?");

    Set<String> states = new HashSet<>();
    List<AutomatonNode> nextNodes = new ArrayList<>();
    Set<String> visited = new HashSet<>();

    Stack<AutomatonNode> fringe = new Stack<>();
    nodes.forEach(fringe::push); // put the nodes onto the stact
    //System.out.println("built fringe "+ fringe.stream().map(AutomatonNode::getId).collect(Collectors.joining()));

    while (!fringe.isEmpty()) {
      AutomatonNode current = fringe.pop();
      //System.out.println("  current "+current.getId());
      if (visited.contains(current.getId())) continue;

      for (AutomatonEdge edge : current.getOutgoingEdges()) {
        //System.out.println("    edge "+edge.myString("simple"));
        if (action.equals(edge.getLabel())) {
          states.add(edge.getTo().getId());
          nextNodes.add(edge.getTo());
        }
      }
      visited.add(current.getId());
    }
// nextNodes.size()>0 ADDED if == 0 then this event is not
    if (nextNodes.size() > 0 && !stateMap.containsKey(states)) {
      stateMap.put(states, nextNodes);
      //System.out.println("ADDING 2 StateMap "+ states + "->"+nextNodes.stream().map(AutomatonNode::getId).collect(Collectors.joining()));
    }
    //if (nextNodes.size()>0)
    //System.out.println("constructStateSet of new node after "+action+" ->"+states);
    return states;
  }

  /**
   * Build the nodeId from set of nodes
   *
   * @param nodes      input
   * @param identifier input
   * @return
   */
  private String constructNodeId(List<AutomatonNode> nodes, String identifier) {
    String out = identifier + constructLabel(nodes);
    return out;
  }

  /**
   * @param nodes set of nodes n1,n2
   * @return a string of the set of nodes "{n1,n1}"
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
