package mc.compiler.parser;

import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 10/02/17.
 */
public class SetTests extends ParserTests {

    public SetTests() throws InterruptedException {
    }

    @Test
    public void correctIndexedSetTest_1() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{[1..2]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexedSetTest_2() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{[10..15]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(10, 15, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexedSetTest_3() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{[1..2], [3..4]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index1 = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        IndexExpNode index2 = new IndexExpNode("$v1", new RangeNode(3, 4, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index1)), null));
        indexMap.put(1, new RangesNode(new ArrayList<>(Collections.singletonList(index2)), null));

        SetNode set = new SetNode(new ArrayList<>(Arrays.asList("[$v0]", "[$v1]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexedSetTest_4() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{[1..3], [3..4]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index1 = new IndexExpNode("$v0", new RangeNode(1, 3, null), null, null);
        IndexExpNode index2 = new IndexExpNode("$v1", new RangeNode(3, 4, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index1)), null));
        indexMap.put(1, new RangesNode(new ArrayList<>(Collections.singletonList(index2)), null));

        SetNode set = new SetNode(new ArrayList<>(Arrays.asList("[$v0]", "[$v1]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexedSetTest_5() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{[1..2], a, b}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Arrays.asList("[$v0]", "a", "b")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexedSetTest_6() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{a, [1..2], b}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(1, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Arrays.asList("a", "[$v0]", "b")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexedSetTest_7() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{a, b, [1..2]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(2, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Arrays.asList("a", "b", "[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexedSetTest_8() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{[{[1..2]}]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap1 = new HashMap<>();
        IndexExpNode index1 = new IndexExpNode("$v1", new RangeNode(1, 2, null), null, null);
        indexMap1.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index1)), null));

        SetNode set1 = new SetNode(new ArrayList<>(Collections.singletonList("[$v1]")), indexMap1, null);

        Map<Integer, RangesNode> indexMap2 = new HashMap<>();
        IndexExpNode index2 = new IndexExpNode("$v0", set1, null, null);
        indexMap2.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index2)), null));

        SetNode set2 = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap2, null);

        IndexExpNode expected = new IndexExpNode("$i", set2, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantIndexedSetTest_1() throws CompilationException, InterruptedException {
        String input = "const N = 1. processes Test = ([i:{[N..2]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantIndexedSetTest_2() throws CompilationException, InterruptedException {
        String input = "const N = 2. processes Test = ([i:{[1..N]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantIndexedSetTest_3() throws CompilationException, InterruptedException {
        String input = "const N = 1. const M = 2. processes Test = ([i:{[N..M]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantIndexedSetTest_4() throws CompilationException, InterruptedException {
        String input = "range N = 1..2. processes Test = ([i:{[N]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantIndexedSetTest_5() throws CompilationException, InterruptedException {
        String input = "const N = 1. range RANGE = N..2. processes Test = ([i:{[RANGE]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantIndexedSetTest_6() throws CompilationException, InterruptedException {
        String input = "const M = 2. range RANGE = 1..M. processes Test = ([i:{[RANGE]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantIndexedSetTest_7() throws CompilationException, InterruptedException {
        String input = "const N = 1. const M = 2. range RANGE = N..M. processes Test = ([i:{[RANGE]}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantIndexedSetTest_8() throws CompilationException, InterruptedException {
        String input = "set N = {[1..2]}. processes Test = ([i:N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence = constructSequenceNode(new String[]{"[$i]"}, new TerminalNode("STOP", null));
        Map<Integer, RangesNode> indexMap = new HashMap<>();
        IndexExpNode index = new IndexExpNode("$v0", new RangeNode(1, 2, null), null, null);
        indexMap.put(0, new RangesNode(new ArrayList<>(Collections.singletonList(index)), null));

        SetNode set = new SetNode(new ArrayList<>(Collections.singletonList("[$v0]")), indexMap, null);

        IndexExpNode expected = new IndexExpNode("$i", set, sequence, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

}
