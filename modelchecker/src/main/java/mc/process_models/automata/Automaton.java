package mc.process_models.automata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import mc.process_models.ProcessModel;
import mc.process_models.ProcessModelObject;

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

    public void setRoot(AutomatonNode root) {
        // check the the new root is defined
        if (root == null) {
            // TODO: throw error
        }

        // check that the new root is part of this automaton
        if (!nodeMap.containsKey(root.getId())) {
            // TODO: throw error
        }

        this.root = root;
    }

    public List<AutomatonNode> getNodes() {
        return nodeMap.entrySet().stream()
                .map(x -> x.getValue())
                .collect(Collectors.toList());
    }

    public AutomatonNode getNode(String id) {
        if (nodeMap.containsKey(id)) {
            return nodeMap.get(id);
        }

        // TODO: throw error
        return null;
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

    public AutomatonNode combineNodes(AutomatonNode node1, AutomatonNode node2) {
        if (!nodeMap.containsKey(node1.getId())) {
            // TODO: throw error
        }

        if (!nodeMap.containsKey(node2.getId())) {
            // TODO: throw error
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

    public AutomatonEdge getEdge(String id) {
        if (edgeMap.containsKey(id)) {
            return edgeMap.get(id);
        }

        // TODO: throw error
        return null;
    }

    public AutomatonEdge addEdge(String label, AutomatonNode from, AutomatonNode to) {
        String id = getNextEdgeId();
        return addEdge(id, label, from, to);
    }

    public AutomatonEdge addEdge(String id, String label, AutomatonNode from, AutomatonNode to) {
        // check that the nodes have been defined
        if (from == null) {
            // TODO: throw error
        }

        if (to == null) {
            // TODO: throw error
        }

        // check that the nodes are part of this automaton
        if (!nodeMap.containsKey(from.getId())) {
            // TODO: throw error
        }

        if (!nodeMap.containsKey(to.getId())) {
            // TODO: throw error
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

    public AutomatonNode addAutomaton(Automaton automaton) {
        AutomatonNode root = null;
        for (AutomatonNode node : automaton.getNodes()) {
            AutomatonNode newNode = addNode(node.getId());
            for (String key : node.getMetaDataKeys()) {
                newNode.addMetaData(key, node.getMetaData(key));
                if (key.equals("startNode")) {
                    root = newNode;
                }
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

    public String getRootId() {
        return getRoot().getId();
    }
}
