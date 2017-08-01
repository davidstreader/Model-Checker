package mc.compiler.expander;

import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 9/02/17.
 */
public class RootTests extends ExpanderTests {

    public RootTests() throws InterruptedException {
    }

    @Test
    public void correctExpandedRelabelSetTest_1() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)/{[i:1..2].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("[1].test", "[1]", null));
        elements.add(new RelabelElementNode("[2].test", "[2]", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctExpandedRelabelSetTest_2() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)/{[i:1..3].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("[1].test", "[1]", null));
        elements.add(new RelabelElementNode("[2].test", "[2]", null));
        elements.add(new RelabelElementNode("[3].test", "[3]", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctExpandedRelabelSetTest_3() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)/{[i:{a}].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("a.test", "a", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctExpandedRelabelSetTest_4() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)/{[i:{a , b}].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("a.test", "a", null));
        elements.add(new RelabelElementNode("b.test", "b", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctExpandedRelabelSetTest_5() throws CompilationException, InterruptedException {
        String input = "automata Test = (a -> STOP)/{[i:{a , b, c}].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("a.test", "a", null));
        elements.add(new RelabelElementNode("b.test", "b", null));
        elements.add(new RelabelElementNode("c.test", "c", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctExpandedRelabelSetTest_6() throws CompilationException, InterruptedException {
        String input = "range N = 1..2 automata Test = (a -> STOP)/{[i:N].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("[1].test", "[1]", null));
        elements.add(new RelabelElementNode("[2].test", "[2]", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctExpandedRelabelSetTest_7() throws CompilationException, InterruptedException {
        String input = "range N = 1..3 automata Test = (a -> STOP)/{[i:N].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("[1].test", "[1]", null));
        elements.add(new RelabelElementNode("[2].test", "[2]", null));
        elements.add(new RelabelElementNode("[3].test", "[3]", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctExpandedRelabelSetTest_8() throws CompilationException, InterruptedException {
        String input = "set N = {a} automata Test = (a -> STOP)/{[i:N].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("a.test", "a", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctExpandedRelabelSetTest_9() throws CompilationException, InterruptedException {
        String input = "set N = {a, b} automata Test = (a -> STOP)/{[i:N].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("a.test", "a", null));
        elements.add(new RelabelElementNode("b.test", "b", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctExpandedRelabelSetTest_10() throws CompilationException, InterruptedException {
        String input = "set N = {a, b, c} automata Test = (a -> STOP)/{[i:N].test/[i]}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);

        List<RelabelElementNode> elements = new ArrayList<>();
        elements.add(new RelabelElementNode("a.test", "a", null));
        elements.add(new RelabelElementNode("b.test", "b", null));
        elements.add(new RelabelElementNode("c.test", "c", null));
        RelabelNode relabel = new RelabelNode(elements, null);

        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }
}
