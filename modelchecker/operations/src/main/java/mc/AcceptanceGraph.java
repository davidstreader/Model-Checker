package mc;

import lombok.Getter;
import lombok.Setter;
import mc.exceptions.CompilationException;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonEdge;
import mc.processmodels.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by dstr on 24/01/18.
 * An acceptance graph should be a dfa plus a Map from nodes to a set of acceptance sets
 */
public class AcceptanceGraph {

    @Getter
    @Setter
    private Automaton automaton;
    @Getter
    @Setter
    private Map<AutomatonNode, List<Set<String>>> node2AcceptanceSets =
            new HashMap<AutomatonNode, List<Set<String>>>();


    /**
     * Construct an Acceptance Graph from a nfa
     *
     * @param id  the id of the resulting automaton
     * @param nfa an automata taken in by the function
     * @return acceptance graph - dfa + node to set of acceptance sets Map
     * @throws CompilationException when the function fails
     */

    public AcceptanceGraph(String id, Automaton nfa) throws CompilationException {
        //Automaton nfa = nfain.copy();  // must copy
        Map<AutomatonNode, List<Set<String>>> dfaNode2ASet = new HashMap<AutomatonNode, List<Set<String>>>();
        Map<AutomatonNode, Set<String>> nfaNode2A = build_nfanode2ASet(nfa);

        Automaton dfa = new Automaton(id, !Automaton.CONSTRUCT_ROOT);

        Map<AutomatonNode, Set<AutomatonNode>> dfa2nfaSet = new HashMap<>(); //dfa to nfa

        Set<AutomatonNode> visited = new HashSet<>(); //set of dfa nodes TODO, this is never queried

        Stack<AutomatonNode> fringe = new Stack<>();

        //set up root node
        for (AutomatonNode nfaRoot : nfa.getRoot()) {
            AutomatonNode drt = dfa.addNode();
            dfa.addRoot(drt);

            Set<AutomatonNode> nr = new HashSet<AutomatonNode>();
            nr.add(nfaRoot);  // one dfa root maps to a singleton set of nfa roots
            dfa2nfaSet.put(drt, nr);
        }

        for (AutomatonNode dfaRoot : dfa.getRoot()) {
            fringe.push(dfaRoot);
            dfa2nfaSet.put(dfaRoot, nfa.getRoot());
        }

        Set<String> alphabet = nfa.getAlphabet();
        int cnt = 0;
        while (!fringe.isEmpty() && cnt < 30) {
            AutomatonNode poped = fringe.pop();

            visited.add(poped);


            List<Set<String>> acceptance = new LinkedList<>();
            // get the nfa nodes making this dfa node
            dfa2nfaSet.get(poped).stream().filter(n -> !acceptance.contains(nfaNode2A.get(n))).forEach(n ->
                    acceptance.add(nfaNode2A.get(n))
            );
            dfaNode2ASet.put(poped, acceptance);

            for (String action : alphabet) {
                Set<AutomatonNode> nextStates = constructStateSet(dfa2nfaSet.get(poped), action);
                AutomatonNode nextdfa = null;
                if (nextStates.isEmpty()) {
                    continue;
                }
                for (AutomatonNode dfand : dfa2nfaSet.keySet()) {
                    if (dfa2nfaSet.get(dfand).equals(nextStates)) {
                        nextdfa = dfand;
                        break;
                    }
                }
                // only build new dfa node if it has not already been built

                if (nextdfa == null) {
                    nextdfa = dfa.addNode();
                    dfa2nfaSet.put(nextdfa, nextStates);
                    fringe.push(nextdfa);    // states to be processed
                }

                cnt++;
                dfa.addEdge(action, poped, nextdfa, null, true);

            }
        }

        dfa.getNodes().stream()
                .filter(nodex -> nodex.getOutgoingEdges().isEmpty())
                .forEach(nodey -> nodey.setTerminal("STOP"));

        this.setAutomaton(dfa);
        this.setNode2AcceptanceSets(dfaNode2ASet);

    }


    /**
     * @param nodes  set of nfa nodes of curret dfa node
     * @param action an action of events leaving set of nfa nodes
     * @return set of next nfa nodes
     */
    private Set<AutomatonNode> constructStateSet(Set<AutomatonNode> nodes, String action) {
        Set<AutomatonNode> nextNodes = new TreeSet<>();
        Stack<AutomatonNode> fringe = new Stack<>();

        nodes.forEach(fringe::push);

        while (!fringe.isEmpty()) {
            AutomatonNode current = fringe.pop();

            List<AutomatonEdge> edges = current.getOutgoingEdges();
            nextNodes.addAll(edges.stream().filter(edge -> action.equals(edge.getLabel()))
                                            .map(AutomatonEdge::getTo)
                                            .collect(Collectors.toList()));
        }

        return nextNodes;
    }

    /**
     * @param nodes      list of nfa nodes to make one dfa node
     * @param identifier
     * @return the dfa nodeid
     * Side effect build the acceptance set and store in node2AcceptanceSets
     */
    private String constructNodeId(Set<AutomatonNode> nodes, String identifier) {
        return identifier + constructLabel(nodes);
    }

    /**
     * @param nodes set of nodes n1,n2
     * @return a string of the set of nodes "{n1,n1}"
     */
    private String constructLabel(Set<AutomatonNode> nodes) {
        Set<String> labelSet = nodes.stream().map(node -> Integer.toString(node.getLabelNumber()))
                                             .collect(Collectors.toSet());
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
     * @param a automaton
     *          This computes the map nfanode2ASe
     */
    private Map<AutomatonNode, Set<String>> build_nfanode2ASet(Automaton a) {
        Map<AutomatonNode, Set<String>> nfanode2ASet = new HashMap<AutomatonNode, Set<String>>();
        for (AutomatonNode n : a.getNodes()) {
            Set<String> as = n.getOutgoingEdges().stream().
                    distinct().
                    map(AutomatonEdge::getLabel).
                    collect(Collectors.toSet());
            nfanode2ASet.put(n, as);
        }
        return nfanode2ASet;
    }

    /**
     * Colors the nodes of an Acceptance graph
     *
     * @param cmap defines the colour in terms of a set of acceptance sets
     *             first time this should be an empty map
     *             colouring subsequent Acceptance graph should reuse the old cmap
     */
    public Integer colorNodes(Map<Integer, List<Set<String>>> cmap,
                              Map<AutomatonNode, List<Set<String>>> n2as,
                              Integer color) {


        for (AutomatonNode nd : this.getAutomaton().getNodes()) {
            List<Set<String>> accept = n2as.get(nd);

            for (int i : cmap.keySet()) {
                if (this.AcceptanceSetEquality(cmap.get(i), accept)) {
                    nd.setColour(i);

                    color++;
                    nd.setColour(color);
                    cmap.put(color, accept);
                    break;
                }
            }
        }
        return color;
    }

    private String node2ac_toString() {
        String outString = "";
        for (AutomatonNode nd : node2AcceptanceSets.keySet()) {
            outString = outString + ("\n " + nd.getId() + "  " + node2AcceptanceSets.get(nd));
        }
        return outString;
    }


    private void printnode2AcceptanceSets() {

/*  System.out.println("Acceptance Sets");
  for (AutomatonNode nd : node2AcceptanceSets.keySet()) {
   System.out.println(" "+nd.getId()+"  "+node2AcceptanceSets.get(nd));
  }*/
    }

    private boolean AcceptanceSubSet(List<Set<String>> a1, List<Set<String>> a2) {
        boolean ok = false;
        for (Set<String> as1 : a1) {
            ok = false;
            for (Set<String> as2 : a2) {
                if (as1.containsAll(as2)) {
                    ok = true;
                    break;
                }
            }  // if one true then inner loop true
            if (!ok) {
                break;
            } //if one inner false then outer false
        }  //outer only true if all inner loops true
        return ok;
    }

    private boolean AcceptanceSetEquality(List<Set<String>> a1, List<Set<String>> a2) {
        // System.out.println("Equ ?  "+ a1.toString() + "  "+ a2.toString());
        return AcceptanceSubSet(a1, a2) && AcceptanceSubSet(a2, a1);
    }

    /*
      Constructor
     */
    AcceptanceGraph(Automaton ain, Map<AutomatonNode, List<Set<String>>> n2as) {
        this.automaton = ain;
        this.node2AcceptanceSets = n2as;
    }

    public String toString() {
        if (this.getAutomaton() == null) {
            System.out.println("AcceptanceGraph aut = null");
        }
        if (this.getNode2AcceptanceSets() == null) {
            System.out.println("AcceptanceGraph n2ac = null");
        }
        return "Acceptance Graph  \n  " + this.getAutomaton().toString() + " " +
                this.node2ac_toString() + " End Acceptance Graph \n";

    }
}
