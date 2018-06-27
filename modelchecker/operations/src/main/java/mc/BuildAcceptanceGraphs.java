package mc;

import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

//public class BuildAcceptanceGraphs implements IProcessFunction {
public class BuildAcceptanceGraphs  {





    /**
     * MOVED TO AcceptanceGraph object
     *
     * @param id       the id of the resulting automaton
     * @param nfa  an automata taken in by the function
     * @return acceptance graph - dfa + node to set of acceptance sets Map
     * @throws CompilationException when the function fails
     */
    //@Override
    public AcceptanceGraph composeAG(String id,   Automaton nfa)
            throws CompilationException {

        //System.out.println("Starting accept");
        Map<AutomatonNode, List<Set<String>> > node2AcceptanceSets =
                new HashMap<AutomatonNode, List<Set<String>> >();
        Map<AutomatonNode, Set<String> > nfanode2ASet = new HashMap<AutomatonNode, Set<String> >();
        Automaton acceptGraph = new Automaton(id, !Automaton.CONSTRUCT_ROOT);
        nfanode2ASet = build_nfanode2ASet(nfa);
        //System.out.println("built nfanode2ASet");

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
            String idNode = nfa.getId() + constructLabel(stateMap.get(states));

// if this set of nfa nodes exists as a dfa node do not build a new dfa node
            if (visited.contains(idNode)) {
                continue;
            }
            visited.add(idNode);
            // nodeMap records the set of nfa nodes used to build a  dfa node
            nodeMap.put(idNode, acceptGraph.addNode());
            AutomatonNode node = nodeMap.get(idNode);
            List<Set<String>> acceptance = new ArrayList<>();
            for(AutomatonNode n : stateMap.get(states)) {
                if (!acceptance.contains(nfanode2ASet.get(n))) {
                    acceptance.add(nfanode2ASet.get(n));
                }
            }
            node2AcceptanceSets.put(node,acceptance);
            //System.out.println("Adding "+ id.toString()+" "+ acceptance);

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
                String nextId = nfa.getId() + constructLabel(stateMap.get(nextStates));
                //String nextId = constructNodeId(stateMap.get(nextStates), nfa.getId());
                //System.out.println("built "+ nextId.toString());
// only build new dfa node if it has not already been built
                if (!nodeMap.containsKey(nextId)) {
                    nodeMap.put(nextId, acceptGraph.addNode());
                }
                AutomatonNode nextNode = nodeMap.get(nextId);

                acceptGraph.addEdge(action, node, nextNode, null, true,false);

                fringe.push(nextStates);  // states to be processed
            }


        }

        acceptGraph.getNodes().stream()
                .filter(node -> node.getOutgoingEdges().isEmpty())
                .forEach(node -> node.setTerminal("STOP"));
       // printnode2AcceptanceSets(node2AcceptanceSets);
        AcceptanceGraph ag = new AcceptanceGraph(nfa,node2AcceptanceSets);
        ag.setA(acceptGraph);
        ag.setNode2AcceptanceSets(node2AcceptanceSets);
        return ag;
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
        //System.out.println("starting constructStateSet");

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
        //System.out.println("ending constructStateSet");
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
        String id = identifier + constructLabel(nodes);

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
    private Map<AutomatonNode, Set<String> > build_nfanode2ASet(Automaton a){
        Map<AutomatonNode, Set<String> > nfanode2ASet = new HashMap<AutomatonNode, Set<String> >();
        for(AutomatonNode n : a.getNodes()) {
            Set<String> as = n.getOutgoingEdges().stream().
                    distinct().
                    map(AutomatonEdge::getLabel).
                    collect(Collectors.toSet());
            nfanode2ASet.put(n,as);
           // System.out.println("++ "+n.getLabel()+" "+nfanode2ASet.get(n).toString() );

        }
        return nfanode2ASet;
    }
    private void printnode2AcceptanceSets(Map<AutomatonNode, List<Set<String>> > node2AcceptanceSets){
        //System.out.println("nfa Sets");
        //for (AutomatonNode n : nfanode2ASet.keySet()){
        //  System.out.println(" "+n.getLabel()+" "+nfanode2ASet.get(n).toString() );
        //}

        System.out.println("Acceptance Sets");
        for (AutomatonNode nd : node2AcceptanceSets.keySet()) {
            System.out.println(" "+nd.getId()+"  "+node2AcceptanceSets.get(nd));
        }

    }
}