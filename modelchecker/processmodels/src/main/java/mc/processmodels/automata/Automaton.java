package mc.processmodels.automata;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import mc.Constant;
import mc.compiler.Guard;
import mc.compiler.ast.RelabelElementNode;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessModelObject;
import mc.processmodels.ProcessType;
import mc.util.Location;
import mc.util.expr.Expression;

public class Automaton extends ProcessModelObject implements ProcessModel {
  /**
   * When refactored BEWARE of stack of pointers not stack of MODELS
   */


  public static final boolean CONSTRUCT_ROOT = true;
  /**
   * The "default" location for things where location is unimportant
   * An underscore prevents any collisions.
   */
  public static final String DEFAULT_OWNER = "_default";
  public static int tagid = 0;

  private List<AutomatonNode> root;
  public Set<AutomatonNode> getRoot(){
    return root.stream().collect(Collectors.toSet());
  }
  public List<AutomatonNode> getRootList(){
    return root.stream().collect(Collectors.toList());
  }

  private List<String> end = new ArrayList<>();
  public List<String> getEndList(){ return end;}
  public void setEndList(List<String> e){
     for(String nd:e){
       end.add(nd);
     }
  }

  private Map<String, AutomatonNode> nodeMap;
  private Map<String, AutomatonEdge> edgeMap;
  private Map<String, List<AutomatonEdge>> alphabet;

  @Setter
  @Getter
  private Set<String> owners = new HashSet<>();

  @Getter
  private int nodeId = 0;
  @Getter
  private int edgeId = 0;
  @Getter
  private int ownerId = 0;
  private static final String INTERSECTION = "^";
  @Getter
  @Setter
  private Location location;

  @Setter  // Used to house data for Fail -SingeltonFail, ......
  @Getter  // Construced in Nfa2DfaWorks  used in TraceWorks
  private Map<AutomatonNode, List<Set<String>>> node2ReadySets = new TreeMap<>();

  /*
  @Getter
  @Setter
  private HidingNode hiding;  //WHAT THE HELL is part of the AST doing here?
*/
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


  public AutomatonNode deadNode(){
    String dead = "_dead";
    AutomatonNode nd;
    if (nodeMap.keySet().contains(dead)) {
      nd = nodeMap.get(dead);
      nd.setLabelNumber(getNodes().size()+2); // Crappy way to make unique
      System.out.println("OLD zombie " + nd.myString());
    }
    else {
      nd = addNode(dead);
      nd.setLabelNumber(nodeId++);
      System.out.println("Adding zombie " + nd.myString());
    }
    return nd;
  }
  /**
   * needed to prevent size of owner growing
   * @return
   * @throws CompilationException
   */
  public Automaton reown() throws CompilationException{
   //System.out.println("Reowning "+myString());
    ownerId = 0;
    // Some data coupted not all edge owners in automata
    Set<String> validate = new HashSet<>(getOwners());
    for (AutomatonEdge edge : getEdges()) {
      if (edge.getOwnerLocation()!= null) validate.addAll(edge.getOwnerLocation());
    }

    Map<String,String> ownersMap = new TreeMap<>();
    for (String s: validate){
      ownerId++;
      ownersMap.putIfAbsent(s, ((Integer) ownerId).toString());
    }

    owners =  ownersMap.values().stream().collect(Collectors.toSet());
    for (AutomatonEdge edge : getEdges()) {
      //   //System.out.println(" edge "+edge.myString());
      edge.setEdgeOwners(
        edge.getOwnerLocation().stream().map(x->ownersMap.get(x)).collect(Collectors.toSet()));

    }
   //System.out.println("Reowned "+myString());
    return this;
  }
  public static Multimap<String, String> ownerProduct(Automaton automaton1,
                                                      Automaton automaton2)
    throws CompilationException{
    Set<String> preowners1 = automaton1.reown().getOwners();
    Set<String> preowners2 = automaton2.reown().getOwners();
//If the automata share soem location relabel
    Set<String> intersection = new HashSet<>(preowners1);
    intersection.retainAll(preowners2);
    if (intersection.size() > 0) {
      relabelOwners(automaton1,"._1");
      relabelOwners(automaton2,"._2");
      preowners1 = automaton1.getOwners();
      preowners2 = automaton2.getOwners();
    }

    //tricking the lambda expressions to evaluate
    Set<String> owners1 = preowners1;
    Set<String> owners2 = preowners2;

    Multimap<String, String> table = ArrayListMultimap.create();
    owners1.forEach(o1 -> owners2.forEach(o2 -> table.put(o1, o1 + INTERSECTION + o2)));
    owners1.forEach(o1 -> owners2.forEach(o2 -> table.put(o2, o1 + INTERSECTION + o2)));
   //System.out.println("table "+table);
    return table;
  }

  public static void relabelOwners(Automaton aut, String label) {
    aut.getEdges().forEach(e -> {

      Set<String> owners = e.getOwnerLocation().stream()
        .map(o -> o + label)
        .collect(Collectors.toSet());
      Set<String> toRemove = new HashSet<>(e.getOwnerLocation());
      toRemove.forEach(o -> aut.removeOwnerFromEdge(e, o));
      try {
        aut.addOwnersToEdge(e, owners);
      } catch (CompilationException ignored) {
      }
    });
  }

  public void removeEdgeDuplicates (){
     for(AutomatonEdge edge: getEdges()){

     }
  }
  public boolean isSymbolic() {
    return (hiddenVariables != null && hiddenVariables.size() > 0);
  }

  public Automaton(String id) {
    this(id, true);
  }

  public Automaton(String id, boolean constructRoot) {
    super(id, "automata");
    setupAutomaton();
    this.root = new ArrayList<>();
    this.end = new ArrayList<>();
    // only construct a root node if specified to do so
    if (constructRoot) {
      root.add(addNode());
      root.forEach(r -> r.setStartNode(true));
    }
  }
  public static Automaton  singleEventAutomata(String id, String event) throws CompilationException {
    Automaton a = new Automaton(id,false);
      a.setupAutomaton();
      a.root = new ArrayList<>();
      Guard g = new Guard();
      AutomatonNode s = a.addNode();
      AutomatonNode e = a.addNode();
      a.addEdge(event, s,e,g, true,false);
      s.setStartNode(true);
      a.addRoot(s);
      e.setStopNode(true);
      a.reown();
   //System.out.println("Single event Automata " +a.myString());
      return a;
  }

  public void copyProperties(Automaton fromThisautomata) {
    this.location = fromThisautomata.getLocation();
   // this.hiding = fromThisautomata.getHiding();
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
      throw new CompilationException(getClass(), "Unable to add null to root",
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

  public void addEnd(String newEndNode) throws CompilationException {
    // check the the new root is defined
    if (newEndNode == null) {
      throw new CompilationException(getClass(), "Unable to add null to end",
        this.getLocation());
    }
    // check that the new root is part of this automaton
    if (!nodeMap.containsKey(newEndNode)) {
      throw new CompilationException(getClass(), "Unable to set the root node to "
        + newEndNode + ", as the root is not a part of this automaton",
        this.getLocation());
    }

    this.end.add(newEndNode);
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
    Throwable t = new Throwable();
    t.printStackTrace();
    throw new CompilationException(getClass(), "Unable to get the node " + id + " as it does not exist in automaton " + getId(), this.getLocation());
  }

  public AutomatonNode addNode() {
    String id = getNextNodeId();
    return addNode(id);
  }

  //  As  public  two nodes with the same id can be added
  public AutomatonNode addNode(String id) {
    AutomatonNode node = new AutomatonNode(id);

    nodeMap.put(id, node);
    //System.out.println("adding "+ node.myString());
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

      AutomatonEdge ed;
      if (!e.getFrom().equals(oldNode)) {
        ed = addEdge(e.getLabel(), e.getFrom(), newNode, newGuard, false,e.getOptionalEdge());
      } else { // If the node links to itself
        ed = addEdge(e.getLabel(), newNode, newNode, newGuard, false,e.getOptionalEdge());
      }
      addOwnersToEdge(ed, e.getOwnerLocation());
      ed.setOptionalEdge(e.getOptionalEdge());
    }

    for (AutomatonEdge e : oldNode.getOutgoingEdges()) {
      Guard newGuard = null;
      if (e.getGuard() != null) {
        newGuard = e.getGuard().copy();
      }

      AutomatonEdge ed;
      if (!e.getTo().equals(oldNode)) {
        ed = addEdge(e.getLabel(), newNode, e.getTo(), newGuard, false,e.getOptionalEdge());
      } else { // If the node links to itself
        ed = addEdge(e.getLabel(), newNode, newNode, newGuard, false,e.getOptionalEdge());
      }
      addOwnersToEdge(ed, e.getOwnerLocation());
      ed.setOptionalEdge(e.getOptionalEdge());

    }
  }

  public AutomatonNode combineNodes(AutomatonNode node1, AutomatonNode node2, Context context) throws CompilationException, InterruptedException {
    if (!nodeMap.containsKey(node1.getId())) {
      throw new CompilationException(getClass(), node1.getId() + "test3 was not found in the automaton " + getId(), this.getLocation());
    }
    if (!nodeMap.containsKey(node2.getId())) {

      throw new CompilationException(getClass(), node2.getId() + "test4 was not found in the automaton " + getId(), this.getLocation());
    }

    //  //System.out.println("combining nodes "+ node1.getId()+" "+node2.getId()+
    //     " in "+this.toString());

    AutomatonNode node = addNode(); //The new node to replae both old nodes


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
    if (node1.isSTOP()||node2.isSTOP()) {
      node.setStopNode(true);
    }

    removeNode(node1);
    removeNode(node2);
    //System.out.println("new merged Node "+node.myString());
    //System.out.println("nodes merged in "+this.myString());
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
            oldEdge.getTo().equals(edge.getTo())) {
          found = true;
          break;
        }
      }
      if (found == false) {
        ////System.out.println("2 adding "+node.getId()+"-" + edge.getLabel() +"->" +edge.getTo().getId());

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

  /**
   *   does not add duplicate  from- label to
   * @param label
   * @param from
   * @param to
   * @param currentEdgesGuard
   * @param addDefaultOwner
   * @return
   * @throws CompilationException
   */
  public AutomatonEdge addEdge(String label, AutomatonNode from, AutomatonNode to,
                               Guard currentEdgesGuard, boolean addDefaultOwner, boolean opt)
      throws CompilationException {
    String id = getNextEdgeId();
    return addEdge(id, label, from, to, currentEdgesGuard, addDefaultOwner, opt);
  }

  private AutomatonEdge addEdge(String id, String label, AutomatonNode from, AutomatonNode to,
                               Guard currentEdgesGuard, boolean addDefaultOwner, boolean opt)
      throws CompilationException {

    // check that the nodes have been defined
    if (from == null) {

      //System.out.println("id = "+id+" label "+label+ " to "+to);
      throw new CompilationException(getClass(),
        "Unable to add the specified edge as the source was null.\n"+this.toString()+"\n",
           this.getLocation());
    }

    if (to == null) {
    /*
      System.out.println("id = "+id+" label "+label+ " from "+from);
      System.out.println("Stack trace:");
      StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
      for (int i = 1; i < stackTraces.length; i++) {
        System.out.println(stackTraces[i]);
      } */
      throw new CompilationException(getClass(),
        "Unable to add the specified edge as the destination was null.\n"+this.toString()+"\n",
          this.getLocation());
    }
    // check that the nodes are part of this automaton
    if (!nodeMap.containsKey(from.getId())) {
      throw new CompilationException(getClass(),
          "Unable to add the specified edge as " + from.getId() + " is not a part of "
              + this.getId() + " automaton. \n"
              + "Please make sure you aren't linking directly to a parallel composed process!",
          this.getLocation());
    }

    if (!nodeMap.containsKey(to.getId())) {
      throw new CompilationException(getClass(),
          "Unable to add the specified edge as " + to.getId() + " is not a part of "
              + this.getId() + " automaton.  \n"
              + "Please make sure you aren't linking directly to a parallel composed process!",
          this.getLocation());
    }

    // check if there is already an identical edge between the specified nodes
    AutomatonEdge e = this.getEdge(label, from, to);
    if (e != null) {
      return e;
    }
    /* Must be redundent  for atomic abutomaon see above
    //Not identical edge but other edges with same from to and label?
    List<AutomatonEdge> edges = from.getOutgoingEdges().stream()
        .filter(edge -> edge.getLabel().equals(label) &&
                        edge.getTo().getId().equals(to.getId()))
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
    } */

//OK this edge is to be built and added
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

    if (addDefaultOwner) {
      addOwnerToEdge(edge, DEFAULT_OWNER);
    }

    edge.setOptionalEdge(opt);

    return edge;
  }

  public AutomatonEdge getEdge(String label, AutomatonNode from, AutomatonNode to) {
    for (AutomatonEdge edge : edgeMap.values()) {
      if (edge.getTo().equals(to) && edge.getFrom().equals(from) && edge.getId().equals(label)) {
        return edge;
      }
    }

    return null;
  }

  public void addOwnerToEdge(AutomatonEdge edge, String owner) throws CompilationException {
    if (!edgeMap.containsValue(edge)) {
      throw new CompilationException(getClass(), "Cannot add an owner to an edge not in this automaton");
    }
    owners.add(owner);
    edge.addOwnerLocation(owner);
  }

  public void addOwnersToEdge(AutomatonEdge edge, Collection<String> owner)
      throws CompilationException {
    if (!edgeMap.containsValue(edge)) {
      throw new CompilationException(getClass(), "Cannot add an owner to an edge not in this automaton");
    }
    owners.addAll(owner);
    owner.forEach(edge::addOwnerLocation);
  }

  public void removeOwnerFromEdge(AutomatonEdge edge, String ownerToRemove) {
    if (ownerToRemove == null) {
      return;
    }
    edge.removeOwnerLocation(ownerToRemove);

    boolean shouldDelete = edgeMap.values().stream()
        .map(AutomatonEdge::getOwnerLocation)
        .flatMap(Set::stream)
        .noneMatch(ownerToRemove::equals);

    if (shouldDelete) {
      owners.remove(ownerToRemove);
    }
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
    Set<String> out = this.getEdges().stream().map(x->x.getLabel()).collect(Collectors.toSet());

    return out;
    //return alphabet.keySet();
  }

  /**
   *
   * @param automaton input data  to be added to this automaton
   * @return
   * @throws CompilationException
   */

  public Set<AutomatonNode> addAutomaton(Automaton automaton) throws CompilationException {

  //  int num = (int) Math.floor(Math.random() * 100);
    Map<AutomatonNode, AutomatonNode> reNode = new HashMap<>();
    boolean hasRoot = !(this.root == null || this.root.size() == 0);

    // If there is some special location data in prefix, we add all of them synchronously to
    // the existing edges
    if (!automaton.getOwners().contains(DEFAULT_OWNER)) {
      for (AutomatonEdge automatonEdge : getEdges()) {
        addOwnersToEdge(automatonEdge, automaton.getOwners());
      }
      getEdges().forEach(e -> removeOwnerFromEdge(e, Automaton.DEFAULT_OWNER));
    }

    Set<AutomatonNode> thisAutomataRoot = new HashSet<>();
    for (AutomatonNode node : automaton.getNodes()) {
      // AutomatonNode newNode = addNode(node.getId() + num); // THIS is NUTS
      AutomatonNode newNode = addNode();
      reNode.put(node,newNode);
      newNode.copyProperties(node);
      if (newNode.isStartNode()) {
        thisAutomataRoot.add(newNode);
        if (!hasRoot) {
          this.root = new ArrayList<>();
          root.add(newNode);
        } else {
          newNode.setStartNode(false);
        }
      }
    }


    for (AutomatonEdge edge : automaton.getEdges()) {
      AutomatonNode from = reNode.get(edge.getFrom());
      AutomatonNode to = reNode.get(edge.getTo());
      addOwnersToEdge(
          addEdge( edge.getLabel(), from, to, edge.getGuard(), false, edge.getOptionalEdge()),
          edge.getOwnerLocation());
    }
    if (thisAutomataRoot.isEmpty()) {
      throw new CompilationException(getClass(),
          "There was no root found while trying to add an automaton");
    }
    return thisAutomataRoot;
  }


  public String getNextNodeId() {
    return getId() + ".n" + nodeId++;
  }

  public String getNextEdgeId() {
    return getId() + ".e" + edgeId++;
  }

  public String myString(){
    StringBuilder sb = new StringBuilder();
    sb.append("Aut "+this.getId()+ " root ");
      for(AutomatonNode r: root){
      sb.append(r.getId()+" ");
      }
      sb.append("end "+end);
      sb.append("alpa "+this.alphabet.keySet()+" ");
     sb.append(" own" + owners+ "nodes "+getNodes().size()+" edges "+getEdges().size()+"\n");
    for(AutomatonNode nd: getNodes()){ sb.append(nd.myString()+"\n");}
    for(AutomatonEdge ed: getEdges()){
      sb.append(ed.myString()+"\n");
    }
    sb.append(" node2ReadySets\n");
    for(AutomatonNode nd: node2ReadySets.keySet()){
      sb.append(nd.getId()+"->"+node2ReadySets.get(nd)+"\n");
    }
    sb.append("this.nodeId "+this.nodeId);
    return sb.toString();
  }

  public String readySets2String(boolean cong){
    StringBuilder sb = new StringBuilder();
    sb.append("Ready set of "+this.getId()+"\n");
    for (AutomatonNode nd: getNodes()){
      sb.append(nd.getId()+" -> "+nd.quiescentReadySet(cong)+"\n");
    }

    return sb.toString();
  }
  public String toString() {
    String tempto = "";
    String tempfrom = "";
    StringBuilder builder = new StringBuilder();
    builder.append("automaton("+getId()+"):" + this.getId() + "{\n");
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
      builder.append("\t\t").append(node.toString());
   }
    builder.append("\t}\n\tedges:{\n");
    for (AutomatonEdge edge : edgeMap.values()) {

      builder.append("\t\t").append(edge.toString());
    }
    builder.append("\t}\n}");

    return builder.toString();
  }

  public Automaton copy() throws CompilationException {
    Automaton copy = new Automaton(getId(), !CONSTRUCT_ROOT);
//New node objects but with same Id causes problems where unique ids assumed!
    // compute bisim between two automata that nodes with same Id
  /*  copy.nodeId = 0;
    copy.edgeId = 0;
    copy.ownerId = 0; */
   //System.out.println("Start of Copy "+ myString());
    //HACK fix the owners as some times global owners not same as owners on edges
    Set<String > union = new HashSet<>();
    for(AutomatonEdge ed: getEdges()) {
      union.addAll(ed.getOwnerLocation());
    }

    setOwners(union);
    Map<String,String> ownersMap = new TreeMap<>();

   //System.out.println("COPY "+ this.getId()+" owners "+ owners);
    for (String s: union){
      ownerId++;
     //System.out.println("s "+s+" own "+ ownerId);
      ownersMap.putIfAbsent(s, "cp"+((Integer) ownerId).toString());
     //System.out.println("ownersMap "+ s+"->"+ownersMap.get(s));
    }

    owners =  ownersMap.values().stream().collect(Collectors.toSet());

   //System.out.println("XX " + ownersMap);

    Map<AutomatonNode,AutomatonNode> reNode = new HashMap<>();
 //System.out.println("Starting copy of "+this.toString());
    List<AutomatonNode> nodes = getNodes();
    for (AutomatonNode node : nodes) {
      AutomatonNode newNode = copy.addNode();
      reNode.put(node,newNode);
  //System.out.println(" reNode "+node.getId()+"->"+newNode.getId());
      newNode.copyProperties(node);
      if (newNode.isStartNode()) {
        copy.addRoot(newNode);
      }
    }

    List<AutomatonEdge> edges = getEdges();
    for (AutomatonEdge edge : edges) {
      //System.out.println(" edge "+edge.myString());
      AutomatonNode from = reNode.get(edge.getFrom());
      AutomatonNode to = reNode.get(edge.getTo());

      //Change to make copies re id the edge
      AutomatonEdge xedge =  copy.addEdge(edge.getLabel(),
        from, to, edge.getGuard(), false,edge.getOptionalEdge());
   //System.out.println("  Adding "+xedge.myString());
      Set<String> os = edge.getOwnerLocation();
 //System.out.print("os "+os);
      Set<String> newos = new HashSet<>();
        //os.stream().map(x->ownersMap.get(x)).collect(Collectors.toSet());
      for(String s:os) {
        newos.add(ownersMap.get(s));
      }
     //System.out.println(" newos "+ newos);
      copy.addOwnersToEdge(xedge,newos);
      xedge.setOptionalEdge(edge.getOptionalEdge());
      //System.out.println("End of adding Edge"+xedge.myString());
    }
    copy.copyProperties(this);
   //System.out.println("copy Ends "+copy.myString());
    for(AutomatonNode nd: this.node2ReadySets.keySet()){
      copy.node2ReadySets.put(reNode.get(nd),node2ReadySets.get(nd));
    }
    return copy;
  }


  public Automaton reId(String tag) throws CompilationException {
    Automaton reIded = new Automaton(getId()+tag, !CONSTRUCT_ROOT);
    setId(getId()+tag);
//New node objects but with same Id causes problems where unique ids assumed!
    // compute bisim between two automata that nodes with same Id
  /*  copy.nodeId = 0;
    copy.edgeId = 0;
    copy.ownerId = 0; */
    //System.out.println("Start of reId "+ reIded.myString());
    //HACK fix the owners as some times global owners not same as owners on edges
    Set<String > union = new HashSet<>();
    for(AutomatonEdge ed: getEdges()) {
      union.addAll(ed.getOwnerLocation());
    }

    setOwners(union);
    Map<String,String> ownersMap = new TreeMap<>();

    //System.out.println("COPY "+ this.getId()+" owners "+ owners);
    for (String s: union){
      ownerId++;
      //System.out.println("s "+s+" own "+ ownerId);
      ownersMap.putIfAbsent(s, "cp"+((Integer) ownerId).toString());
      //System.out.println("ownersMap "+ s+"->"+ownersMap.get(s));
    }

    owners =  ownersMap.values().stream().collect(Collectors.toSet());

    //System.out.println("XX " + ownersMap);

    Map<AutomatonNode,AutomatonNode> reNode = new HashMap<>();
    //System.out.println("Starting copy of "+this.toString());
    List<AutomatonNode> nodes = getNodes();
    for (AutomatonNode node : nodes) {
      AutomatonNode newNode = reIded.addNode();
      reNode.put(node,newNode);
      //System.out.println(" reNode "+node.getId()+"->"+newNode.getId());
      newNode.copyProperties(node);
      if (newNode.isStartNode()) {
        reIded.addRoot(newNode);
      }
    }

    List<AutomatonEdge> edges = getEdges();
    for (AutomatonEdge edge : edges) {
      //System.out.println(" edge "+edge.myString());
      AutomatonNode from = reNode.get(edge.getFrom());
      AutomatonNode to = reNode.get(edge.getTo());

      //Change to make copies re id the edge
      AutomatonEdge xedge =  reIded.addEdge(edge.getLabel(),
        from, to, edge.getGuard(), false,edge.getOptionalEdge());
      //System.out.println("  Adding "+xedge.myString());
      Set<String> os = edge.getOwnerLocation();
      //System.out.print("os "+os);
      Set<String> newos = new HashSet<>();
      //os.stream().map(x->ownersMap.get(x)).collect(Collectors.toSet());
      for(String s:os) {
        newos.add(ownersMap.get(s));
      }
      //System.out.println(" newos "+ newos);
      reIded.addOwnersToEdge(xedge,newos);
      xedge.setOptionalEdge(edge.getOptionalEdge());
      //System.out.println("End of adding Edge"+xedge.myString());
    }
    reIded.copyProperties(this);
    //System.out.println("reId Ends "+reIded.myString());
    return reIded;
  }



  public ProcessType getProcessType() {
    return ProcessType.AUTOMATA;
  }

  public AutomatonEdge square(AutomatonEdge e,AutomatonEdge ed) {
    if (!e.getFrom().getId().equals(ed.getFrom().getId())) {
   //System.out.println("\nSQUARE ERROR edges "+e.myString()+ " "+ed.myString()+"\n");
      return null;
    }
    for(AutomatonEdge par: e.getTo().getOutgoingEdges()) {
      if (!par.getLabel().equals(ed.getLabel())) continue;
      if (!par.getOwnerLocation().equals(ed.getOwnerLocation()))  continue;
      for(AutomatonEdge par2: ed.getTo().getOutgoingEdges()) {
        if (!par2.getLabel().equals(e.getLabel())) continue;
        if (!par2.getOwnerLocation().equals(e.getOwnerLocation()))  continue;
        if (par2.getTo().equals(par.getTo())) {
     //System.out.println("Square "+par.myString());
          return par;
        }
      }
    }
    return null;
  }
  public String myString(Collection<AutomatonEdge> edges) {
    String out = " {";
    for(AutomatonEdge e: edges){
      out=out+e.myString()+" ** ";
    }
    return out+"}";
  }

  public void tagEvents() {
    for(AutomatonEdge ed: getEdges()) {
      ed.setLabel(ed.getLabel()+":"+tagid++);
    }
  }

  /**
   * @param ed
   * @return the edges parallel to ed
   */
  public  Set<AutomatonEdge> parallelSet(AutomatonEdge ed) {
    Set<String> edOwn = ed.getOwnerLocation();
    //AutomatonEdge ed
    Set<AutomatonNode> visited = new HashSet<>();
    Set<AutomatonEdge> parallel = new HashSet<>();
    Stack<AutomatonNode> fringe = new Stack<>();
    Map<AutomatonNode, AutomatonEdge> selectPar  = new HashMap<>();

    parallel.add(ed);
    fringe.push(ed.getFrom());
    selectPar.putIfAbsent(ed.getFrom(), ed);
    //System.out.println("PAR selectPar "+ed.myString());
//
    while (!fringe.isEmpty()) {  //all nodes in slice
      AutomatonNode current = fringe.pop();
      //System.out.println(" While node " + current.getId()+ " size "+current.getOutgoingEdges().size());
      for (AutomatonEdge edge : current.getOutgoingEdges()) { // all edges leaving outer node
        //System.out.println("select "+myString(selectPar.values()));
        ed = selectPar.get(current);  // must find what is papellel to the known edge

        //System.out.println(" PAR latest " + ed.myString() + " test " + edge.myString());
//if not parallel continue
        for (String own : edge.getOwnerLocation()) {
          if (edOwn.contains(own)) continue;
        }
//if not square continue
        AutomatonEdge next = this.square(edge, ed);
        if (next != null) { // Is square
          parallel.add(next);
          selectPar.putIfAbsent(next.getFrom(), next);
          //System.out.println("  PAR add to selectPar " + next.myString());
          if (!visited.contains(next.getFrom())) {
            //System.out.println(" PAR add to slice " + next.getFrom().getId());
            fringe.push(next.getFrom());
          }
        }
        visited.add(current);
      }
    }
    //System.out.println("  PAR returns " + this.myString(parallel));

    return parallel;
  }

  public String node2ReadySets2String() {
    StringBuilder sb = new StringBuilder();
    for(AutomatonNode nd : node2ReadySets.keySet()){
      sb.append(nd.getId()+"-> "+node2ReadySets.get(nd)+"\n");
    }
    return sb.toString();
  }
}


