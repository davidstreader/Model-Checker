package mc.compiler.expander;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.TerminalNode;
import org.junit.Test;

import java.util.Stack;

import static org.junit.Assert.fail;

public class SetIndexTests extends ExpanderTests {

    public SetIndexTests() throws InterruptedException {
    }

    @Test
    public void correctSetIndexExpansionTest_1() throws InterruptedException {
        String input = "automata Test = ([{a}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_2() throws InterruptedException {
        String input = "automata Test = ([{a}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"ab"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_3() throws InterruptedException {
        String input = "automata Test = ([{a}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_4() throws InterruptedException {
        String input = "automata Test = ([{a}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_5() throws InterruptedException {
        String input = "automata Test = (a[{a}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aa"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_6() throws InterruptedException {
        String input = "automata Test = (a[{a}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aab"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_7() throws InterruptedException {
        String input = "automata Test = (a[{a}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aa.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_8() throws InterruptedException {
        String input = "automata Test = (a[{a}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aa[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_9() throws InterruptedException {
        String input = "automata Test = (a.[{a}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.a"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_10() throws InterruptedException {
        String input = "automata Test = (a.[{a}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.ab"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_11() throws InterruptedException {
        String input = "automata Test = (a.[{a}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.a.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_12() throws InterruptedException {
        String input = "automata Test = (a.[{a}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.a[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_13() throws InterruptedException {
        String input = "automata Test = ([1][{a}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]a"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_14() throws InterruptedException {
        String input = "automata Test = ([1][{a}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]ab"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_15() throws InterruptedException {
        String input = "automata Test = ([1][{a}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]a.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_16() throws InterruptedException {
        String input = "automata Test = ([1][{a}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]a[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_17() throws InterruptedException {
        String input = "automata Test = ([{a, b}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a"}, terminal));
        branches.push(constructSequenceNode(new String[]{"b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_18() throws InterruptedException {
        String input = "automata Test = ([{a, b}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"ab"}, terminal));
        branches.push(constructSequenceNode(new String[]{"bb"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_19() throws InterruptedException {
        String input = "automata Test = ([{a, b}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"b.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_20() throws InterruptedException {
        String input = "automata Test = ([{a, b}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"b[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_21() throws InterruptedException {
        String input = "automata Test = (a[{a, b}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aa"}, terminal));
        branches.push(constructSequenceNode(new String[]{"ab"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_22() throws InterruptedException {
        String input = "automata Test = (a[{a, b}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aab"}, terminal));
        branches.push(constructSequenceNode(new String[]{"abb"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_23() throws InterruptedException {
        String input = "automata Test = (a[{a, b}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aa.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"ab.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_24() throws InterruptedException {
        String input = "automata Test = (a[{a, b}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aa[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"ab[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_25() throws InterruptedException {
        String input = "automata Test = (a.[{a, b}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.a"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_26() throws InterruptedException {
        String input = "automata Test = (a.[{a, b}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.ab"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.bb"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_27() throws InterruptedException {
        String input = "automata Test = (a.[{a, b}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.a.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.b.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_28() throws InterruptedException {
        String input = "automata Test = (a.[{a, b}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.a[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.b[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_29() throws InterruptedException {
        String input = "automata Test = ([1][{a, b}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]a"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_30() throws InterruptedException {
        String input = "automata Test = ([1][{a, b}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]ab"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]bb"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_31() throws InterruptedException{
        String input = "automata Test = ([1][{a, b}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]a.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]b.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_32() throws InterruptedException{
        String input = "automata Test = ([1][{a, b}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]a[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]b[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_33() throws InterruptedException{
        String input = "automata Test = ([{a, b, c}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a"}, terminal));
        branches.push(constructSequenceNode(new String[]{"b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"c"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_34() throws InterruptedException{
        String input = "automata Test = ([{a, b, c}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"ab"}, terminal));
        branches.push(constructSequenceNode(new String[]{"bb"}, terminal));
        branches.push(constructSequenceNode(new String[]{"cb"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_35() throws InterruptedException{
        String input = "automata Test = ([{a, b, c}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"b.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"c.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_36() throws InterruptedException{
        String input = "automata Test = ([{a, b, c}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"b[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"c[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_37() throws InterruptedException{
        String input = "automata Test = (a[{a, b, c}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aa"}, terminal));
        branches.push(constructSequenceNode(new String[]{"ab"}, terminal));
        branches.push(constructSequenceNode(new String[]{"ac"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_38() throws InterruptedException{
        String input = "automata Test = (a[{a, b, c}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aab"}, terminal));
        branches.push(constructSequenceNode(new String[]{"abb"}, terminal));
        branches.push(constructSequenceNode(new String[]{"acb"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_39() throws InterruptedException{
        String input = "automata Test = (a[{a, b, c}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aa.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"ab.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"ac.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_40() throws InterruptedException{
        String input = "automata Test = (a[{a, b, c}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"aa[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"ab[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"ac[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_41() throws InterruptedException{
        String input = "automata Test = (a.[{a, b, c}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.a"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.c"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_42() throws InterruptedException{
        String input = "automata Test = (a.[{a, b, c}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.ab"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.bb"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.cb"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_43() throws InterruptedException{
        String input = "automata Test = (a.[{a, b, c}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.a.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.b.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.c.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_44() throws InterruptedException{
        String input = "automata Test = (a.[{a, b, c}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"a.a[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.b[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"a.c[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_45() throws InterruptedException{
        String input = "automata Test = ([1][{a, b, c}] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]a"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]c"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_46() throws InterruptedException{
        String input = "automata Test = ([1][{a, b, c}]b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]ab"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]bb"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]cb"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_47() throws InterruptedException{
        String input = "automata Test = ([1][{a, b, c}].b -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]a.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]b.b"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]c.b"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetIndexExpansionTest_48() throws InterruptedException{
        String input = "automata Test = ([1][{a, b, c}][3] -> STOP).";
        ProcessNode node = constructProcessNode(input);
        Stack<ASTNode> branches = new Stack<>();
        TerminalNode terminal = new TerminalNode("STOP", null);
        branches.push(constructSequenceNode(new String[]{"[1]a[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]b[3]"}, terminal));
        branches.push(constructSequenceNode(new String[]{"[1]c[3]"}, terminal));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

}
