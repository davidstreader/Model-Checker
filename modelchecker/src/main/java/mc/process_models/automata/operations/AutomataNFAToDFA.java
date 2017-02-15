package mc.process_models.automata.operations;

import mc.Constant;
import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class AutomataNFAToDFA {

    public Automaton performNFAToDFA(Automaton nfa) throws CompilationException {
        Automaton dfa = new Automaton(nfa.getId(), !Automaton.CONSTRUCT_ROOT);

        Map<Set<String>, List<AutomatonNode>> stateMap = new HashMap<Set<String>, List<AutomatonNode>>();
        Map<String, AutomatonNode> nodeMap = new HashMap<String, AutomatonNode>();
        Set<String> visited = new HashSet<String>();

        Stack<Set<String>> fringe = new Stack<Set<String>>();
        fringe.push(constructClosure(nfa.getRoot(), stateMap));

        Set<String> alphabet = nfa.getAlphabet();
        alphabet.remove(Constant.HIDDEN);

        boolean processedRoot = false;
        while(!fringe.isEmpty()){
            Set<String> states = fringe.pop();
            String id = constructNodeId(stateMap.get(states), nfa.getId());

            if(visited.contains(id)){
                continue;
            }

            if(!nodeMap.containsKey(id)){
                nodeMap.put(id, dfa.addNode(id));
                AutomatonNode node = nodeMap.get(id);
                node.addMetaData("label", constructLabel(stateMap.get(states)));
                node.addMetaData("dfa", true);
            }
            AutomatonNode node = nodeMap.get(id);

            if(!processedRoot){
                dfa.setRoot(node);
                node.addMetaData("startNode", true);
                processedRoot = true;
            }

            for(String action : alphabet){
                Set<String> nextStates = constructStateSet(stateMap.get(states), action, stateMap);

                if(nextStates.isEmpty()){
                    continue;
                }

                String nextId = constructNodeId(stateMap.get(nextStates), nfa.getId());

                if(!nodeMap.containsKey(nextId)){
                    nodeMap.put(nextId, dfa.addNode(nextId));
                    AutomatonNode nextNode = nodeMap.get(nextId);
                    nextNode.addMetaData("label", constructLabel(stateMap.get(nextStates)));
                    nextNode.addMetaData("dfa", true);
                }
                AutomatonNode nextNode = nodeMap.get(nextId);

                dfa.addEdge(action, node, nextNode);

                fringe.push(nextStates);
            }

            visited.add(id);
        }

        return dfa;
    }

    private Set<String> constructClosure(AutomatonNode node, Map<Set<String>, List<AutomatonNode>> stateMap){
        Set<String> states = new HashSet<String>();
        List<AutomatonNode> nodes = new ArrayList<AutomatonNode>();

        Stack<AutomatonNode> fringe = new Stack<AutomatonNode>();
        fringe.push(node);

        while(!fringe.isEmpty()){
            AutomatonNode current = fringe.pop();

            if(states.contains(current.getId())){
                continue;
            }

            states.add(current.getId());
            nodes.add(current);

            List<AutomatonEdge> edges = current.getOutgoingEdges().stream()
                    .filter(edge -> edge.isHidden())
                    .collect(Collectors.toList());

            edges.forEach(edge -> fringe.push(edge.getTo()));
        }

        if(!stateMap.containsKey(states)){
            stateMap.put(states, nodes);
        }

        return states;
    }

    private Set<String> constructStateSet(List<AutomatonNode> nodes, String action, Map<Set<String>, List<AutomatonNode>> stateMap){
        Set<String> states = new HashSet<String>();
        List<AutomatonNode> nextNodes = new ArrayList<AutomatonNode>();
        Set<String> visited = new HashSet<String>();

        Stack<AutomatonNode> fringe = new Stack<AutomatonNode>();
        nodes.forEach(node -> fringe.push(node));

        while(!fringe.isEmpty()){
            AutomatonNode current = fringe.pop();

            if(visited.contains(current.getId())){
                continue;
            }

            List<AutomatonEdge> edges = current.getOutgoingEdges();
            for(AutomatonEdge edge : edges){
                if(action.equals(edge.getLabel())){
                    states.add(edge.getTo().getId());
                    nextNodes.add(edge.getTo());
                }
                else if(edge.getLabel().equals(Constant.HIDDEN)){
                    fringe.push(edge.getTo());
                }
            }

            visited.add(current.getId());
        }

        if(!stateMap.containsKey(states)){
            stateMap.put(states, nextNodes);
        }

        return states;
    }

    private String constructNodeId(List<AutomatonNode> nodes, String identifier){
        StringBuilder builder = new StringBuilder();
        builder.append(identifier);
        builder.append(constructLabel(nodes));

        return builder.toString();
    }

    private String constructLabel(List<AutomatonNode> nodes){
        Set<String> labelSet = new HashSet<String>();
        for(AutomatonNode node : nodes){
            Object label = node.getMetaData("label");
            if(label instanceof Integer){
                labelSet.add(Integer.toString((Integer)label));
            }
            else if(label instanceof String){
                labelSet.add((String)label);
            }
        }

        List<String> labels = new ArrayList<String>(labelSet);
        Collections.sort(labels);

        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for(int i = 0; i < labels.size(); i++){
            builder.append(labels.get(i));
            if(i < labels.size() - 1){
                builder.append(",");
            }
        }
        builder.append("}");

        return builder.toString();
    }
}