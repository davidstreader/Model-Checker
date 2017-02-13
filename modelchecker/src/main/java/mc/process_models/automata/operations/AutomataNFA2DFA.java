package mc.process_models.automata.operations;

import com.google.common.base.Stopwatch;
import mc.Constant;
import mc.exceptions.CompilationException;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;

import java.util.*;
import java.util.stream.Collectors;

public class AutomataNFA2DFA {
    public Automaton preformNFA2DFA(Automaton automaton) throws CompilationException {
        Automaton newAutomaton = new Automaton(automaton.getId(),false);
        Stack<Set<AutomatonNode>> working = new Stack<>();
        HashMap<Set<AutomatonNode>,AutomatonNode> states = new HashMap<>();
        AutomatonNode root;
        Set<AutomatonNode> rootClosure = clousure(automaton.getRoot());
        states.put(rootClosure, root = newAutomaton.addNode());
        root.addMetaData("label",makeLabel(rootClosure));
        working.add(clousure(automaton.getRoot()));
        while (!working.isEmpty()) {
            Set<AutomatonNode> set = working.pop();
            AutomatonNode workingNode = states.get(set);
            if (isRoot(set)) {
                states.get(set).addMetaData("startNode",true);
                newAutomaton.setRoot(states.get(set));
            }
            HashMap<String,Set<AutomatonNode>> mappedToAlpha = new HashMap<>();
            for (AutomatonNode node: set) {
                for (AutomatonEdge edge: node.getOutgoingEdges()) {
                    mappedToAlpha.putIfAbsent(edge.getLabel(),new HashSet<>());
                    mappedToAlpha.get(edge.getLabel()).addAll(clousure(edge.getTo()));
                }
            }
            for (String alpha: mappedToAlpha.keySet()) {
                if (Objects.equals(alpha, Constant.HIDDEN)) continue;
                Set<AutomatonNode> mappedSet = mappedToAlpha.get(alpha);
                if (!states.keySet().contains(mappedSet)) {
                    states.put(mappedSet,newAutomaton.addNode());
                    states.get(mappedSet).addMetaData("label",makeLabel(mappedSet));
                    working.add(mappedSet);
                }
                newAutomaton.addEdge(alpha,workingNode,states.get(mappedSet));
            }
        }
        return newAutomaton;
    }
    private boolean isRoot(Set<AutomatonNode> nodes) {
        return nodes.stream().anyMatch(node -> node.hasMetaData("startNode"));
    }
    private String makeLabel(Collection<AutomatonNode> nodes) {
        return "{"+nodes.stream().map(s -> s.getMetaData("label").toString()).sorted().collect(Collectors.joining(","))+"}";
    }
    private Set<AutomatonNode> clousure(AutomatonNode node) {
        Set<AutomatonNode> nodes = new HashSet<>();
        nodes.add(node);
        for (AutomatonEdge edge: node.getOutgoingEdges()) {
            if (Objects.equals(edge.getLabel(), Constant.HIDDEN)) {
                nodes.add(edge.getTo());
                nodes.addAll(clousure(edge.getTo()));
            }
        }
        return nodes;
    }
}
