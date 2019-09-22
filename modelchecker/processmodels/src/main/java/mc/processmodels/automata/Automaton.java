package mc.processmodels.automata;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rits.cloning.Cloner;
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
  private static final String INTERSECTION = "^";
  public static int tagid = 0;
  private List<AutomatonNode> root;
  private List<String> end = new ArrayList<>();
  private Map<String, AutomatonNode> nodeMap;
  private Map<String, AutomatonEdge> edgeMap;
  private Map<String, List<AutomatonEdge>> alphabet;
  // @Setter Must not set to singleton
  @Getter
  private Set<String> owners = new HashSet<>();
  @Getter
  private int nodeId = 0;
  @Getter
  private int edgeId = 0;
  @Getter
  private int ownerId = 0;
  @Setter
  @Getter
  private boolean sequential = false;
  public boolean isSequential(){return sequential;}
  // abstraction can muck up concurrancy hence set this flag
  @Getter
  @Setter
  private Location location;
  @Setter  // Used to house data for Fail -SingeltonFail, ......
  @Getter  // Construced in Nfa2DfaWorks  used in TraceWorks
  private Map<AutomatonNode, List<Set<String>>> node2ReadySets = new TreeMap<>();
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

  public Automaton(String id) {
    this(id, true);
  }

  /*
  @Getter
  @Setter
  private HidingNode hiding;  //WHAT THE HELL is part of the AST doing here?
*/
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

  public static Multimap<String, String> ownerProduct(Automaton automaton1,
                                                      Automaton automaton2)
    throws CompilationException {
    Set<String> preowners1 = automaton1.reown().getOwners();
    Set<String> preowners2 = automaton2.reown().getOwners();
//If the automata share soem location relabel
    Set<String> intersection = new HashSet<>(preowners1);
    intersection.retainAll(preowners2);
    if (intersection.size() > 0) {
      relabelOwners(automaton1, "._1");
      relabelOwners(automaton2, "._2");
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

  /*

   */
  public static void relabelOwners(Automaton aut, String label) {

    aut.getEdges().forEach(edge -> {
      Set<String> owners = edge.getEdgeOwners().stream()
        .map(o -> o + label)
        .collect(Collectors.toSet());
      Set<String> toRemove = new HashSet<>(edge.getEdgeOwners());
      toRemove.forEach(o -> aut.removeOwnerFromEdge(edge, o));
      try {
        aut.addOwnersToEdge(edge, owners);
      } catch (CompilationException ignored) {
      }

        Set<String> ownersOpt = edge.getOptionalOwners().stream()
            .map(o -> o + label)
            .collect(Collectors.toSet());
        edge.setOptionalOwners(ownersOpt);


    });
  }

  public static Automaton singleEventAutomata(String id, String event) throws CompilationException {
    Automaton a = new Automaton(id, false);
    a.setupAutomaton();
    a.root = new ArrayList<>();
    Guard g = new Guard();
    AutomatonNode s = a.addNode();
    AutomatonNode e = a.addNode();
    a.addEdge(event, s, e, g, true, false);
    s.setStartNode(true);
    a.addRoot(s);
    e.setStopNode(true);
    a.reown();
    //System.out.println("Single event Automata " +a.myString());
    return a;
  }

  public Set<AutomatonNode> getRoot() {
    return root.stream().collect(Collectors.toSet());
  }

  public void setRoot(Set<AutomatonNode> rt) {
    List<AutomatonNode> r = new ArrayList<>();
    for (AutomatonNode nd : rt) {
      r.add(nd);
    }
    root = r;
  }

  public List<AutomatonNode> getRootList() {
    return root.stream().collect(Collectors.toList());
  }

  public List<String> getEndList() {
    return end;
  }

  public void setEndList(List<String> e) {
    end = new ArrayList<>();
    for (String nd : e) {
      end.add(nd);
    }
  }

  public Set<AutomatonNode> endNodes() {
    return end.stream().map(x -> nodeMap.get(x)).collect(Collectors.toSet());
  }

  /*
     When Abstraction saturated the graph it may destroy concurrancy and
      Hence the OwnersRule will give spurious results SO set automata to sequential
   */
  public void setAllOwnerstoSeq(){
    this.setSequential(true);
    Set<String> o = new TreeSet<>();
    o.add("seq");
    setOwners(o);
    for(AutomatonEdge ed: this.getEdges()){
      ed.setEdgeOwners(o);
    }
  }
  public void setOwners(Collection<String> os) {
    Set<String> eos = new TreeSet<>();
    os.forEach(o -> eos.add(o));
    owners = eos;
  }

  public void cleanNodeLables() {
    int nodeLabelNumber = 0;
    for (AutomatonNode nd : getNodes()) {
      nd.setLabelNumber(nodeLabelNumber++);
    }

  }

  public boolean validateAutomaton(String ping) throws CompilationException {
    boolean r = true;

    try {
      r = validateAutomaton();  // throw error on failure
      //System.out.println(ping + " true");
    } catch (CompilationException e) {
      //System.out.println(ping + " false ");
      throw e;
    }
    return r;
  }

  public boolean validateAutomaton() throws CompilationException {
    //System.out.println("              Validate "+getId());
    boolean ok = true;

    List<String> endList = getEndList();
    for (String key : endList) {
      if (!nodeMap.containsKey(key)) {
        System.out.println("End " + endList + " but " + key + " not found");
        ok = false;
      } else if (!nodeMap.get(key).isSTOP()) {
        System.out.println("End " + endList + " but " + nodeMap.get(key).myString());
        ok = false;
      }
    }


    Set<AutomatonNode> root = getRoot();
    Set<String> rootS = root.stream().map(AutomatonNode::getId).collect(Collectors.toSet());
    for (String key : rootS) {
      if (!nodeMap.containsKey(key)) {
        System.out.println("root " + rootS + " but " + key + " not found");
        ok = false;
      } else if (!nodeMap.get(key).isStartNode()) {
        System.out.println("Root " + rootS + " but " + nodeMap.get(key).myString());
        ok = false;
      }
    }

    for (AutomatonNode nd : getNodes()) {
      if (nd.isSTOP() && !endList.contains(nd.getId())) {
        System.out.println("End " + endList + " but " + nd.myString());
        ok = false;
      }
      if (nd.isStartNode() && !rootS.contains(nd.getId())) {
        System.out.println("Root " + root + " but " + nd.myString());
        ok = false;
      }
      for (AutomatonEdge ed : nd.getIncomingEdges()) {
        if (!edgeMap.containsValue(ed)) {
          System.out.println(nd.getId() + " <- NO in edge " + ed.getId());
          ok = false;
        }
        if (!ed.getTo().equalId(nd)) {
          System.out.println(ed.getId() + " -> " + nd.getId() + " nd ERROR");
          ok = false;
        }
      }
      for (AutomatonEdge ed : nd.getOutgoingEdges()) {
        if (!edgeMap.containsValue(ed)) {
          System.out.println(nd.getId() + " -> NO out edge " + ed.getId());
          ok = false;
        }
        if (!ed.getFrom().equalId(nd)) {
          System.out.println(nd.getId() + " -> " + ed.getId() + " nd ERROR");
          ok = false;
        }
      }
    }
    for (AutomatonEdge ed : getEdges()) {
      //System.out.println("EDGE "+ed.myString());
      if (!nodeMap.containsValue(ed.getFrom())) {
        System.out.println(ed.getId() + " <- " + ed.getFrom().getId() + " NO From");
        ok = false;
      }
      if (!nodeMap.containsValue(ed.getTo())) {
        System.out.println(ed.getId() + " -> " + ed.getTo().getId() + " NO To");
        ok = false;
      }
      if (!ed.getTo().getIncomingEdges().contains(ed)) {
        System.out.println(ed.getId() + " -> " + ed.getTo().getId() + " ed ERROR");
        ok = false;
      }
      if (!ed.getFrom().getOutgoingEdges().contains(ed)) {
        System.out.println(ed.getFrom().getId() + " -> " + ed.getId() + " ed ERROR");
        ok = false;
      }
    }
    if (!ok) {//KEEP BOTH  as Exception passed on up to gain info but call stack needed
      Throwable t = new Throwable();
      t.printStackTrace();
      System.out.println("SORT the Automaton OUT \n" + this.myString() + "\nSORT OUT AUT ABOVE\n");
      throw new CompilationException(getClass(), " invalid Automaton " + this.getId());
    } else {
      //System.out.println(this.getId()+ " is valid");
    }
    //System.out.println(this.getId()+ " is valid = "+ok);
    return ok;
  }

  /*
     restrict automaton to having only reachable nodes and edges
     delta edges dropped.
   */
  public void reachable() throws CompilationException {
    //System.out.println("Reachable start "+ myString());
    validateAutomaton("Valid reachable start "+getId()+ " ");
    List<AutomatonNode> todo = new ArrayList<>();
    Map<String, AutomatonNode> sofarN = new TreeMap<>();
    Map<String, AutomatonEdge> sofarE = new TreeMap<>();
//must prune all delta edges else recursive routine has dangeling incoming/outgoing
    //avoid Concurrent updates
    Set<AutomatonEdge> eds = edgeMap.values().stream().collect(Collectors.toSet());
    for (AutomatonEdge ed : eds) {
      if (ed.getLabel().equals(Constant.DEADLOCK)) {
        removeEdge(ed);
      }
    }
    root.stream().distinct().forEach(x -> todo.add(x));

    while (!todo.isEmpty()) {
      AutomatonNode current = todo.remove(0);
      sofarN.put(current.getId(), current);
      //System.out.println("Adding "+current.myString());
      for (AutomatonEdge ed : current.getOutgoingEdges()) {
        sofarE.put(ed.getId(), ed);
        //System.out.println("Adding "+ed.myString());
        AutomatonNode next = ed.getTo();
        if (!sofarN.containsKey(next.getId()) && !todo.contains(next)) {
          todo.add(next);
        }
      }
    }
    //System.out.println("Pingo");
    // may still have dangeling incoming from unreachable edges
    Set<AutomatonEdge> eds2 = edgeMap.values().stream().collect(Collectors.toSet());
    for(AutomatonEdge ed: eds2){
      if (!sofarE.containsValue(ed)) {
        removeEdge(ed);
      }
    }
    nodeMap = sofarN;
    edgeMap = sofarE;
    setRootFromNodes();
    setEndFromNodes();
    setAlphabetFromEdges();
    //System.out.println("Reachable end "+ myString());
    validateAutomaton("Valid reachable end "+getId()+ " ");
  }

  public Map<String, Set<String>> eventNames2Owner() {
    Map<String, Set<String>> a2o = new TreeMap<>();
    for (AutomatonEdge e : this.getEdges()) {
      for (String o : e.getEdgeOwners()) {
        Set<String> os = a2o.get(e.getLabel());
        if (a2o.containsKey(e.getLabel())) {
          os.add(o);
        } else {
          Set<String> ol = Stream.of(o).collect(Collectors.toSet());
          a2o.put(e.getLabel(), ol);
        }
      }
    }
    return a2o;
  }

  /* ONLY called from Nfa2dfaWorks and after ReId  that forces
      nodeId and labelNumber to be the same and Unique for each node
   */
  public AutomatonNode deadNode() {
    return getDeadNode("_dead");
  }

  public AutomatonNode zombieNode() {
    return getDeadNode("_zombie");
  }

  public AutomatonNode getDeadNode(String dead) {
    AutomatonNode nd;
    if (nodeMap.keySet().contains(dead)) {
      nd = nodeMap.get(dead);
      //System.out.println("OLD zombie " + nd.myString());
    } else {
      nd = addNode(dead);
      nd.setLabelNumber(nodeId++); //  works
      //System.out.println("Adding zombie " + nd.myString());
    }
    return nd;
  }

  /**
   * needed to prevent size of owner growing
   *
   * @return
   * @throws CompilationException
   */
  public Automaton reown() throws CompilationException {
    //System.out.println("Reowning "+myString());
    ownerId = 0;
    // Some data coupted not all edge owners in automata
    Set<String> validate = new HashSet<>(getOwners());
    for (AutomatonEdge edge : getEdges()) {
      if (edge.getEdgeOwners() != null) validate.addAll(edge.getEdgeOwners());
    }

    Map<String, String> ownersMap = new TreeMap<>();
    for (String s : validate) {
      ownerId++;
      ownersMap.putIfAbsent(s, ((Integer) ownerId).toString());
    }

    owners = ownersMap.values().stream().collect(Collectors.toSet());
    for (AutomatonEdge edge : getEdges()) {
      //   //System.out.println(" edge "+edge.myString());
      edge.setEdgeOwners(edge.getEdgeOwners().stream().
          map(x -> ownersMap.get(x)).collect(Collectors.toSet()));
      edge.setOptionalOwners(edge.getOptionalOwners().stream().
          map(x -> ownersMap.get(x)).collect(Collectors.toSet()));
    }
    //System.out.println("Reowned "+myString());
    return this;
  }

  public boolean isSymbolic() {
    return (hiddenVariables != null && hiddenVariables.size() > 0);
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
    this.sequential = fromThisautomata.isSequential();
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
      //System.out.println("ERROR \n"+this.myString()+"ERROR");
      Throwable t = new Throwable();
      t.printStackTrace();
      throw new CompilationException(getClass(), "Unable to set the root node to "
        + newRootNode.getId() + ", as the root is not a part of this automaton",
        this.getLocation());

    }
    if (!root.contains(newRootNode))
      this.root.add(newRootNode);
  }

  public void setEndFromNodes() {
    List<String> endL = new ArrayList<>();
    for (AutomatonNode nd : nodeMap.values()) {
      //System.out.println("Nd "+nd.myString());
      if (nd.isSTOP()) {
        endL.add(nd.getId());
        //System.out.println("newEnd"+endL);
      }
    }
    this.end = endL;
  }

  public void setRootFromNodes() {
    List<AutomatonNode> rootL = new ArrayList<>();
    for (AutomatonNode nd : nodeMap.values()) {
      //System.out.println("Nd "+nd.myString());
      if (nd.isStartNode()) {
        rootL.add(nd);
        //System.out.println("newRoot"+endL);
      }
    }
    this.root = rootL;
  }

  public void removeEnd(String key) {
    end.remove(key);
  }

  public void addEnd(String newEndNode) throws CompilationException {
    //System.out.println("addEnd "+this.getEndList() + "  add "+newEndNode);
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
    if (!end.contains(newEndNode))
      this.end.add(newEndNode);
    //System.out.println("addEnd "+this.getEndList());
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
    this.getRoot().remove(node);
    this.end.remove(node.getId());
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

    for (AutomatonEdge edge : oldNode.getIncomingEdges()) {
      Guard newGuard = null;
      if (edge.getGuard() != null) {
        newGuard = edge.getGuard().copy();
      }

      AutomatonEdge ed;
      if (!edge.getFrom().equals(oldNode)) {
        ed = addEdge(edge.getLabel(), edge.getFrom(), newNode, newGuard, edge.getOptionalOwners(), edge.getOptionalEdge());
      } else { // If the node links to itself
        ed = addEdge(edge.getLabel(), newNode, newNode, newGuard, edge.getOptionalOwners(), edge.getOptionalEdge());
      }
      addOwnersToEdge(ed, edge.getEdgeOwners());
      ed.setOptionalEdge(edge.getOptionalEdge());
    }

    for (AutomatonEdge e : oldNode.getOutgoingEdges()) {
      Guard newGuard = null;
      if (e.getGuard() != null) {
        newGuard = e.getGuard().copy();
      }

      AutomatonEdge ed;
      if (!e.getTo().equals(oldNode)) {
        ed = addEdge(e.getLabel(), newNode, e.getTo(), newGuard, e.getOptionalOwners(), e.getOptionalEdge());
      } else { // If the node links to itself
        ed = addEdge(e.getLabel(), newNode, newNode, newGuard, e.getOptionalOwners(), e.getOptionalEdge());
      }
      addOwnersToEdge(ed, e.getEdgeOwners());
      ed.setOptionalEdge(e.getOptionalEdge());

    }
  }

  public void removeDuplicateEdges() {
    List<AutomatonEdge> found = new ArrayList<>();
    List<AutomatonEdge> edges = this.getEdges().stream().collect(Collectors.toList());
    for (AutomatonEdge edge : edges) {
      //System.out.println("  edge "+edge.myString());
      boolean f = false;
      for (AutomatonEdge ed : found) {
        if (edge.equals(ed)) {
          //System.out.println("    Duplicate " + ed.myString());
          this.removeEdge(ed.getId());
          f = true;
          break;
        } else {
          //System.out.print("    NO "+ed.getId());
        }
      }
      if (!f) {
        found.add(edge);
        //System.out.println(" FOUND.add " + edge.myString());
      }
    }
  }


  /**
   * merges second node into the first keeping the firsts id
   * ONLY used in SIMP
   *
   * @param node1
   * @param node2
   * @param context
   * @return
   * @throws CompilationException
   * @throws InterruptedException
   */
  public void mergeAutNodes( AutomatonNode node1, AutomatonNode node2, Context context) throws CompilationException, InterruptedException {
     mergeAutNodes( node1, node2, context, false);
     return;
  }

  public void mergeAutNodes( AutomatonNode node1, AutomatonNode node2, Context context, boolean force) throws CompilationException, InterruptedException {
    Automaton ain = this;
    if (!nodeMap.containsKey(node1.getId())) {
      throw new CompilationException(getClass(), node1.getId() + "test3 was not found in the automaton " + getId(), this.getLocation());
    }
    if (!nodeMap.containsKey(node2.getId())) {

      throw new CompilationException(getClass(), node2.getId() + "test4 was not found in the automaton " + getId(), this.getLocation());
    }
    //System.out.println("Merging "+node1.getId()+"\n <= "+node2.getId());
// for symbilic TOBE reviewed
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

    processIncomingEdges(ain, node1, node2);
    processOutgoingEdges(ain, node1, node2);

    if (!node1.isStartNode() && node2.isStartNode()) {
      root.add(node1);
      root.remove(node2);
      node1.setStartNode(true);
    }
    if (!node1.isSTOP() && node2.isSTOP()) {
      node1.setStopNode(true);
      this.addEnd(node1.getId());
      end.remove(node2.getId());

    }

    nodeMap.remove(node2.getId());
    //removeNode(node2);
    //this.setEndList();
    //System.out.println("new merged Node "+node1.myString());
    //System.out.println("Automaton.java nodes merged in "+ain.myString());
    return;
  }


  /*

   */
  private void processGuards(AutomatonEdge edge1, AutomatonEdge edge2, Context context) throws CompilationException, InterruptedException {
    if (edge1.getLabel().equals(edge2.getLabel()) && edge1.getGuard() != null && edge2.getGuard() != null) {
      Guard guard1 = edge1.getGuard();
      Guard guard2 = edge2.getGuard();
      if (guard1 == null || guard2 == null || guard1.getGuard() == null || guard2.getGuard() == null) {
        return;
      }
      //Since assignment should be the same (same colour) we can just copy most data from either guard.
      Guard combined = guard1.copy();
      //By putting both equations equal to eachother,
      // if we have multiple or operations, then if one matches then it will be solveable.
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
  private void processIncomingEdges(Automaton ain, AutomatonNode node, AutomatonNode oldNode) {
    List<AutomatonEdge> edges = oldNode.getIncomingEdges();
    //System.out.println("processIncomingEdges "+node.getId()+" <- "+oldNode.getId());
    for (AutomatonEdge edge : edges) {
      //System.out.println("processesing "+edge.myString());
      // Only add edges if not already there  code in Automaton
      Set<String> own = edge.getEdgeOwners();
      boolean found = false;
      for (AutomatonEdge oldEdge : node.getIncomingEdges()) {
        if (oldEdge.getLabel().equals(edge.getLabel()) &&
          oldEdge.getFrom().equals(edge.getFrom())) {
          found = true;
          //System.out.println("found "+oldEdge.myString());
          break;
        }
      }
      if (found == false) {
        //System.out.println("1 adding " + edge.getFrom().getId()+"-" + edge.getLabel() +"->" + node.getId());
        node.addIncomingEdge(edge);
        edge.setTo(node);
        ain.nodeMap.put(node.getId(), node);
        ain.edgeMap.put(edge.getId(), edge);
        //System.out.println(ain.myString());
        //oldNode.removeIncomingEdge(edge);
      } else {
        //System.out.println("In not needed "+edge.myString());
        ain.edgeMap.remove(edge.getId());
        edge.getFrom().removeOutgoingEdge(edge);
        //System.out.println(" edge gone!  "+ edge.getFrom().myString());
      }
    }
  }

  private void processOutgoingEdges(Automaton ain, AutomatonNode node, AutomatonNode oldNode) {
    List<AutomatonEdge> edges = oldNode.getOutgoingEdges();
    //System.out.println("processOutgoingEdges "+node.getId()+" <- "+oldNode.getId());
    for (AutomatonEdge edge : edges) {
      //System.out.println("processesing "+edge.myString());
      // Only add edges if not already there  code in Automaton
      boolean found = false;
      for (AutomatonEdge oldEdge : node.getOutgoingEdges()) {
        if (oldEdge.getLabel().equals(edge.getLabel()) &&
          oldEdge.getTo().equals(edge.getTo())) {
          found = true;
          //System.out.println("found "+oldEdge.myString());
          break;
        }
      }
      if (found == false) {
        //System.out.println("2 adding "+node.getId()+"-" + edge.getLabel() +"->" +edge.getTo().getId());
        node.addOutgoingEdge(edge);
        edge.setFrom(node);
        ain.nodeMap.put(node.getId(), node);
        ain.edgeMap.put(edge.getId(), edge);
        //System.out.println(ain.myString());
        //  oldNode.removeOutgoingEdge(edge);
      } else {
        //System.out.println("Out not needed "+edge.myString());
        ain.edgeMap.remove(edge.getId());
        edge.getTo().removeIncomingEdge(edge);
        //System.out.println(" edge gone!  "+ edge.getTo().myString());
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
                                 Guard currentEdgesGuard, Set<String> optOwn, boolean opt)
        throws CompilationException {
        String id = getNextEdgeId();

        AutomatonEdge ed =  addEdge(id, label, from, to, currentEdgesGuard, false, opt);
        ed.setOptionalOwners(optOwn);
        return ed;
    }
                                 /**
                                  * does not add duplicate  from- label to
                                  *
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
      Throwable t = new Throwable();
      t.printStackTrace();
      //System.out.println("id = "+id+" label "+label+ " to "+to);
      throw new CompilationException(getClass(),
        "Unable to add the specified edge as the source was null.\n" + this.toString() + "\n",
        this.getLocation());
    }

    if (to == null) {
      Throwable t = new Throwable();
      t.printStackTrace();
      throw new CompilationException(getClass(),
        "Unable to add the specified edge as the destination was null.\n" + this.toString() + "\n",
        this.getLocation());
    }
    // check that the nodes are part of this automaton
    if (!nodeMap.containsKey(from.getId())) {
      Throwable t = new Throwable();
      t.printStackTrace();
      throw new CompilationException(getClass(),
        "Unable to add the specified edge as " + from.getId() + " is not a part of "
          + this.getId() + " automaton. \n"
          + "Please make sure you aren't linking directly to a parallel composed process!",
        this.getLocation());
    }

    if (!nodeMap.containsKey(to.getId())) {
      Throwable t = new Throwable();
      t.printStackTrace();
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
    edge.addOwner(owner);
  }

  public void addOwnersToEdge(AutomatonEdge edge, Collection<String> owner)
    throws CompilationException {
    if (!edgeMap.containsValue(edge)) {
      throw new CompilationException(getClass(), "Cannot add an owner to an edge not in this automaton");
    }
    //System.out.println("owner "+owner+ " "+ owner.getClass().getName());
    //System.out.println("owners "+owners.getClass().getName());
    owners.addAll(owner);
    owner.forEach(edge::addOwner);
  }

  public void removeOwnerFromEdge(AutomatonEdge edge, String ownerToRemove) {
    if (ownerToRemove == null) {
      return;
    }
    edge.removeOwnerLocation(ownerToRemove);

    boolean shouldDelete = edgeMap.values().stream()
      .map(AutomatonEdge::getEdgeOwners)
      .flatMap(Set::stream)
      .noneMatch(ownerToRemove::equals);

    if (shouldDelete) {
      owners.remove(ownerToRemove);
    }
  }

    public void removeOptOwnerFromEdge(AutomatonEdge edge, String ownerToRemove) {
        if (ownerToRemove == null) {
            return;
        }
        edge.removeOptOwnerLocation(ownerToRemove);

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
    Set<String> out = this.getEdges().stream().map(x -> x.getLabel()).collect(Collectors.toSet());

    return out;
    //return alphabet.keySet();
  }

  public void setAlphabetFromEdges() {
    alphabet = new TreeMap<>();
    for (AutomatonEdge ed : getEdges()) {
      String label = ed.getLabel();
      if (alphabet.containsKey(label)) {
        alphabet.get(label).add(ed);
      }
      List<AutomatonEdge> neds = new ArrayList<>();
      neds.add(ed);
      alphabet.put(label, neds);
    }
  }

  public Set<String> getAlphabetFromEdges() {
    Set<String> alph = new TreeSet<>();
    for (AutomatonEdge ed : getEdges()) {
      String label = ed.getLabel();
      if (!alph.contains(label)) {
        alph.add(label);
      }
    }
    //System.out.println("alphabet in "+getId()+" is "+alph);
    return alph;
  }

  /**
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
      reNode.put(node, newNode);
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
        addEdge(edge.getLabel(), from, to, edge.getGuard(), edge.getOptionalOwners(), edge.getOptionalEdge()),
        edge.getEdgeOwners());
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

  public String myString() {
    StringBuilder sb = new StringBuilder();
    if (this.sequential) {
      sb.append("Aut " + this.getId() + " sequential root ");
    } else {
      sb.append("Aut " + this.getId() + " root ");
    }
    for (AutomatonNode r : root) {
      sb.append(r.getId() + " ");
    }
    sb.append(" end " + end);
    sb.append(" alpa " + this.alphabet.keySet() + " ");
    sb.append(" own " + owners + " nodes " + getNodes().size() + " edges " + getEdges().size() + "\n");
    for (AutomatonNode nd : getNodes()) {
      sb.append( nd.myString() + "   in " + nd.getIncomingEdges().size() +
        " out " + nd.getOutgoingEdges().size() + "\n");
    }
    for (AutomatonEdge ed : getEdges()) {
      sb.append(ed.myString() + "\n");
    }
    sb.append(" node2ReadySets " + node2ReadySets.size() + "\n");
    for (AutomatonNode nd : node2ReadySets.keySet()) {
      sb.append(nd.getId() + "->" + node2ReadySets.get(nd) + "\n");
    }

    sb.append("this.nodeId " + this.nodeId);

    return sb.toString();
  }

  public String readySets2String(boolean cong) {
    StringBuilder sb = new StringBuilder();
    sb.append("Ready set of " + this.getId() + "\n");
    for (AutomatonNode nd : getNodes()) {
      sb.append(nd.getId() + " -> " + nd.quiescentReadySet(cong) + "\n");
    }

    return sb.toString();
  }

  public String toString() {
    String tempto = "";
    String tempfrom = "";
    StringBuilder builder = new StringBuilder();
    builder.append("automaton(" + getId() + "):" + this.getId() + "{\n");
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

  public Automaton copy() {
    //System.out.println("Cloning Automata "+this.getId());
    Cloner cloner = new Cloner();
    return cloner.deepClone(this);

  }

  public Automaton reId(String tag) throws CompilationException {
    //System.out.println("Start reId "+this.myString());
    Automaton reIded = new Automaton(getId() + tag, !CONSTRUCT_ROOT);
    int newLabelNumber = 0;
    setId(getId() + tag);
//New node objects but with same Id causes problems where unique ids assumed!
    // compute bisim between two automata that nodes with same Id
  /*  copy.nodeId = 0;
    copy.edgeId = 0;
    copy.ownerId = 0; */
    //System.out.println("Start of reId "+ reIded.myString());
    //HACK fix the owners as some times global owners not same as owners on edges
    Set<String> union = new HashSet<>();
    for (AutomatonEdge ed : getEdges()) {
      union.addAll(ed.getEdgeOwners());
    }

    setOwners(union);
    Map<String, String> ownersMap = new TreeMap<>();

    //System.out.println("COPY "+ this.getId()+" owners "+ owners);
    for (String s : union) {
      ownerId++;
      //System.out.println("s "+s+" own "+ ownerId);
      ownersMap.putIfAbsent(s, "cp" + ((Integer) ownerId).toString());
      //System.out.println("ownersMap "+ s+"->"+ownersMap.get(s));
    }

    owners = ownersMap.values().stream().collect(Collectors.toSet());

    //System.out.println("XX " + ownersMap);

    Map<AutomatonNode, AutomatonNode> reNode = new HashMap<>();
    //System.out.println("Starting copy of "+this.toString());
    List<AutomatonNode> nodes = getNodes();
    for (AutomatonNode node : nodes) {
      AutomatonNode newNode = reIded.addNode();
      reNode.put(node, newNode);
      newNode.copyProperties(node);
      newNode.setLabelNumber(newLabelNumber++);
      //System.out.println("   reNode " + node.getId() + "->" + newNode.myString());
      if (newNode.isStartNode()) {
        reIded.addRoot(newNode);
      }
      if (newNode.isSTOP()) {
        reIded.addEnd(newNode.getId());
      }
    }
    //System.out.println("1 " + reIded.myString());
    List<AutomatonEdge> edges = getEdges();
    for (AutomatonEdge edge : edges) {
      //System.out.println(" edge "+edge.myString());
      AutomatonNode from = reNode.get(edge.getFrom());
      AutomatonNode to = reNode.get(edge.getTo());

      //Change to make copies re id the edge
        Set<String> newOpos = new HashSet<>();
        System.out.println("ownersMap.key "+ ownersMap.keySet());
        //os.stream().map(x->ownersMap.get(x)).collect(Collectors.toSet());
        for (String s : edge.getOptionalOwners()) {
            System.out.println("s= "+s);
            if (ownersMap.containsKey(s)) newOpos.add(ownersMap.get(s));
        }

      AutomatonEdge xedge = reIded.addEdge(edge.getLabel(),
        from, to, edge.getGuard(), newOpos, edge.getOptionalEdge());
      //System.out.println("  Added "+xedge.myString());
      Set<String> os = edge.getEdgeOwners();
      //System.out.print("os "+os);
      Set<String> newos = new HashSet<>();
      //os.stream().map(x->ownersMap.get(x)).collect(Collectors.toSet());
      for (String s : os) {
        newos.add(ownersMap.get(s));
      }
      //System.out.println(" newos "+ newos);
      reIded.addOwnersToEdge(xedge, newos);
      //System.out.println("End of adding Edge"+xedge.myString());
    }
    //System.out.println("2 "+ reIded.myString());

    reIded.copyProperties(this);
    //System.out.println("       reId Ends \n" + reIded.myString());
    return reIded;
  }


  public ProcessType getProcessType() {
    return ProcessType.AUTOMATA;
  }

  public AutomatonEdge square(AutomatonEdge e, AutomatonEdge ed) {
    if (!e.getFrom().getId().equals(ed.getFrom().getId())) {
      //System.out.println("\nSQUARE ERROR edges "+e.myString()+ " "+ed.myString()+"\n");
      return null;
    }
    for (AutomatonEdge par : e.getTo().getOutgoingEdges()) {
      if (!par.getLabel().equals(ed.getLabel())) continue;
      if (!par.getEdgeOwners().equals(ed.getEdgeOwners())) continue;
      for (AutomatonEdge par2 : ed.getTo().getOutgoingEdges()) {
        if (!par2.getLabel().equals(e.getLabel())) continue;
        if (!par2.getEdgeOwners().equals(e.getEdgeOwners())) continue;
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
    for (AutomatonEdge e : edges) {
      out = out + e.myString() + " ** ";
    }
    return out + "}";
  }

  public void tagEvents() {
    for (AutomatonEdge ed : getEdges()) {
      ed.setLabel(ed.getLabel() + ":" + tagid++);
      //System.out.println("tag "+ed.myString());
    }
  }

  /**
   * @param ed
   * @return the edges parallel to ed
   */
  public Set<AutomatonEdge> parallelSet(AutomatonEdge ed) {
    Set<String> edOwn = ed.getEdgeOwners();
    //AutomatonEdge ed
    Set<AutomatonNode> visited = new HashSet<>();
    Set<AutomatonEdge> parallel = new HashSet<>();
    Stack<AutomatonNode> fringe = new Stack<>();
    Map<AutomatonNode, AutomatonEdge> selectPar = new HashMap<>();

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
        for (String own : edge.getEdgeOwners()) {
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
    for (AutomatonNode nd : node2ReadySets.keySet()) {
      sb.append(nd.getId() + "-> " + node2ReadySets.get(nd) + "\n");
    }
    return sb.toString();
  }
}


