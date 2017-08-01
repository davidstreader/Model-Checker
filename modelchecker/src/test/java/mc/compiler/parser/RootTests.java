package mc.compiler.parser;

import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 2/02/17.
 */
public class RootTests extends ParserTests {

    @Test
    public void correctRootTest_1() throws CompilationException, InterruptedException {
        String input = "automata Test = a:(a -> STOP).";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        ProcessRootNode expected = new ProcessRootNode(process, "a", null, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_2() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)/{b/a}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("a");
        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_3() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)\\{a}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        HidingNode hiding = constructHiding("includes", "a");
        ProcessRootNode expected = new ProcessRootNode(process, null, null, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_4() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)@{a}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        HidingNode hiding = constructHiding("excludes", "a");
        ProcessRootNode expected = new ProcessRootNode(process, null, null, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_5() throws CompilationException, InterruptedException {
        String input = "automata Test = a:(a -> STOP)/{b/a}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("a");
        ProcessRootNode expected = new ProcessRootNode(process, "a", relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_6() throws CompilationException, InterruptedException {
        String input = "automata Test = a:(a -> STOP)\\{a.a}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        HidingNode hiding = constructHiding("includes", "a.a");
        ProcessRootNode expected = new ProcessRootNode(process, "a", null, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_7() throws CompilationException, InterruptedException {
        String input = "automata Test = a:(a -> STOP)@{a.a}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        HidingNode hiding = constructHiding("excludes", "a.a");
        ProcessRootNode expected = new ProcessRootNode(process, "a", null, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_8() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)/{b/a}\\{b}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("a");
        HidingNode hiding = constructHiding("includes", "b");
        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_9() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)/{b/a}@{b}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("a");
        HidingNode hiding = constructHiding("excludes", "b");
        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_10() throws CompilationException, InterruptedException {
        String input = "automata Test = a:(a -> STOP)/{b/a.a}\\{b}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("a.a");
        HidingNode hiding = constructHiding("includes", "b");
        ProcessRootNode expected = new ProcessRootNode(process, "a", relabel, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_11() throws CompilationException, InterruptedException {
        String input = "automata Test = a:(a -> STOP)/{b/a.a}@{b}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("a.a");
        HidingNode hiding = constructHiding("excludes", "b");
        ProcessRootNode expected = new ProcessRootNode(process, "a", relabel, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexedRelabelSetTest_1() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)/{[i:1..2].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        IndexNode index = new IndexNode("$i", new RangeNode(1, 2, null), null, null);
        RangesNode range = new RangesNode(new ArrayList<>(Collections.singletonList(index)), null);
        RelabelNode relabel = constructRelabelSet(range);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    private RelabelNode constructRelabelSet(String oldLabel){
        RelabelElementNode element = new RelabelElementNode("b", oldLabel, null);
        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(element);
        return new RelabelNode(elements, null);
    }

    private RelabelNode constructRelabelSet(RangesNode ranges){
        RelabelElementNode element = new RelabelElementNode("[$i].test", "[$i]", null);
        element.setRanges(ranges);
        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(element);
        return new RelabelNode(elements, null);
    }

    private HidingNode constructHiding(String type, String element){
        List<String> set = new ArrayList<>();
        set.add(element);
        return new HidingNode(type, new SetNode(set, null), null);
    }
}
