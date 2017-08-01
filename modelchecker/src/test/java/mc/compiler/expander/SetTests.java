package mc.compiler.expander;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.TerminalNode;
import org.junit.Test;

import java.util.Stack;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 9/02/17.
 */
public class SetTests extends ExpanderTests {

    public SetTests() throws InterruptedException {
    }

    @Test
    public void correctSetExpansionTest_1() throws InterruptedException {
        String input = "automata Test = ([i:{[1..2]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetExpansionTest_2() throws InterruptedException {
        String input = "automata Test = ([i:{[10..15]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[10]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[11]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[12]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[13]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[14]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[15]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetExpansionTest_3() throws InterruptedException {
        String input = "automata Test = ([i:{[1..2], [3..4]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[3]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[4]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetExpansionTest_4() throws InterruptedException {
        String input = "automata Test = ([i:{[1..3], [3..4]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[3]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[3]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[4]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetExpansionTest_5() throws InterruptedException {
        String input = "automata Test = ([i:{[1..2], a, b}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"b"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetExpansionTest_6() throws InterruptedException {
        String input = "automata Test = ([i:{a, [1..2], b}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"b"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetExpansionTest_7() throws InterruptedException {
        String input = "automata Test = ([i:{a, b, [1..2]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"b"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctSetExpansionTest_8() throws InterruptedException {
        String input = "automata Test = ([i:{[{[1..2]}]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantSetExpansionTest_1() throws InterruptedException {
        String input = "const N = 1 automata Test = ([i:{[N..2]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantSetExpansionTest_2() throws InterruptedException {
        String input = "const N = 2 automata Test = ([i:{[1..N]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantSetExpansionTest_3() throws InterruptedException {
        String input = "const N = 1 const M = 2 automata Test = ([i:{[N..M]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantSetExpansionTest_4() throws InterruptedException {
        String input = "range N = 1..2 automata Test = ([i:{[N]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantSetExpansionTest_5() throws InterruptedException {
        String input = "const N = 1 range RANGE = N..2 automata Test = ([i:{[RANGE]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantSetExpansionTest_6() throws InterruptedException {
        String input = "const M = 2 range RANGE = 1..M automata Test = ([i:{[RANGE]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantSetExpansionTest_7() throws InterruptedException {
        String input = "const N = 1 const M = 2 range RANGE = N..M automata Test = ([i:{[RANGE]}] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }

    @Test
    public void correctConstantSetExpansionTest_8() throws InterruptedException {
        String input = "set N = {[1..2]} automata Test = ([i:N] -> STOP).";
        ProcessNode node = constructProcessNode(input);

        Stack<ASTNode> branches = new Stack<>();
        branches.push(constructSequenceNode(new String[]{"[1]"}, new TerminalNode("STOP", null)));
        branches.push(constructSequenceNode(new String[]{"[2]"}, new TerminalNode("STOP", null)));

        ASTNode expected = branches.pop();
        while(!branches.isEmpty()){
            expected = constructChoiceNode(branches.pop(), expected);
        }

        if(!expected.equals(node.getProcess())){
            fail("expecting choice nodes to be equivalent");
        }
    }
}
