package mc.operations.functions;

import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

public class BuildAcceptanceGraphs implements IProcessFunction {
 //public class BuildAcceptanceGraphs  {

 private   Map<String, List<Set<String>> > node2AcceptanceSets =
   new HashMap<String, List<Set<String>> >();
 private   Map<AutomatonNode, Set<String> > nfanode2ASet =
   new HashMap<AutomatonNode, Set<String> >();

 /**
  * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)}).
  *
  * @return the name of the function
  */
 @Override
 public String getFunctionName() {
  return "accept";
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
 //@Override
 public Automaton compose(String id, Set<String> flags, Context context, Automaton... automata)
   throws CompilationException {
  System.out.println("Starting accept");
  assert automata.length == 1;
  Automaton nfa = automata[0].copy();
  Automaton acceptGraph = new Automaton(id, !Automaton.CONSTRUCT_ROOT);
  build_nfanode2ASet(nfa);
  System.out.println("built nfanode2ASet");

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
    nodeMap.put(idNode, acceptGraph.addNode(idNode));
   }
   AutomatonNode node = nodeMap.get(idNode);



   if (!processedRoot) {  // so must be root!
    acceptGraph.getRoot().clear();
    acceptGraph.addRoot(node);
    node.setStartNode(true);
    processedRoot = true;
   }

   for (String action : alphabet) {
    Set<String> nextStates = constructStateSet(stateMap.get(states), action, stateMap);

    if (nextStates.isEmpty()) {
     continue;
    }

    String nextId = constructNodeId(stateMap.get(nextStates), nfa.getId());
System.out.println("built "+ nextId.toString());
    if (!nodeMap.containsKey(nextId)) {
     nodeMap.put(nextId, acceptGraph.addNode(nextId));
    }
    AutomatonNode nextNode = nodeMap.get(nextId);

    acceptGraph.addEdge(action, node, nextNode, null);

    fringe.push(nextStates);
   }

   visited.add(idNode);
  }

  acceptGraph.getNodes().stream()
    .filter(node -> node.getOutgoingEdges().isEmpty())
    .forEach(node -> node.setTerminal("STOP"));
  printnode2AcceptanceSets();
  return acceptGraph;
 }

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
 private Set<String> constructStateSet(List<AutomatonNode> nodes, String action,
                                       Map<Set<String>, List<AutomatonNode>> stateMap) {
  Set<String> states = new HashSet<>();
  List<AutomatonNode> nextNodes = new ArrayList<>();
  Set<String> visited = new HashSet<>();
System.out.println("starting constructStateSet");

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
  System.out.println("ending constructStateSet");
  return states;
 }

 /**
  *
  * @param nodes list of nfa nodes to make one dfa node
  * @param identifier
  * @return  the dfa nodeid
  * Side effect build the acceptance set and store in node2AcceptanceSets
  */
 private String constructNodeId(List<AutomatonNode> nodes, String identifier) {
  List<Set<String>> acceptance = new ArrayList<>();
  for(AutomatonNode n : nodes) {
     if (!acceptance.contains(nfanode2ASet.get(n))) {
      acceptance.add(nfanode2ASet.get(n));
     }
  }

  String id = identifier + constructLabel(nodes);
  node2AcceptanceSets.put(id,acceptance);
 System.out.println("Adding "+ id.toString()+" "+ acceptance);
  return id;
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
  *
  * @param a automaton
  *   This computes the map nfanode2ASe
  */
 private void build_nfanode2ASet(Automaton a){
    for(AutomatonNode n : a.getNodes()) {
      Set<String> as = n.getOutgoingEdges().stream().
                   distinct().
                   map(AutomatonEdge::getLabel).
                   collect(Collectors.toSet());
       nfanode2ASet.put(n,as);
     System.out.println("++ "+n.getLabel()+" "+nfanode2ASet.get(n).toString() );
    }
 }
 private void printnode2AcceptanceSets(){
  System.out.println("nfa Sets");
  for (AutomatonNode n : nfanode2ASet.keySet()){
    System.out.println(" "+n.getLabel()+" "+nfanode2ASet.get(n).toString() );
  }

  System.out.println("Acceptance Sets");
  for (String nd : node2AcceptanceSets.keySet()) {
    System.out.println(" "+nd+"  "+node2AcceptanceSets.get(nd));
  }

 }
}
