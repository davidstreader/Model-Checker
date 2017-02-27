package mc.process_models.automata.generator;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonEdge;
import mc.process_models.automata.AutomatonNode;
import mc.process_models.automata.operations.AutomataOperations;

import java.util.*;
import java.util.stream.Collectors;

public class AutomatonGenerator {
    public List<ProcessModel> generateAutomaton(int alphabetCount, int nodeCount, String id, AutomataOperations operations, boolean multipleAlphabet) throws CompilationException {
        List<ProcessModel> automata = new ArrayList<>();
        //Generate alphabet for alphabetCount a -> b -> .. -> zz etc
        Set<String> alphabet = new HashSet<>();
        String current = "a";
        while (alphabet.size() < alphabetCount) {
            alphabet.add(current);
            current = nextCharacter(current);
        }
        Set<Set<AutomatonEdge>> edges = new HashSet<>();
        List<ProcessModel> basic = new ArrayList<>();
        root:
        for (BinaryNode treeNode : allBinaryTrees(nodeCount-1)) {
            Automaton automaton = new Automaton(id, false);
            automaton.setRoot(binToAutomata(automaton,new BinaryNode(treeNode,null), 0));
            automaton.getRoot().addMetaData("startNode",true);
            HashMap<Integer,List<AutomatonNode>> levels = new HashMap<>();
            HashMap<AutomatonNode, Integer> nodeToLevels = new HashMap<>();
            fillLevels(automaton.getRoot(), levels, nodeToLevels, 0);
            Set<Set<AutomatonNode>> rootPowerSet = Sets.powerSet(nodeToLevels.keySet());
            for (ProcessModel b : basic) {
                if (operations.bisimulation(Arrays.asList((Automaton)b,automaton))) {
                    continue root;
                }
            }
            basic.add(automaton.copy());
            if (multipleAlphabet) {
                applyAlphabet(automaton,alphabet).forEach(s -> addToSet(edges,automata,s));
            } else {
                addToSet(edges,automata,automaton);
            }
            for (Set<AutomatonNode> powerSets: rootPowerSet) {
                List<Automaton> currentNodes = new ArrayList<>();
                for (AutomatonNode node :powerSets) {
                    Set<AutomatonNode> links = levels.entrySet().stream().filter(s -> s.getKey() <= nodeToLevels.get(node)).flatMap(s -> s.getValue().stream()).collect(Collectors.toSet());
                    Set<Set<AutomatonNode>> powerSet = Sets.powerSet(links);
                    if (currentNodes.isEmpty()) {
                        currentNodes.addAll(addEdgesBelow(automaton,node,powerSet, nodeToLevels.get(node)));
                    } else {
                        List<Automaton> clone = new ArrayList<>(currentNodes);
                        for (Automaton a: currentNodes) {
                            clone.addAll(addEdgesBelow(a,node,powerSet, nodeToLevels.get(node)));
                        }
                        currentNodes = clone;
                    }
                }
                if (multipleAlphabet) {
                    currentNodes.stream().flatMap(s -> applyAlphabet(s,alphabet).stream()).forEach(s -> addToSet(edges,automata,s));
                } else {
                    currentNodes.forEach(a -> addToSet(edges,automata,a));
                }
            }
        }
        return automata;
    }
    private void addToSet(Set<Set<AutomatonEdge>> edges, List<ProcessModel> automata, Automaton a) {
        Set<AutomatonEdge> edgeSet = new HashSet<>(a.getEdges());
        if (!edges.contains(edgeSet)) {
            edges.add(edgeSet);
            automata.add(a);
        }
    }
    private List<Automaton> addEdgesBelow(Automaton automaton, AutomatonNode node, Set<Set<AutomatonNode>> powerSet, int level) throws CompilationException {
        List<Automaton> automata = new ArrayList<>();
        for (Set<AutomatonNode> set : powerSet) {
            if (set.isEmpty()) continue;
            Automaton clone = automaton.copy();
            for (AutomatonNode setNode: set) {
                AutomatonNode from = clone.getNode(node.getId());
                AutomatonNode to = clone.getNode(setNode.getId());
                clone.addEdge("edge"+level,from,to);
            }
            automata.add(clone);
        }
        return automata;
    }
    @SneakyThrows
    private Collection<Automaton> applyAlphabet(Automaton automaton, Set<String> alphabet) {
        ArrayList<Automaton> list = new ArrayList<>();
        Set<Set<String>> powerSet = Sets.powerSet(alphabet).stream().filter(s -> s.size() == automaton.getEdgeCount()).collect(Collectors.toSet());
        for (Set<String> set:powerSet) {
            Collection<List<String>> permutations = Collections2.permutations(set);
            for (List<String> permutation: permutations) {
                ArrayList<String> list2 = new ArrayList<>(permutation);
                Automaton clone = automaton.copy();
                for (AutomatonEdge edge: clone.getEdges()) {
                    edge.setLabel(list2.remove(0));
                }
                list.add(clone);
            }
        }
        return list;
    }

    private void fillLevels(AutomatonNode root, HashMap<Integer, List<AutomatonNode>> levels, HashMap<AutomatonNode, Integer> nodeToLevels, int i) {
        levels.putIfAbsent(i,new ArrayList<>());
        levels.get(i).add(root);
        nodeToLevels.put(root,i);
        for (AutomatonEdge e: root.getOutgoingEdges()) {
            fillLevels(e.getTo(), levels, nodeToLevels, i+1);
        }
    }
    private AutomatonNode binToAutomata(Automaton a, BinaryNode n, int level) throws CompilationException {
        AutomatonNode c = a.addNode();
        //Essentially, all binary trees map to arbitrary trees. https://blogs.msdn.microsoft.com/ericlippert/2010/04/22/every-tree-there-is/
        for (BinaryNode child = n.getLeft(); child != null; child = child.getRight()) {
            a.addEdge("edge"+level,c, binToAutomata(a,child, level + 1));
        }
        return c;
    }
    private String nextCharacter(String s) {
        int length = s.length();
        char c = s.charAt(length - 1);

        if(c == 'z')
            return length > 1 ? nextCharacter(s.substring(0, length - 1)) + 'a' : "aa";

        return s.substring(0, length - 1) + ++c;
    }
    private List<BinaryNode> allBinaryTrees(int n) {
        return allBinaryTrees1(1, n);
    }

    private List<BinaryNode> allBinaryTrees1(int start, int end) {
        List<BinaryNode> result = new ArrayList<>();

        if (start > end) {
            result.add(null);
            return result;
        }

        for (int i = start; i <= end; i++) {
            List<BinaryNode> left = allBinaryTrees1(start, i - 1);
            List<BinaryNode> right = allBinaryTrees1(i + 1, end);
            for (BinaryNode l : left) {
                for (BinaryNode r : right) {
                    result.add(new BinaryNode(l,r));
                }
            }
        }

        return result;
    }

}
