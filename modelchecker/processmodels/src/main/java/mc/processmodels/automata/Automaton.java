package mc.processmodels.automata;

import lombok.experimental.var;
import mc.processmodels.ProcessModelObject;
import com.microsoft.z3.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import mc.compiler.Guard;
import mc.compiler.ast.HidingNode;
import mc.compiler.ast.RelabelElementNode;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.ProcessType;
import mc.util.Location;
import mc.util.expr.Expression;

public class Automaton extends ProcessModelObject implements ProcessModel {

  public static final boolean CONSTRUCT_ROOT = true;


  @Getter
  private Set<AutomatonNode> root;
  private Map<String, AutomatonNode> nodeMap;
  private Map<String, AutomatonEdge> edgeMap;
  private Map<String, List<AutomatonEdge>> alphabet;

  private int nodeId;
  private int edgeId;

  @Getter
  @Setter
  private Location location;

  @Getter
  @Setter
  private HidingNode hiding;

  /**
   * use this to decide what guards and assignments to use+display
   * details will appear on edges even if not needed
   * Atomic automaton = null or size 0
   */
  @Getter
  @Setter
  private Set<String> hiddenVariables;


  @Getter
  @Setter
  private Location hiddenVariablesLocation;

  @Getter
  @Setter
  private Location variablesLocation;

  @Getter
  @Setter
  private Set<String> variables;

  @Getter
  @Setter
  private Set<String> alphabetBeforeHiding;

  @Getter
  @Setter
  private List<RelabelElementNode> relabels;

  public boolean isSymbolic(){
    return (hiddenVariables != null &&  hiddenVariables.size() >0);
  }

  public Automaton(String id) {
    this(id, true);
  }

  public Automaton(String id, boolean constructRoot) {
    super(id, "automata");
    setupAutomaton();
    this.root = new HashSet<>();

    // only construct a root node if specified to do so
    if (constructRoot) {
      root.add(addNode());
      root.forEach(r -> r.setStartNode(true));
    }
  }

  public void copyProperties(Automaton fromThisautomata) {
    this.location = fromThisautomata.getLocation();
    this.hiding = fromThisautomata.getHiding();
    this.hiddenVariables = fromThisautomata.getHiddenVariables();
    this.hiddenVariablesLocation = fromThisautomata.getHiddenVariablesLocation();

    this.variables = fromThisautomata.getVariables();
    this.variablesLocation = fromThisautomata.getVariablesLocation();

    this.alphabetBeforeHiding = fromThisautomata.getAlphabetBeforeHiding();
    this.relabels = fromThisautomata.getRelabels();
  }

  private void setupAutomaton() {
    this.nodeMap = new HashMap<>();
    this.edgeMap = new HashMap<>();
    this.alphabet = new HashMap<>();

    this.nodeId = 0;
    this.edgeId = 0;
  }

  public void addRoot(AutomatonNode newRootNode) throws CompilationException {
    // check the the new root is defined
    if (newRootNode == null) {
      throw new CompilationException(getClass(), "Unable to set the root node to null",
        this.getLocation());
    }

    // check that the new root is part of this automaton
    if (!nodeMap.containsKey(newRootNode.getId())) {
      throw new CompilationException(getClass(), "Unable to set the root node to "
        + newRootNode.getId() + ", as the root is not a part of this automaton",
        this.getLocation());
    }

    this.root.add(newRootNode);
  }

  public Set<String> getRootId() {
    return root.stream().map(AutomatonNode::getId).collect(Collectors.toSet());
  }

  public List<AutomatonNode> getNodes() {
    return new ArrayList<>(nodeMap.values());
  }

  public AutomatonNode getNode(String id) throws CompilationException {
    if (nodeMap.containsKey(id)) {
      return nodeMap.get(id);
    }

    throw new CompilationException(getClass(), "Unable to get the node " + id + " as it does not exist in automaton " + getId(), this.getLocation());
  }

  public AutomatonNode addNode() {
    String id = getNextNodeId();
    while (nodeMap.containsKey(id)) {
      id = getNextNodeId();
    }

    return addNode(id);
  }
//  As  public  two nodes with the same id can be added
  public AutomatonNode addNode(String id) {
    AutomatonNode node = new AutomatonNode(id);
    nodeMap.put(id, node);
    return node;
  }

  public boolean removeNode(AutomatonNode node) {
    // check that the specified node is part of this automaton
    if (!nodeMap.containsKey(node.getId())) {
      return false;
    }
    // remove the edges that reference the specified node
    List<AutomatonEdge> edges = node.getIncomingEdges();
    edges.addAll(node.getOutgoingEdges());
    edges.stream()
      .map(ProcessModelObject::getId)
      .forEach(this::removeEdge);
    nodeMap.remove(node.getId());
    return true;
  }

  public Set<AutomatonNode> combineNondeterministic(AutomatonNode node1, Set<AutomatonNode> nodes2, Context context) throws CompilationException, InterruptedException {

    if (!nodeMap.containsKey(node1.getId())) {
      throw new CompilationException(getClass(), node1.getId() + "(node1) was not found in the automaton " + getId(), this.getLocation());
    }
    for (AutomatonNode node2 : nodes2) {
      if (!nodeMap.containsKey(node2.getId())) {
        throw new CompilationException(getClass(), node2.getId() + "(node2) was not found in the automaton " + getId(), this.getLocation());
      }
    }


    Set<AutomatonNode> returnNodes = new HashSet<>();

    for (AutomatonNode nodeToMerge : nodes2) {
      AutomatonNode newNode = addNode();

      //For merging incomming edges
      for (AutomatonEdge edge1 : node1.getIncomingEdges()) {
        for (AutomatonEdge edge2 : nodeToMerge.getIncomingEdges()) {
          processGuards(edge1, edge2, context);
        }
      }

      //For merging outgoing edges
      for (AutomatonEdge edge1 : node1.getOutgoingEdges()) {
        for (AutomatonEdge edge2 : nodeToMerge.getOutgoingEdges()) {
          processGuards(edge1, edge2, context);
        }
      }
      // add the incoming and outgoing edges from both nodes to the combined nodes
      copyEdges(newNode, node1);
      copyEdges(newNode, nodeToMerge);
      // create a union of the metadata from both nodes
      newNode.copyProperties(node1);
      newNode.copyProperties(nodeToMerge);

      newNode.setVariables(null); // Remove the variables
      if (node1.getVariables() != null && nodeToMerge.getVariables() != null) {
        if (node1.getVariables().equals(nodeToMerge.getVariables())) {
          newNode.setVariables(node1.getVariables());
        }
      }


      if (node1.isStartNode()) {
        root.removeAll(Arrays.asList(node1, nodeToMerge));
        root.add(newNode);
        newNode.setStartNode(true);
      }

      returnNodes.add(newNode);
    }

    removeNode(node1);
    nodes2.forEach(this::removeNode);

    return returnNodes;
  }

  private void copyEdges(AutomatonNode newNode, AutomatonNode oldNode) throws CompilationException {

    for (AutomatonEdge e : oldNode.getIncomingEdges()) {
      Guard newGuard = null;
      if (e.getGuard() != null) {
        newGuard = e.getGuard().copy();
      }

      if(!e.getFrom().equals(oldNode)) {
        addEdge(e.getLabel(), e.getFrom(), newNode, newGuard);
      } else { // If the node links to itself
        addEdge(e.getLabel(), newNode, newNode, newGuard);
      }
    }

    for (AutomatonEdge e : oldNode.getOutgoingEdges()) {
      Guard newGuard = null;
      if (e.getGuard() != null) {
        newGuard = e.getGuard().copy();
      }

      if(!e.getTo().equals(oldNode)) {
        addEdge(e.getLabel(), newNode, e.getTo(), newGuard);
      } else {
        addEdge(e.getLabel(), newNode, newNode, newGuard);
      }

    }
  }

  public AutomatonNode combineNodes(AutomatonNode node1, AutomatonNode node2, Context context) throws CompilationException, InterruptedException {
    if (!nodeMap.containsKey(node1.getId())) {
      throw new CompilationException(getClass(), node1.getId() + "test3 was not found in the automaton " + getId(), this.getLocation());
    }
    if (!nodeMap.containsKey(node2.getId())) {

      throw new CompilationException(getClass(), node2.getId() + "test4 was not found in the automaton " + getId(), this.getLocation());
    }

 //   System.out.println("combining nodes "+ node1.getId()+" "+node2.getId()+
 //     " in "+this.toString());

    AutomatonNode node = addNode();


    for (AutomatonEdge edge1 : node1.getIncomingEdges()) {
      for (AutomatonEdge edge2 : node2.getIncomingEdges()) {
        processGuards(edge1, edge2, context);
      }
    }

    for (AutomatonEdge edge1 : node1.getOutgoingEdges()) {
      for (AutomatonEdge edge2 : node2.getOutgoingEdges()) {
        processGuards(edge1, edge2, context);
      }
    }
    // add the incoming and outgoing edges from both nodes to the combined nodes

    processIncomingEdges(node, node1);
    processIncomingEdges(node, node2);
    processOutgoingEdges(node, node1);
    processOutgoingEdges(node, node2);
    // create a union of the metadata from both nodes
    node.copyProperties(node1);
    node.copyProperties(node2);

    node.setVariables(null); // Remove the variables
    if (node1.getVariables() != null && node2.getVariables() != null) {
      if (node1.getVariables().equals(node2.getVariables())) {
        node.setVariables(node1.getVariables());
      }
    }


    if (node1.isStartNode() || node2.isStartNode()) {
      root.removeAll(Arrays.asList(node1, node2));
      root.add(node);
      node.setStartNode(true);
    }


    removeNode(node1);
    removeNode(node2);
  //  System.out.println("nodes merged "+this.toString());
    return node;
  }

  private void processGuards(AutomatonEdge edge1, AutomatonEdge edge2, Context context) throws CompilationException, InterruptedException {
    if (edge1.getLabel().equals(edge2.getLabel()) && edge1.getGuard() != null && edge2.getGuard() != null) {
      Guard guard1 = edge1.getGuard();
      Guard guard2 = edge2.getGuard();
      if (guard1 == null || guard2 == null || guard1.getGuard() == null || guard2.getGuard() == null) {
        return;
      }
      //Since assignment should be the same (same colour) we can just copy most data from either guard.
      Guard combined = guard1.copy();
      //By putting both equations equal to eachother, if we have multiple or operations, then if one matches then it will be solveable.
      if (!guard1.getVariables().isEmpty() && !Expression.equate(guard1, guard2, context))
      //We could take either path
      {
        combined.setGuard(context.mkOr(guard1.getGuard(), guard2.getGuard()));
      } else {
        combined.setGuard(guard1.getGuard());
      }
      edge1.setGuard(combined);
      edge2.setGuard(combined);
    }
  }

  // used to add  incoming edges when merging nodes
  // should only add edges if not already there  code in Automaton
  private void processIncomingEdges(AutomatonNode node, AutomatonNode oldNode) {
    List<AutomatonEdge> edges = oldNode.getIncomingEdges();
    for (AutomatonEdge edge : edges) {
      // Only add edges if not already there  code in Automaton
      boolean found = false;
      for (AutomatonEdge oldEdge : node.getIncomingEdges()) {
        if (oldEdge.getLabel().equals(edge.getLabel()) &&
            oldEdge.getFrom().equals(edge.getFrom())) {
          found = true;
          break;
        }
      }
      if (found == false) {
//System.out.println("1 adding " + edge.getFrom().getId()+"-" + edge.getLabel() +"->" + node.getId());
        node.addIncomingEdge(edge);
        edge.setTo(node);
        oldNode.removeIncomingEdge(edge);
      }
    }
  }

  private void processOutgoingEdges(AutomatonNode node, AutomatonNode oldNode) {
    List<AutomatonEdge> edges = oldNode.getOutgoingEdges();
    for (AutomatonEdge edge : edges) {
      // Only add edges if not already there  code in Automaton
      AutomatonEdge e = this.getEdge(edge.getLabel(), node, edge.getTo());
      boolean found = false;
      for (AutomatonEdge oldEdge : node.getOutgoingEdges()) {
        if (oldEdge.getLabel().equals(edge.getLabel()) &&
          oldEdge.getTo().equals(edge.getTo()) ) {
          found = true;
          break;
        }
      }
      if (found == false) {
 // System.out.println("2 adding "+node.getId()+"-" + edge.getLabel() +"->" +edge.getTo().getId());

        node.addOutgoingEdge(edge);
        edge.setFrom(node);
        oldNode.removeOutgoingEdge(edge);
      }
    }
  }

  public int getNodeCount() {
    return nodeMap.size();
  }

  public List<AutomatonEdge> getEdges() {
    return edgeMap.entrySet().stream()
      .map(Map.Entry::getValue)
      .collect(Collectors.toList());
  }

  public AutomatonEdge getEdge(String id) throws CompilationException {
    if (edgeMap.containsKey(id)) {
      return edgeMap.get(id);
    }

    throw new CompilationException(getClass(), "Edge " + id + " was not found in the automaton " + getId(), this.getLocation());
  }

  public AutomatonEdge addEdge(String label, AutomatonNode from, AutomatonNode to,
                               Guard currentEdgesGuard)
    throws CompilationException {
    String id = getNextEdgeId();
    return addEdge(id, label, from, to, currentEdgesGuard);
  }

  public AutomatonEdge addEdge(String id, String label, AutomatonNode from, AutomatonNode to,
                               Guard currentEdgesGuard) throws CompilationException {
    // check that the nodes have been defined
    if (currentEdgesGuard == null) {
      System.out.print("addEdge guard null \n");
    } else {
      System.out.print("addEdge guard " + currentEdgesGuard.myString() + "\n");
    }
    if (from == null) {
      throw new CompilationException(getClass(), "Unable to add the specified edge as the source was null.", this.getLocation());
    }

    if (to == null) {
      throw new CompilationException(getClass(), "Unable to add the specified edge as the destination was null.", this.getLocation());
    }
    // check that the nodes are part of this automaton
    if (!nodeMap.containsKey(from.getId())) {
      throw new CompilationException(getClass(), "Unable to add the specified edge as " + from.getId() + " is not a part of " + this.getId() + " automaton. \nPlease make sure you aren't linking directly to a parallel composed process!", this.getLocation());
    }

    if (!nodeMap.containsKey(to.getId())) {
      throw new CompilationException(getClass(), "Unable to add the specified edge as " + to.getId() + " is not a part of " + this.getId() + " automaton.  \nPlease make sure you aren't linking directly to a parallel composed process!", this.getLocation());
    }

    // check if there is already an identical edge between the specified nodes
    AutomatonEdge e = this.getEdge(label, from, to);
    if (e != null) {
      return e;
    }
    List<AutomatonEdge> edges = from.getOutgoingEdges().stream()
      .filter(edge -> edge.getLabel().equals(label) && edge.getTo().getId().equals(to.getId()))
      .collect(Collectors.toList());

    if (edges.size() > 0) {
      for (AutomatonEdge edge : edges) {

        if (edge.getGuard() == null && currentEdgesGuard == null) {
          return edge;
        }


        if (edge.getGuard() != null && edge.getGuard().equals(currentEdgesGuard)) {
          return edge;
        }
      }
    }

    AutomatonEdge edge = new AutomatonEdge(id, label, from, to);
    edge.setGuard(currentEdgesGuard);

    // add edge reference to the incoming and outgoing nodes
    from.addOutgoingEdge(edge);
    to.addIncomingEdge(edge);

    // add edge to the edge and alphabet maps
    if (!alphabet.containsKey(label)) {
      alphabet.put(label, new ArrayList<>());
    }
    alphabet.get(label).add(edge);
    edgeMap.put(id, edge);

    return edge;
  }

  public AutomatonEdge getEdge(String label, AutomatonNode from, AutomatonNode to) {
    for (AutomatonEdge edge : edgeMap.values()) {
      if (edge.getTo() == to && edge.getFrom() == from && edge.getId().equals(label)) {
        return edge;
      }
    }

    return null;
  }


  public boolean removeEdge(AutomatonEdge edge) {
    // check that the specified edge is part of this automaton
    if (!edgeMap.containsKey(edge.getId())) {
      return false;
    }

    // remove references to this edge
    edge.getFrom().removeOutgoingEdge(edge);
    edge.getTo().removeIncomingEdge(edge);
    edgeMap.remove(edge.getId());
    return true;
  }

  public boolean removeEdge(String id) {
    if (!edgeMap.containsKey(id)) {
      return false;
    }

    return removeEdge(edgeMap.get(id));
  }

  public void relabelEdges(String oldLabel, String newLabel) {
    if (alphabet.containsKey(oldLabel)) {
      List<AutomatonEdge> edges = alphabet.get(oldLabel);
      edges.forEach(edge -> edge.setLabel(newLabel));
      if (alphabet.containsKey(newLabel)) {
        edges.addAll(alphabet.get(newLabel));
      }
      alphabet.put(newLabel, edges);
      alphabet.remove(oldLabel);
    }
  }

  public int getEdgeCount() {
    return edgeMap.size();
  }

  public Set<String> getAlphabet() {
    return alphabet.keySet();
  }

  public Set<AutomatonNode> addAutomaton(Automaton automaton) throws CompilationException {
    int num = (int) Math.floor(Math.random() * 100);
    boolean hasRoot = !(this.root == null || this.root.size() == 0);

    Set<AutomatonNode> thisAutomataRoot = new HashSet<>();
    for (AutomatonNode node : automaton.getNodes()) {


      AutomatonNode newNode = addNode(node.getId() + num);

      newNode.copyProperties(node);
      if (newNode.isStartNode()) {
        thisAutomataRoot.add(newNode);
        if (!hasRoot) {
          this.root = new HashSet<>();
          root.add(newNode);
        } else {
          newNode.setStartNode(false);
        }
      }

    }


    for (AutomatonEdge edge : automaton.getEdges()) {
      AutomatonNode from = getNode(edge.getFrom().getId() + num);
      AutomatonNode to = getNode(edge.getTo().getId() + num);

      this.addEdge(edge.getId() + num, edge.getLabel(), from, to, edge.getGuard());

    }
    if (thisAutomataRoot.isEmpty()) {
      throw new CompilationException(getClass(), "There was no root found while trying to add an automaton");
    }
    return thisAutomataRoot;
  }


  public String getNextNodeId() {
    return getId() + ".n" + nodeId++;
  }

  public String getNextEdgeId() {
    return getId() + ".e" + edgeId++;
  }

  public String toString() {
    String tempto = "";
    String tempfrom = "";
    StringBuilder builder = new StringBuilder();
    builder.append("automaton:" + this.getId()+"{\n");
    if (this.hiddenVariables != null) {
      builder.append("\thiddenVar:{");
      for (String var : this.hiddenVariables) {
        builder.append(", " + var);
      }
    }
    if (this.variables != null) {
      builder.append("}\n\tvariables:{");
      for (String var : this.variables) {
        builder.append(", " + var);
      }
    }
    builder.append("}\n\tnodes:{\n");
    for (AutomatonNode node : nodeMap.values()) {
      builder.append("\t\t").append(node.getId()).
                             append(" c= "+ node.getColour()).toString();
      if (node.getGuard() !=null) {
        builder.append(" g= "+ node.getGuard().myString());
      } else {builder.append(" Guard=null");}
      if (node == root) {
        builder.append("(root)");
      }

      if(node.isStartNode()) {
        builder.append("(Start)");

      }

      if (node.isTerminal()) {
        builder.append("(").append(node.getTerminal()).append(")");
      }
      builder.append("\n");
    }
    builder.append("\t}\n\tedges:{\n");
    for (AutomatonEdge edge : edgeMap.values()) {
      if(!nodeMap.containsValue(edge.getTo())) {tempto="NOT TO ";}else{tempto="";}
      if(!nodeMap.containsValue(edge.getFrom())) {tempfrom="NOT From ";}else{tempfrom="";}
      builder.append("\t\t").append(tempfrom+edge.getFrom().getId()
        //+" "+edge.getFrom().getColour()
        ).
           append("-").append(edge.getLabel()).append(">").
           append(tempto+edge.getTo().getId()+" col "+edge.getTo().getColour()).
           append(edge.getGuard() == null ? "null" : edge.getGuard().myString()).
           append("\n");

    }
    builder.append("\t}\n}");

    return builder.toString();
  }

  public Automaton copy() throws CompilationException {
    Automaton copy = new Automaton(getId(), !CONSTRUCT_ROOT);

    copy.nodeId = nodeId;
    copy.edgeId = edgeId;
    List<AutomatonNode> nodes = getNodes();
    for (AutomatonNode node : nodes) {
      AutomatonNode newNode = copy.addNode(node.getId());
      newNode.copyProperties(node);
      if (newNode.isStartNode()) {
        copy.addRoot(newNode);
      }

    }

    List<AutomatonEdge> edges = getEdges();
    for (AutomatonEdge edge : edges) {
      AutomatonNode from = copy.getNode(edge.getFrom().getId());
      AutomatonNode to =   copy.getNode(edge.getTo().getId());
      /*
      if (!nodeMap.containsKey(from.getId())) {
        System.out.println("from failure " + from.myString());
      }else{if (nodeMap.get(from.getId()) != from) {System.out.println("FromFail");}
      }
      if (!nodeMap.containsKey(to.getId())) { System.out.println("to failure "+to.myString());
      }else{if (nodeMap.get(to.getId()) != to) {System.out.println("ToFail");}
      }
      */
          copy.addEdge(edge.getId(), edge.getLabel(), from, to, edge.getGuard());

    }

    copy.copyProperties(this);


    return copy;
  }

  public ProcessType getProcessType() {
    return ProcessType.AUTOMATA;
  }
}
