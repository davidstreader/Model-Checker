package mc.process_models.automata;

import com.rits.cloning.Cloner;
import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.ProcessModelObject;
import mc.process_models.automata.serializers.EdgeClone;
import mc.util.Location;

import java.util.*;
import java.util.stream.Collectors;

public class Automaton extends ProcessModelObject implements ProcessModel {

    public static final boolean CONSTRUCT_ROOT = true;

    private AutomatonNode root;
    private Map<String, AutomatonNode> nodeMap;
    private Map<String, AutomatonEdge> edgeMap;
    private Map<String, List<AutomatonEdge>> alphabet;

    private int nodeId;
    private int edgeId;

    public Automaton(String id) {
        super(id, "automata");
        setupAutomaton();

        // setup the root for this automaton
        this.root = addNode();
        root.addMetaData("startNode", true);
    }

    public Automaton(String id, boolean constructRoot) {
        super(id, "automata");
        setupAutomaton();

        // only construct a root node if specified to do so
        if (constructRoot) {
            this.root = addNode();
            root.addMetaData("startNode", true);
        }
    }

    private void setupAutomaton() {
        this.nodeMap = new HashMap<String, AutomatonNode>();
        this.edgeMap = new HashMap<String, AutomatonEdge>();
        this.alphabet = new HashMap<String, List<AutomatonEdge>>();

        this.nodeId = 0;
        this.edgeId = 0;
    }

    public AutomatonNode getRoot() {
        return root;
    }

    public void setRoot(AutomatonNode root) throws CompilationException {
        // check the the new root is defined
        if (root == null) {
            throw new CompilationException(getClass(),"Unable to set the root node to null",(Location)getMetaData().get("location"));
        }

        // check that the new root is part of this automaton
        if (!nodeMap.containsKey(root.getId())) {
            throw new CompilationException(getClass(),"Unable to set the root node to "+root.getId()+", as the root is not a part of this automaton",(Location)getMetaData().get("location"));
        }

        this.root = root;
    }

    public String getRootId() {
        return root.getId();
    }

    public List<AutomatonNode> getNodes() {
        return nodeMap.entrySet().stream()
            .map(x -> x.getValue())
            .collect(Collectors.toList());
    }

    public AutomatonNode getNode(String id) throws CompilationException {
        if (nodeMap.containsKey(id)) {
            return nodeMap.get(id);
        }

        throw new CompilationException(getClass(),"Unable to get the node "+id+" as it does not exist.",(Location)getMetaData().get("location"));
    }

    public AutomatonNode addNode() {
        String id = getNextNodeId();
        return addNode(id);
    }

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
            .map(edge -> edge.getId())
            .forEach(id -> edgeMap.remove(id));
        nodeMap.remove(node.getId());
        return true;
    }

    public AutomatonNode combineNodes(AutomatonNode node1, AutomatonNode node2) throws CompilationException {
        if (!nodeMap.containsKey(node1.getId())) {
            throw new CompilationException(getClass(),node1.getId()+" was not found in the automaton "+getId(),(Location)getMetaData().get("location"));
        }

        if (!nodeMap.containsKey(node2.getId())) {
            throw new CompilationException(getClass(),node2.getId()+" was not found in the automaton "+getId(),(Location)getMetaData().get("location"));
        }

        AutomatonNode node = addNode();

        // add the incoming and outgoing edges from both nodes to the combined nodes
        processIncomingEdges(node, node1);
        processIncomingEdges(node, node2);
        processOutgoingEdges(node, node1);
        processOutgoingEdges(node, node2);

        // create an intersection of the metadata from both nodes
        for (String key : node1.getMetaDataKeys()) {
            Object data = node1.getMetaData(key);

            // check if the second node has a matching metadata key
            if (node2.hasMetaData(key)) {
                // check that the metadata is equivalent
                if (data.equals(node2.getMetaData(key))) {
                    // add metadata to the composed node
                    node.addMetaData(key, data);

                    if (key.equals("startNode")) {
                        setRoot(node);
                    }
                }
            }
        }
        if (node1.hasMetaData("startNode") || node2.hasMetaData("startNode")) {
            setRoot(node);
            node.addMetaData("startNode",true);
        }
        removeNode(node1);
        removeNode(node2);
        return node;
    }

    private void processIncomingEdges(AutomatonNode node, AutomatonNode oldNode) {
        List<AutomatonEdge> edges = oldNode.getIncomingEdges();
        for (int i = 0; i < edges.size(); i++) {
            AutomatonEdge edge = edges.get(i);
            node.addIncomingEdge(edge);
            edge.setTo(node);
            oldNode.removeIncomingEdge(edge);
        }
    }

    private void processOutgoingEdges(AutomatonNode node, AutomatonNode oldNode) {
        List<AutomatonEdge> edges = oldNode.getOutgoingEdges();
        for (int i = 0; i < edges.size(); i++) {
            AutomatonEdge edge = edges.get(i);
            node.addOutgoingEdge(edge);
            edge.setFrom(node);
            oldNode.removeOutgoingEdge(edge);
        }
    }

    public int getNodeCount() {
        return nodeMap.size();
    }

    public List<AutomatonEdge> getEdges() {
        return edgeMap.entrySet().stream()
            .map(x -> x.getValue())
            .collect(Collectors.toList());
    }

    public AutomatonEdge getEdge(String id) throws CompilationException {
        if (edgeMap.containsKey(id)) {
            return edgeMap.get(id);
        }

        throw new CompilationException(getClass(),"Edge "+id+" was not found in the automaton "+getId(),(Location)getMetaData().get("location"));
    }

    public AutomatonEdge addEdge(String label, AutomatonNode from, AutomatonNode to) throws CompilationException {
        String id = getNextEdgeId();
        return addEdge(id, label, from, to);
    }

    public AutomatonEdge addEdge(String id, String label, AutomatonNode from, AutomatonNode to) throws CompilationException {
        // check that the nodes have been defined
        if (from == null) {
            throw new CompilationException(getClass(),"Unable to add the specified edge as the source was null.",(Location)getMetaData().get("location"));
        }

        if (to == null) {
            throw new CompilationException(getClass(),"Unable to add the specified edge as the destination was null.",(Location)getMetaData().get("location"));
        }

        // check that the nodes are part of this automaton
        if (!nodeMap.containsKey(from.getId())) {
            throw new CompilationException(getClass(),"Unable to add the specified edge as "+from.getId()+" is not a part of this automaton",(Location)getMetaData().get("location"));
        }

        if (!nodeMap.containsKey(to.getId())) {
            throw new CompilationException(getClass(),"Unable to add the specified edge as "+to.getId()+" is not a part of this automaton",(Location)getMetaData().get("location"));
        }

        // check if there is already an identical edge between the specified nodes
        List<AutomatonEdge> edges = from.getOutgoingEdges().stream()
            .filter(edge -> edge.getLabel().equals(label) && edge.getTo().getId().equals(to.getId()))
            .collect(Collectors.toList());

        if(edges.size() > 0){
            return edges.get(0);
        }

        AutomatonEdge edge = new AutomatonEdge(id, label, from, to);

        // add edge reference to the incoming and outgoing nodes
        from.addOutgoingEdge(edge);
        to.addIncomingEdge(edge);

        // add edge to the edge and alphabet maps
        if (!alphabet.containsKey(label)) {
            alphabet.put(label, new ArrayList<AutomatonEdge>());
        }
        alphabet.get(label).add(edge);
        edgeMap.put(id, edge);

        return edge;
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

    public AutomatonNode addAutomaton(Automaton automaton) throws CompilationException {
        AutomatonNode root = null;
        for(AutomatonNode node : automaton.getNodes()){
            AutomatonNode newNode = addNode(node.getId());
            for(String key : node.getMetaDataKeys()){
                if(key.equals("startNode")){
                    root = newNode;
                    if(this.root != null){
                        continue;
                    }

                    this.root = newNode;
                }

                newNode.addMetaData(key, node.getMetaData(key));
            }
        }

        for (AutomatonEdge edge : automaton.getEdges()) {
            AutomatonNode from = getNode(edge.getFrom().getId());
            AutomatonNode to = getNode(edge.getTo().getId());
            AutomatonEdge newEdge = addEdge(edge.getId(), edge.getLabel(), from, to);
            for (String key : edge.getMetaDataKeys()) {
                newEdge.addMetaData(key, edge.getMetaData(key));
            }
        }
        if (root == null) {
            throw new CompilationException(getClass(),"There was no root found while trying to add an automaton");
        }
        return root;
    }


    public String getNextNodeId() {
        return getId() + ".n" + nodeId++;
    }

    public String getNextEdgeId() {
        return getId() + ".e" + edgeId++;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("automaton:{\n");
        builder.append("\tnodes:{\n");
        for (AutomatonNode node : nodeMap.values()) {
            builder.append("\t\t" + node.getId());
            if (node == root) {
                builder.append("(root)");
            }
            builder.append("\n");
        }
        builder.append("\t}\n\tedges:{\n");
        for (AutomatonEdge edge : edgeMap.values()) {
            builder.append("\t\t" + edge.getFrom().getId() + " -" + edge.getLabel() + "> " + edge.getTo().getId() + "\n");
        }
        builder.append("\t}\n}");

        return builder.toString();
    }

    public Automaton copy() throws CompilationException {
        Cloner cloner = new Cloner();
        //Cloning while all the edges and nodes point to each other is asking for trouble.
        //We can just keep a list of all cloned edges and add the data in after the cloning is done.
        IdentityHashMap<AutomatonEdge,EdgeClone> edgeMap = new IdentityHashMap<>();
        cloner.registerFastCloner(AutomatonEdge.class, (t, cloner1, clones) -> {
            AutomatonEdge edge = (AutomatonEdge)t;
            //Create a new edge that does not reference the nodes.
            AutomatonEdge newEdge = new AutomatonEdge(edge.getId(),edge.getLabel(),null,null);
            //Keep track of where this edge should point
            edgeMap.put(newEdge, new EdgeClone(edge));
            return newEdge;
        });
        //Deep clone this automata, using the above method to clone edges.
        Automaton ret = cloner.deepClone(this);
        //Correct all the edge clones so they point to the right place.
        for (AutomatonEdge edge: ret.getEdges()) {
            edgeMap.get(edge).apply(edge,ret);
        }
        return ret;
    }
}
