package mc.process_models.automata.generator;

import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.AutomatonNode;

import java.util.*;

public class AutomatonGenerator {
    public Map<String, ProcessModel> generateAutomaton(int alphabetCount, int nodeCount, String id) throws CompilationException {
        Map<String, ProcessModel> automata = new HashMap<>();
        //Generate alphabet for alphabetCount a -> b -> .. -> zz etc
        List<String> alphabet = new ArrayList<>();
        String current = "a";
        while (alphabet.size() < alphabetCount) {
            alphabet.add(current);
            current = next(current);
        }
        int i = 0;
        for (BinaryNode treeNode : allBinaryTrees(nodeCount-1)) {
            Automaton automaton = new Automaton(id+(i++), false);
            automaton.setRoot(binToAutomata(automaton,new BinaryNode(treeNode,null),alphabet,0));
            automata.put(automaton.getId(),automaton);
        }
        return automata;
    }
    private AutomatonNode binToAutomata(Automaton a, BinaryNode n, List<String> alphabet, int l) throws CompilationException {
        AutomatonNode c = a.addNode();
        //Essentially, all binary trees map to arbitrary trees. https://blogs.msdn.microsoft.com/ericlippert/2010/04/22/every-tree-there-is/
        for (BinaryNode child = n.getLeft(); child != null; child = child.getRight()) {
            a.addEdge(alphabet.get(l),c, binToAutomata(a,child, alphabet,l+1));
        }
        return c;
    }
    public String next(String s) {
        int length = s.length();
        char c = s.charAt(length - 1);

        if(c == 'z')
            return length > 1 ? next(s.substring(0, length - 1)) + 'a' : "aa";

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
