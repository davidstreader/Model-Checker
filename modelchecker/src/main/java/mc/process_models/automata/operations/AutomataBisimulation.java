package mc.process_models.automata.operations;

import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;

import java.util.*;

public class AutomataBisimulation {

    private static final int BASE_COLOUR = 1;
    private static final int STOP_COLOUR = 0;
    private static final int ERROR_COLOUR = -1;

    private int nextColourId;

    public Automaton performSimplification(Automaton automaton) throws CompilationException {
        reset();

        Map<Integer, List<Colour>> colourMap = new HashMap<Integer, List<Colour>>();
        Map<Integer, List<AutomatonNode>> nodeColours = performColouring(automaton, colourMap);
        List<AutomatonNode> merged = new ArrayList<>();
        // merge nodes that have the same colouring
        for(int colourId : nodeColours.keySet()){
            List<AutomatonNode> nodes = nodeColours.get(colourId);
            // no need to combine nodes if there is only one
            if(nodes.size() < 2){
                continue;
            }

            // merge the nodes to form one node
            AutomatonNode mergedNode = nodes.get(0);
            for(int i = 1; i < nodes.size(); i++){
                mergedNode = automaton.combineNodes(mergedNode, nodes.get(i));
            }
            merged.add(mergedNode);
            // remove the nodes that were merged
            nodes.forEach(automaton::removeNode);
        }
        //The above code leaves out the last merge with the next node, so perform that now.
        while (!merged.isEmpty()) {
            AutomatonNode node = merged.remove(0);
            String firstAlpha = node.getOutgoingEdges().get(0).getLabel();
            //If all outgoing transitions are not the same, do not merge.
            if (node.getOutgoingEdges().stream().anyMatch(s -> !Objects.equals(s.getLabel(), firstAlpha))) continue;
            for (AutomatonEdge edge : node.getOutgoingEdges()) {
                if (edge.getTo() != node) {
                    if (automaton.getNodes().contains(edge.getTo())) {
                        AutomatonNode node2 = automaton.combineNodes(node, edge.getTo());
                        if (merged.contains(edge.getTo())) {
                            merged.remove(edge.getTo());
                            merged.add(node2);
                        }
                        break;
                    }
                }

            }
        }
        return automaton;
    }

    public boolean areBisimular(List<Automaton> automata){
        reset();

        Map<Integer, List<Colour>> colourMap = new HashMap<Integer, List<Colour>>();
        int lastColourMapSize;
        int rootColour = Integer.MIN_VALUE;
        for(Automaton automaton : automata){
            lastColourMapSize = colourMap.size();
            performColouring(automaton, colourMap);
            AutomatonNode root = automaton.getRoot();
            int colour = (int)root.getMetaData("colour");
            if(rootColour == Integer.MIN_VALUE){
                rootColour = colour;
            }
            //If we have added a colouring, then the automatons are not bisimular.
            else if(lastColourMapSize != colourMap.size()){
                return false;
            }
        }

        return true;
    }

    private Map<Integer, List<AutomatonNode>> performColouring(Automaton automaton, Map<Integer, List<Colour>> colourMap){
        int lastColourCount = 0;
        perfromInitialColouring(automaton);
        Map<Integer, List<AutomatonNode>> nodeColours;

        while(true){
            nodeColours = new HashMap<>();
            Set<String> visited = new HashSet<>();

            Queue<AutomatonNode> fringe = new LinkedList<>();
            fringe.offer(automaton.getRoot());

            while(!fringe.isEmpty()){
                AutomatonNode current = fringe.poll();

                // check if the current node has been visited
                if(visited.contains(current.getId())){
                    continue;
                }
                // check if the current node is a terminal
                if(current.hasMetaData("isTerminal")){
                    String terminal = (String)current.getMetaData("isTerminal");
                    if(terminal.equals("STOP")){
                        if(!nodeColours.containsKey(STOP_COLOUR)){
                            nodeColours.put(STOP_COLOUR, new ArrayList<>());
                        }
                        nodeColours.get(STOP_COLOUR).add(current);
                        //Store STOP in the colourMap so that we can use it to check if bisimular.
                        colourMap.putIfAbsent(STOP_COLOUR,new ArrayList<>());
                    }
                    else if(terminal.equals("ERROR")){
                        if(!nodeColours.containsKey(ERROR_COLOUR)){
                            nodeColours.put(ERROR_COLOUR, new ArrayList<>());
                        }
                        nodeColours.get(ERROR_COLOUR).add(current);
                        //Store ERROR in the colourMap so that we can use it to check if bisimular.
                        colourMap.putIfAbsent(ERROR_COLOUR,new ArrayList<>());
                    }
                    visited.add(current.getId());
                    continue;
                }
                // construct a colouring for the current node
                List<Colour> colouring = constructColouring(current);

                // check if this colouring already exists
                int colourId = Integer.MIN_VALUE;

                for(int id : colourMap.keySet()){
                    List<Colour> oldColouring = colourMap.get(id);
                    if(colouring.equals(oldColouring)){
                        colourId = id;
                        break;
                    }
                }

                if(colourId == Integer.MIN_VALUE){
                    colourId = getNextColourId();
                    colourMap.put(colourId, colouring);
                }

                if(!nodeColours.containsKey(colourId)){
                    nodeColours.put(colourId, new ArrayList<>());
                }
                nodeColours.get(colourId).add(current);

                current.getOutgoingEdges().stream()
                    .map(AutomatonEdge::getTo)
                    .filter(node -> !visited.contains(node.getId()))
                    .forEach(fringe::offer);

                visited.add(current.getId());
            }
            if (lastColourCount == colourMap.size()) {
                break;
            }
            lastColourCount = colourMap.size();
            // apply colours to the nodes
            for(int colourId : nodeColours.keySet()){
                nodeColours.get(colourId).forEach(node -> node.addMetaData("colour", colourId));
            }
        }

        return nodeColours;
    }

    private void perfromInitialColouring(Automaton automaton){
        List<AutomatonNode> nodes = automaton.getNodes();
        for(AutomatonNode node : nodes){
            node.addMetaData("colour", BASE_COLOUR);
        }
    }


    private List<Colour> constructColouring(AutomatonNode node){
        Set<Colour> colouringSet = new HashSet<>();

        int from = (int)node.getMetaData("colour");
        node.getOutgoingEdges()
            .forEach(edge -> colouringSet.add(new Colour(from, edge.getLabel())));
        List<Colour> colouring = new ArrayList<>(colouringSet);
        Collections.sort(colouring);
        return colouring;
    }

    private int getNextColourId(){
        return nextColourId++;
    }

    private void reset(){
        nextColourId = 1;
    }
    private class Colour implements Comparable<Colour> {

        public int from;
        public String action;

        Colour(int from, String action){
            this.from = from;
            this.action = action;
        }

        public boolean equals(Object obj){
            if(obj == this){
                return true;
            }

            if(obj instanceof Colour){
                Colour col = (Colour)obj;
                return action.equals(col.action);
            }

            return false;
        }

        @Override
        public int hashCode() {
            int result = from;
            result = 31 * result + action.hashCode();
            return result;
        }

        public int compareTo(Colour col){
            if(from < col.from){
                return -1;
            }

            if(from > col.from){
                return 1;
            }
            return action.compareTo(col.action);
        }

    }
}
