package mc.operations.functions;

import com.microsoft.z3.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.petrinet.Petrinet;

public class NFtoDFconvFunction implements IProcessFunction {
  /**
   * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
   *
   * @return the name of the function
   */
  @Override
  public String getFunctionName() {
    return "nfa2dfa";
  }

  /**
   * Get the available flags for the function described by this interface (e.g. {@code unfair} in
   * {@code abs{unfair}(A)}.
   *
   * @return a collection of available flags (note, no variables may be flags)
   */
  @Override
  public Collection<String> getValidFlags() {
    return Collections.emptySet();
  }

  /**
   * Gets the number of automata to parse into the function.
   *
   * @return the number of arguments
   */
  @Override
  public int getNumberArguments() {
    return 1;
  }

  /**
   * Execute the function on automata.
   *
   * @param id       the id of the resulting automaton
   * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context  the z3 context
   * @param automata a variable number of automata taken in by the function
   * @return the resulting automaton of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
      throws CompilationException {
    //BuildAcceptanceGraphs ba = new BuildAcceptanceGraphs();
    //ba.compose(id,flags,context,automata);
    //System.out.println("Pingo2");
    assert automata.length == 1;
    Automaton nfa = automata[0].copy();
    Automaton dfa = new Automaton(id, !Automaton.CONSTRUCT_ROOT);

    //  maps internal rpresentation to actual set of nodes
    Map<Set<String>, List<AutomatonNode>> stateMap = new HashMap<>();
    Map<String, AutomatonNode> nodeMap = new HashMap<>();
    Set<String> visited = new HashSet<>();

    Stack<Set<String>> fringe = new Stack<>();

    fringe.push(constructClosure(nfa.getRoot(), stateMap));

    Set<String> alphabet = nfa.getAlphabet();
    alphabet.remove(Constant.HIDDEN);

    boolean processedRoot = false;
    while (!fringe.isEmpty()) {
      Set<String> states = fringe.pop();
      String idNode = constructNodeId(stateMap.get(states), nfa.getId());

      if (visited.contains(idNode)) {
        continue;
      }
      if (!nodeMap.containsKey(idNode)) {
        nodeMap.put(idNode, dfa.addNode(idNode));
      }
      AutomatonNode node = nodeMap.get(idNode);

      if (!processedRoot) { // then this is node is the root
        dfa.getRoot().clear();
        dfa.addRoot(node);
        node.setStartNode(true);
        processedRoot = true;
      }

      for (String action : alphabet) {
        Set<String> nextStates = constructStateSet(stateMap.get(states), action, stateMap);

        if (nextStates.isEmpty()) {
          continue;
        }

        String nextId = constructNodeId(stateMap.get(nextStates), nfa.getId());

        if (!nodeMap.containsKey(nextId)) {
          nodeMap.put(nextId, dfa.addNode(nextId));
        }
        AutomatonNode nextNode = nodeMap.get(nextId);

        dfa.addEdge(action, node, nextNode, null, true);

        fringe.push(nextStates);
      }

      visited.add(idNode);
    }

    dfa.getNodes().stream()
        .filter(node -> node.getOutgoingEdges().isEmpty())
        .forEach(node -> node.setTerminal("STOP"));
    //System.out.println("built dfa " +dfa.toString());
    return dfa;
  }

  /**
   *
   * @param node
   * @param stateMap
   * @return  internal reprentation of new node
   */
  private Set<String> constructClosure(Collection<AutomatonNode> node, Map<Set<String>,
      List<AutomatonNode>> stateMap) {
    Set<String> states = new HashSet<>();
    List<AutomatonNode> nodes = new ArrayList<>();

    Stack<AutomatonNode> fringe = new Stack<>();
    node.forEach(fringe::push);

    while (!fringe.isEmpty()) {
      AutomatonNode current = fringe.pop();

      if (states.contains(current.getId())) {
        continue;
      }

      states.add(current.getId());
      nodes.add(current);

      List<AutomatonEdge> edges = current.getOutgoingEdges().stream()
          .filter(AutomatonEdge::isHidden)
          .collect(Collectors.toList());

      edges.forEach(edge -> fringe.push(edge.getTo()));
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
   * @param stateMap
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
        } else if (edge.getLabel().equals(Constant.HIDDEN)) {
          fringe.push(edge.getTo());
        }
      }

      visited.add(current.getId());
    }

    if (!stateMap.containsKey(states)) {
      stateMap.put(states, nextNodes);
    }

    return states;
  }

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

  /**
   * TODO:
   * Execute the function on one or more petrinet.
   *
   * @param id        the id of the resulting petrinet
   * @param flags     the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
   * @param context
   * @param petrinets the variable number of petrinets taken in by the function
   * @return the resulting petrinet of the operation
   * @throws CompilationException when the function fails
   */
  @Override
  public Petrinet compose(String id, Set<String> flags, Context context, Petrinet... petrinets) throws CompilationException {
    return null;
  }
}
