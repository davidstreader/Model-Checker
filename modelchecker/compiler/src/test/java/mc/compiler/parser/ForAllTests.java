package mc.compiler.parser;

import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Created by sheriddavi on 8/02/17.
 */
public class ForAllTests extends ParserTests {

    public ForAllTests() throws InterruptedException {
    }

    @Test
    public void correctForAllTest_1() throws CompilationException, InterruptedException {
        String input = "processes Test = (forall [i:1..2] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        IndexNode index = new IndexNode("$i", new RangeNode(1, 2, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

    @Test
    public void correctForAllTest_2() throws CompilationException, InterruptedException {
        String input = "processes Test = (forall [i:1..3] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        IndexNode index = new IndexNode("$i", new RangeNode(1, 3, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

    @Test
    public void correctForAllTest_3() throws CompilationException, InterruptedException {
        String input = "processes Test = (forall [i:{a}] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        List<String> set = new ArrayList<>(Collections.singletonList("a"));
        IndexNode index = new IndexNode("$i", new SetNode(set, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

    @Test
    public void correctForAllTest_4() throws CompilationException, InterruptedException {
        String input = "processes Test = (forall [i:{a, b}] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        List<String> set = new ArrayList<>(Arrays.asList("a", "b"));
        IndexNode index = new IndexNode("$i", new SetNode(set, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

    @Test
    public void correctForAllTest_5() throws CompilationException, InterruptedException {
        String input = "processes Test = (forall [i:{a, b, c}] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        List<String> set = new ArrayList<>(Arrays.asList("a", "b", "c"));
        IndexNode index = new IndexNode("$i", new SetNode(set, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

    @Test
    public void correctForAllTest_6() throws CompilationException, InterruptedException {
        String input = "range N = 1..2 processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        IndexNode index = new IndexNode("$i", new RangeNode(1, 2, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

    @Test
    public void correctForAllTest_7() throws CompilationException, InterruptedException {
        String input = "range N = 1..3 processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        IndexNode index = new IndexNode("$i", new RangeNode(1, 3, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

    @Test
    public void correctForAllTest_8() throws CompilationException, InterruptedException {
        String input = "set N = {a}. processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        List<String> set = new ArrayList<>(Collections.singletonList("a"));
        IndexNode index = new IndexNode("$i", new SetNode(set, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

    @Test
    public void correctForAllTest_9() throws CompilationException, InterruptedException {
        String input = "set N = {a, b}. processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        List<String> set = new ArrayList<>(Arrays.asList("a", "b"));
        IndexNode index = new IndexNode("$i", new SetNode(set, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

    @Test
    public void correctForAllTest_10() throws CompilationException, InterruptedException {
        String input = "set N = {a, b, c}. processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
                "automata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode process = new ProcessRootNode(sequence, "[$i]", null, null, null);
        List<String> set = new ArrayList<>(Arrays.asList("a", "b", "c"));
        IndexNode index = new IndexNode("$i", new SetNode(set, null), null, null);
        RangesNode ranges = new RangesNode(Collections.singletonList(index), null);
        ForAllStatementNode expected = new ForAllStatementNode(ranges, process, null);

        if(!expected.equals(node.getProcess())){
            fail("expecting forall nodes to be equivalent");
        }
    }

}
