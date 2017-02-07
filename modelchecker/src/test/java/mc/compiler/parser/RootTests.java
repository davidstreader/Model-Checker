package mc.compiler.parser;

import mc.compiler.ast.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 2/02/17.
 */
public class RootTests extends ParserTests {

    @Test
    public void correctRootTest_1(){
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
    public void correctRootTest_2(){
        String input = "automata Test = (a -> STOP)/{b/a}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("b", "a");
        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_3(){
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
    public void correctRootTest_4(){
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
    public void correctRootTest_5(){
        String input = "automata Test = a:(a -> STOP)/{b/a}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("b", "a");
        ProcessRootNode expected = new ProcessRootNode(process, "a", relabel, null, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_6(){
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
    public void correctRootTest_7(){
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
    public void correctRootTest_8(){
        String input = "automata Test = (a -> STOP)/{b/a}\\{b}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("b", "a");
        HidingNode hiding = constructHiding("includes", "b");
        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_9(){
        String input = "automata Test = (a -> STOP)/{b/a}@{b}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("b", "a");
        HidingNode hiding = constructHiding("excludes", "b");
        ProcessRootNode expected = new ProcessRootNode(process, null, relabel, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_10(){
        String input = "automata Test = a:(a -> STOP)/{b/a.a}\\{b}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("b", "a.a");
        HidingNode hiding = constructHiding("includes", "b");
        ProcessRootNode expected = new ProcessRootNode(process, "a", relabel, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    @Test
    public void correctRootTest_11(){
        String input = "automata Test = a:(a -> STOP)/{b/a.a}@{b}.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"a"}, terminal);
        RelabelNode relabel = constructRelabelSet("b", "a.a");
        HidingNode hiding = constructHiding("excludes", "b");
        ProcessRootNode expected = new ProcessRootNode(process, "a", relabel, hiding, null);
        if(!expected.equals(node.getProcess())){
            fail("expecting process root nodes to be equivalent");
        }
    }

    private RelabelNode constructRelabelSet(String newLabel, String oldLabel){
        RelabelElementNode element = new RelabelElementNode(newLabel, oldLabel, null);
        List<RelabelElementNode> elements = new ArrayList<RelabelElementNode>();
        elements.add(element);
        return new RelabelNode(elements, null);
    }

    private HidingNode constructHiding(String type, String element){
        List<String> set = new ArrayList<String>();
        set.add(element);
        return new HidingNode(type, set, null);
    }
}
