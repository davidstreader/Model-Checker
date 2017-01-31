package mc.process_models.automata.operations;

import mc.Constant;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;

import java.util.*;

public class AutomataNFA2DFA {
    public Automaton preformNFA2DFA(Automaton automaton){
        Automaton newAutomaton = new Automaton(automaton.getId(),false);
        Set<String> alphabet = automaton.getAlphabet();
        HashMap<String,HashMap<String,HashSet<String>>> table = new HashMap<>();
        for (AutomatonNode cur: automaton.getNodes()) {
            String curLbl = cur.getMetaData("label").toString();
            for (AutomatonEdge rout : cur.getOutgoingEdges()) {
                AutomatonNode to = rout.getTo();
                table.putIfAbsent(curLbl,new HashMap<>());
                table.get(curLbl).putIfAbsent(rout.getLabel(),new HashSet<>());
                table.get(curLbl).get(rout.getLabel()).add(to.getMetaData("label").toString());
            }
            table.get(curLbl).putIfAbsent(Constant.HIDDEN,new HashSet<>());
            table.get(curLbl).get(Constant.HIDDEN).add(curLbl);
        }
        table.remove(Constant.HIDDEN);
        Stack<Set<String>> stack = new Stack<>();
        stack.add(clousure(automaton.getRoot()));
        HashMap<String,HashMap<String,Set<String>>> table2 = new HashMap<>();
        while (!stack.isEmpty()) {
            Set<String> curList = stack.pop();
            String lbl = curList.toString();
            for (String a : alphabet) {
                Set<String> subTable = new HashSet<>();
                for (String cur : curList) {
                    HashMap<String,HashSet<String>> ta = table.get(cur);
                    if (ta.containsKey(a)) {
                        for (String s : ta.get(a)) {
                            table.get(s).get(Constant.HIDDEN).forEach(subTable::add);
                        }
                    }
                }
                if (subTable.isEmpty()) continue;
                table2.putIfAbsent(lbl,new HashMap<>());
                if (table2.get(lbl).containsKey(a)) continue;
                table2.get(lbl).put(a,subTable);
                stack.push(subTable);
            }
        }
        HashMap<String,AutomatonNode> nodeTable = new HashMap<>();
        for (String node : table2.keySet()) {
            nodeTable.put(node,newAutomaton.addNode());
            nodeTable.get(node).addMetaData("label",node);
            if (isRoot(node,automaton)) {
                newAutomaton.setRoot(nodeTable.get(node));
                nodeTable.get(node).addMetaData("startNode",true);
            }
        }
        for (String node : table2.keySet()) {
            for (String edge : table2.get(node).keySet()) {
                if (Objects.equals(edge, Constant.HIDDEN)) continue;
                newAutomaton.addEdge(edge,nodeTable.get(node),nodeTable.get(table2.get(node).get(edge).toString()));
            }
        }
        return newAutomaton;
    }

    private boolean isRoot(String node, Automaton automaton) {
        String[] nodes = node.replaceAll("[\\[\\]]","").split(",");
        return Arrays.asList(nodes).contains(automaton.getRoot().getMetaData("label").toString());
    }

    public Set<String> clousure(AutomatonNode node) {
        Set<String> nodes = new HashSet<>();
        nodes.add(node.getMetaData("label").toString());
        for (AutomatonEdge edge: node.getOutgoingEdges()) {
            if (Objects.equals(edge.getLabel(), Constant.HIDDEN)) {
                nodes.add(edge.getTo().getMetaData("label").toString());
            }
        }
        return nodes;
    }
}
