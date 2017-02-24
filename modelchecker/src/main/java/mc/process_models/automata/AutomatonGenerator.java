package mc.process_models.automata;

import mc.exceptions.CompilationException;

import java.util.*;

public class AutomatonGenerator {
    public Automaton generateAutomaton(int alphabetCount, int nodeCount, String id) throws CompilationException {
        List<AutomatonNode> nodes = new ArrayList<>();
        Automaton automaton = new Automaton(id);
        //Exclude root
        nodeCount--;
        nodes.add(automaton.getRoot());
        //Add the rest of the nodes
        for (int i =0; i < nodeCount; i++) nodes.add(automaton.addNode());
        //Generate alphabet for alphabetCount a -> b -> .. -> zz etc
        List<String> alphabet = new ArrayList<>();
        String current = "a";
        while (alphabet.size() < alphabetCount) {
            alphabet.add(current);
            current = next(current);
        }
        Random random = new Random();
        //Randomize the alphabet order
        Collections.shuffle(alphabet);
        alphabet.subList(alphabetCount, alphabet.size()).clear();
        for (AutomatonNode node: nodes) {
            for (String alpha: alphabet) {
                if (random.nextBoolean()) {
                    AutomatonNode next = nodes.get(random.nextInt(nodes.size()));
                    automaton.addEdge(alpha, node, next);
                }
            }
        }
        return automaton;
    }
    public static String next(String s) {
        int length = s.length();
        char c = s.charAt(length - 1);

        if(c == 'z')
            return length > 1 ? next(s.substring(0, length - 1)) + 'a' : "aa";

        return s.substring(0, length - 1) + ++c;
    }
}
