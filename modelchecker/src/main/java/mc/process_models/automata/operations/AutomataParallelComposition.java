package mc.process_models.automata.operations;

import mc.Constant;
import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class AutomataParallelComposition {

    private Automaton automaton;
    private Map<String, List<AutomatonNode>> nodeMap;
    private Set<String> syncedActions;
    private Set<String> unsyncedActions;

    public Automaton performParallelComposition(String id, Automaton automaton1, Automaton automaton2) throws CompilationException {
        setup(id);

        // construct the parallel composition of the states from both automata
        List<AutomatonNode> nodes1 = automaton1.getNodes();
        List<AutomatonNode> nodes2 = automaton2.getNodes();
        setupNodes(nodes1, nodes2);

        // find the synchronous and non-synchronous actions in both alphabet sets
        Set<String> alphabet1 = automaton1.getAlphabet();
        Set<String> alphabet2 = automaton2.getAlphabet();
        setupActions(alphabet1, alphabet2);

        List<AutomatonEdge> edges1 = automaton1.getEdges();
        List<AutomatonEdge> edges2 = automaton2.getEdges();

        processUnsyncedActions(edges1, edges2);
        processSyncedActions(edges1, edges2);

        return automaton;
    }

    private void setupNodes(List<AutomatonNode> nodes1, List<AutomatonNode> nodes2) throws CompilationException {
        for(AutomatonNode node1 : nodes1){
            nodeMap.put(node1.getId(), new ArrayList<AutomatonNode>());

            for(AutomatonNode node2 : nodes2){
                if(!nodeMap.containsKey(node2.getId())){
                    nodeMap.put(node2.getId(), new ArrayList<AutomatonNode>());
                }

                String id = createId(node1, node2);
                AutomatonNode node = automaton.addNode(id);

                // create an intersection of the metadata from both nodes
                for(String key : node1.getMetaDataKeys()){
                    Object data = node1.getMetaData(key);

                    // check if the second node has a matching metadata key
                    if(node2.hasMetaData(key)){
                        // check that the metadata is equivalent
                        if(data.equals(node2.getMetaData(key))){
                            // add metadata to the composed node
                            node.addMetaData(key, data);

                            if(key.equals("startNode")){
                                automaton.setRoot(node);
                            }
                        }
                    }
                }

                nodeMap.get(node1.getId()).add(node);
                nodeMap.get(node2.getId()).add(node);
            }
        }
    }

    private void setupActions(Set<String> alphabet1, Set<String> alphabet2){
        for(String action : alphabet1){
            processAction(action, alphabet2);
        }
        for(String action : alphabet2){
            processAction(action, alphabet1);
        }
    }

    private void processAction(String action, Set<String> alphabet){
        // if action is hidden or deadlocked it is always unsynced
        if(action.equals(Constant.HIDDEN) || action.equals(Constant.DEADLOCK)){
            unsyncedActions.add(action);
        }
        // broadcasting actions are always unsynced
        else if(action.endsWith("!")){
            if(containsReceiver(action, alphabet)){
                syncedActions.add(action);
            }

            if(containsBroadcaster(action, alphabet)){
                syncedActions.add(action);
            }
            else {
                unsyncedActions.add(action);
            }
        }
        else if(action.endsWith("?")){
            if(containsReceiver(action, alphabet)) {
                syncedActions.add(action);
            }
        }
        else if(alphabet.contains(action)){
            syncedActions.add(action);
        }
        else{
            unsyncedActions.add(action);
        }
    }

    private void processUnsyncedActions(List<AutomatonEdge> edges1, List<AutomatonEdge> edges2) throws CompilationException {
        List<AutomatonEdge> allEdges = new ArrayList<>(edges1);
        allEdges.addAll(edges2);

        for(String action : unsyncedActions){
            List<AutomatonEdge> edges = allEdges.stream()
                .filter(edge -> action.equals(edge.getLabel()) && !edge.hasMetaData("isReceiver")) // receivers never get executed
                .collect(Collectors.toList());

            for(AutomatonEdge edge : edges){
                List<AutomatonNode> from = nodeMap.get(edge.getFrom().getId());
                List<AutomatonNode> to = nodeMap.get(edge.getTo().getId());

                for(int i = 0; i < from.size(); i++){
                    automaton.addEdge(edge.getLabel(), from.get(i), to.get(i));
                }
            }
        }
    }

    private void processSyncedActions(List<AutomatonEdge> edges1, List<AutomatonEdge> edges2) throws CompilationException {
        for(String action : syncedActions){
            List<AutomatonEdge> syncedEdges1 = edges1.stream()
                    .filter(edge -> equals(action, edge.getLabel()))
                    .collect(Collectors.toList());
            List<AutomatonEdge> syncedEdges2 = edges2.stream()
                    .filter(edge -> equals(action, edge.getLabel()))
                    .collect(Collectors.toList());

            for(AutomatonEdge edge1 : syncedEdges1){
                for(AutomatonEdge edge2 : syncedEdges2){
                    AutomatonNode from = automaton.getNode(createId(edge1.getFrom(), edge2.getFrom()));

                    // any edges from the from node are broadcasted and should get replaced by the synced transition
                    from.getOutgoingEdges().stream().forEach(edge -> automaton.removeEdge(edge.getId()));

                    AutomatonNode to = automaton.getNode(createId(edge1.getTo(), edge2.getTo()));
                    automaton.addEdge(action, from, to);
                }
            }
        }
    }

    private String createId(AutomatonNode node1, AutomatonNode node2){
        return node1.getId() + "||" + node2.getId();
    }

    private boolean containsReceiver(String broadcaster, Set<String> receivers){
        String broadcastAction = broadcaster.substring(0, broadcaster.length() - 1);
        for(String receiver : receivers){
            if(receiver.endsWith("?")){
                String action = receiver.substring(0, receiver.length() - 1);
                if(action.equals(broadcastAction)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean containsBroadcaster(String broadcaster, Set<String> receivers){
        String broadcastAction = broadcaster.substring(0, broadcaster.length() - 1);
        for(String receiver : receivers){
            if(receiver.endsWith("!")){
                String action = receiver.substring(0, receiver.length() - 1);
                if(action.equals(broadcastAction)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean equals(String action1, String action2){
        if(action1.equals(action2)){
            return true;
        }
        else if(action1.endsWith("!")){
            action1 = action1.substring(0, action1.length() - 1);
            action2 = action2.substring(0, action2.length() - 1);
            return action1.equals(action2);
        }

        return false;
    }

    private void setup(String id){
        this.automaton = new Automaton(id, !Automaton.CONSTRUCT_ROOT);
        this.nodeMap = new HashMap<String, List<AutomatonNode>>();
        this.syncedActions = new HashSet<String>();
        this.unsyncedActions = new HashSet<String>();
    }
}
