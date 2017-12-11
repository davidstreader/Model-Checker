package mc.operations;

import com.microsoft.z3.Context;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.plugins.IProcessFunction;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class NFAtoDFAFunction implements IProcessFunction{
    /**
     * Gets the method name when it is called (e.g. {@code abs} in {@code abs(A)})
     *
     * @return the name of the function
     */
    @Override
    public String getFunctionName() {
        return "nfa2dfa";
    }

    /**
     * Get the available flags for the function described by this interface (e.g. {@code unfair} in
     * {@code abs{unfair}(A)}
     *
     * @return a collection of available flags (note, no variables may be flags)
     */
    @Override
    public Collection<String> getValidFlags() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Gets the number of automata to parse into the function
     *
     * @return the number of arguments
     */
    @Override
    public int getNumberArguments() {
        return 1;
    }

    /**
     * Execute the function on automata
     *
     * @param id       the id of the resulting automaton
     * @param flags    the flags given by the function (e.g. {@code unfair} in {@code abs{unfair}(A)}
     * @param context
     * @param automata a variable number of automata taken in by the function
     * @return the resulting automaton of the operation
     * @throws CompilationException when the function fails
     */
    @Override
    public Automaton compose(String id, String[] flags, Context context, Automaton... automata) throws CompilationException {
        assert automata.length == 1;
        Automaton nfa = automata[0].copy();
        Automaton dfa = new Automaton(id, !Automaton.CONSTRUCT_ROOT);

        Map<Set<String>, List<AutomatonNode>> stateMap = new HashMap<>();
        Map<String, AutomatonNode> nodeMap = new HashMap<>();
        Set<String> visited = new HashSet<>();

        Stack<Set<String>> fringe = new Stack<>();
        fringe.push(constructClosure(nfa.getRoot(), stateMap));

        Set<String> alphabet = nfa.getAlphabet();
        alphabet.remove(Constant.HIDDEN);

        boolean processedRoot = false;
        while(!fringe.isEmpty()){
            Set<String> states = fringe.pop();
            String idNode = constructNodeId(stateMap.get(states), nfa.getId());

            if(visited.contains(idNode)){
                continue;
            }
            if(!nodeMap.containsKey(idNode)){
                nodeMap.put(idNode, dfa.addNode(idNode));
            }
            AutomatonNode node = nodeMap.get(idNode);

            if(!processedRoot){
                dfa.setRoot(node);
                node.setStartNode(true);
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
                }
                AutomatonNode nextNode = nodeMap.get(nextId);

                dfa.addEdge(action, node, nextNode, null);

                fringe.push(nextStates);
            }

            visited.add(idNode);
        }

        dfa.getNodes().stream()
                .filter(node -> node.getOutgoingEdges().isEmpty())
                .forEach(node -> node.setTerminal("STOP"));
        return dfa;
    }

    private Set<String> constructClosure(AutomatonNode node, Map<Set<String>, List<AutomatonNode>> stateMap){
        Set<String> states = new HashSet<>();
        List<AutomatonNode> nodes = new ArrayList<>();

        Stack<AutomatonNode> fringe = new Stack<>();
        fringe.push(node);

        while(!fringe.isEmpty()){
            AutomatonNode current = fringe.pop();

            if(states.contains(current.getId())){
                continue;
            }

            states.add(current.getId());
            nodes.add(current);

            List<AutomatonEdge> edges = current.getOutgoingEdges().stream()
                    .filter(AutomatonEdge::isHidden)
                    .collect(Collectors.toList());

            edges.forEach(edge -> fringe.push(edge.getTo()));
        }

        if(!stateMap.containsKey(states)){
            stateMap.put(states, nodes);
        }

        return states;
    }

    private Set<String> constructStateSet(List<AutomatonNode> nodes, String action, Map<Set<String>, List<AutomatonNode>> stateMap){
        Set<String> states = new HashSet<>();
        List<AutomatonNode> nextNodes = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        Stack<AutomatonNode> fringe = new Stack<>();
        nodes.forEach(fringe::push);

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
        return identifier + constructLabel(nodes);
    }

    private String constructLabel(List<AutomatonNode> nodes){
        Set<String> labelSet = new HashSet<>();
        for(AutomatonNode node : nodes)
            labelSet.add(Integer.toString(node.getLabelNumber()));

        List<String> labels = new ArrayList<>(labelSet);
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
