package mc.compiler.expander;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.TerminalNode;
import org.junit.Test;

import java.util.Stack;

import static org.junit.Assert.fail;

public class RangeIndexTests extends ExpanderTests {

    public RangeIndexTests() throws InterruptedException {
    }

    @Test
	public void correctRangeIndexExpansionTest_1() throws InterruptedException {
		String input = "automata Test = ([1..2] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_2() throws InterruptedException {
		String input = "automata Test = ([1..2]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_3() throws InterruptedException {
		String input = "automata Test = ([1..2].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_4() throws InterruptedException {
		String input = "automata Test = ([1..2][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_5() throws InterruptedException {
		String input = "automata Test = (a[1..2] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_6() throws InterruptedException {
		String input = "automata Test = (a[1..2]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_7() throws InterruptedException {
		String input = "automata Test = (a[1..2].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_8() throws InterruptedException {
		String input = "automata Test = (a[1..2][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_9() throws InterruptedException {
		String input = "automata Test = (a.[1..2] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_10() throws InterruptedException {
		String input = "automata Test = (a.[1..2]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_11() throws InterruptedException {
		String input = "automata Test = (a.[1..2].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_12() throws InterruptedException {
		String input = "automata Test = (a.[1..2][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_13() throws InterruptedException {
		String input = "automata Test = ([1][1..2] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_14() throws InterruptedException {
		String input = "automata Test = ([1][1..2]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_15() throws InterruptedException {
		String input = "automata Test = ([1][1..2].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_16() throws InterruptedException {
		String input = "automata Test = ([1][1..2][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_17() throws InterruptedException {
		String input = "automata Test = ([1..3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_18() throws InterruptedException {
		String input = "automata Test = ([1..3]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_19() throws InterruptedException {
		String input = "automata Test = ([1..3].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_20() throws InterruptedException {
		String input = "automata Test = ([1..3][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_21() throws InterruptedException {
		String input = "automata Test = (a[1..3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2]"}, terminal));
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
	public void correctRangeIndexExpansionTest_22() throws InterruptedException {
		String input = "automata Test = (a[1..3]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_23() throws InterruptedException {
		String input = "automata Test = (a[1..3].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_24() throws InterruptedException {
		String input = "automata Test = (a[1..3][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_25() throws InterruptedException {
		String input = "automata Test = (a.[1..3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_26() throws InterruptedException {
		String input = "automata Test = (a.[1..3]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_27() throws InterruptedException {
		String input = "automata Test = (a.[1..3].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_28() throws InterruptedException {
		String input = "automata Test = (a.[1..3][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_29() throws InterruptedException {
		String input = "automata Test = ([1][1..3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_30() throws InterruptedException {
		String input = "automata Test = ([1][1..3]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_31() throws InterruptedException {
		String input = "automata Test = ([1][1..3].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_32() throws InterruptedException {
		String input = "automata Test = ([1][1..3][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_33() throws InterruptedException {
		String input = "automata Test = ([1..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[4]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[5]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_34() throws InterruptedException {
		String input = "automata Test = ([1..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[4]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[5]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_35() throws InterruptedException {
		String input = "automata Test = ([1..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[4].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[5].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_36() throws InterruptedException {
		String input = "automata Test = ([1..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[4][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[5][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_37() throws InterruptedException {
		String input = "automata Test = (a[1..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[4]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[5]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_38() throws InterruptedException {
		String input = "automata Test = (a[1..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[4]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[5]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_39() throws InterruptedException {
		String input = "automata Test = (a[1..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[4].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[5].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_40() throws InterruptedException {
		String input = "automata Test = (a[1..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[4][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[5][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_41() throws InterruptedException {
		String input = "automata Test = (a.[1..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[4]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[5]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_42() throws InterruptedException {
		String input = "automata Test = (a.[1..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[4]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[5]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_43() throws InterruptedException {
		String input = "automata Test = (a.[1..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[4].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[5].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_44() throws InterruptedException {
		String input = "automata Test = (a.[1..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[4][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[5][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_45() throws InterruptedException {
		String input = "automata Test = ([1][1..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][4]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][5]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_46() throws InterruptedException {
		String input = "automata Test = ([1][1..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][4]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][5]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_47() throws InterruptedException {
		String input = "automata Test = ([1][1..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][4].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][5].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_48() throws InterruptedException {
		String input = "automata Test = ([1][1..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][4][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][5][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_49() throws InterruptedException {
		String input = "automata Test = ([10..20] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[10]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[11]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[12]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[13]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[14]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[15]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[16]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[17]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[18]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[19]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[20]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_50() throws InterruptedException {
		String input = "automata Test = ([10..20]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[10]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[11]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[12]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[13]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[14]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[15]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[16]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[17]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[18]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[19]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[20]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_51() throws InterruptedException {
		String input = "automata Test = ([10..20].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[10].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[11].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[12].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[13].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[14].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[15].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[16].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[17].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[18].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[19].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[20].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_52() throws InterruptedException {
		String input = "automata Test = ([10..20][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[10][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[11][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[12][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[13][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[14][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[15][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[16][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[17][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[18][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[19][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[20][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_53() throws InterruptedException {
		String input = "automata Test = (a[10..20] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[10]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[11]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[12]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[13]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[14]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[15]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[16]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[17]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[18]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[19]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[20]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_54() throws InterruptedException {
		String input = "automata Test = (a[10..20]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[10]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[11]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[12]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[13]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[14]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[15]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[16]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[17]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[18]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[19]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[20]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_55() throws InterruptedException {
		String input = "automata Test = (a[10..20].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[10].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[11].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[12].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[13].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[14].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[15].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[16].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[17].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[18].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[19].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[20].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_56() throws InterruptedException {
		String input = "automata Test = (a[10..20][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[10][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[11][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[12][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[13][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[14][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[15][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[16][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[17][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[18][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[19][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[20][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_57() throws InterruptedException {
		String input = "automata Test = (a.[10..20] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[10]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[11]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[12]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[13]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[14]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[15]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[16]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[17]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[18]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[19]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[20]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_58() throws InterruptedException {
		String input = "automata Test = (a.[10..20]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[10]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[11]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[12]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[13]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[14]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[15]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[16]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[17]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[18]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[19]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[20]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_59() throws InterruptedException {
		String input = "automata Test = (a.[10..20].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[10].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[11].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[12].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[13].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[14].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[15].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[16].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[17].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[18].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[19].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[20].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_60() throws InterruptedException {
		String input = "automata Test = (a.[10..20][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[10][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[11][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[12][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[13][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[14][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[15][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[16][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[17][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[18][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[19][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[20][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_61() throws InterruptedException {
		String input = "automata Test = ([1][10..20] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][10]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][11]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][12]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][13]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][14]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][15]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][16]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][17]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][18]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][19]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][20]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_62() throws InterruptedException {
		String input = "automata Test = ([1][10..20]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][10]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][11]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][12]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][13]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][14]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][15]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][16]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][17]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][18]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][19]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][20]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_63() throws InterruptedException {
		String input = "automata Test = ([1][10..20].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][10].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][11].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][12].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][13].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][14].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][15].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][16].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][17].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][18].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][19].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][20].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_64() throws InterruptedException {
		String input = "automata Test = ([1][10..20][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][10][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][11][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][12][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][13][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][14][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][15][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][16][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][17][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][18][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][19][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][20][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_65() throws InterruptedException {
		String input = "automata Test = ([-2..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[-2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[-1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[0]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[4]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[5]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_66() throws InterruptedException {
		String input = "automata Test = ([-2..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[-2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[-1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[0]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[4]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[5]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_67() throws InterruptedException {
		String input = "automata Test = ([-2..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[-2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[-1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[0].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[4].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[5].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_68() throws InterruptedException {
		String input = "automata Test = ([-2..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[-2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[-1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[0][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[3][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[4][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[5][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_69() throws InterruptedException {
		String input = "automata Test = (a[-2..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[-2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[-1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[0]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[4]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[5]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_70() throws InterruptedException {
		String input = "automata Test = (a[-2..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[-2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[-1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[0]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[4]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[5]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_71() throws InterruptedException {
		String input = "automata Test = (a[-2..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[-2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[-1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[0].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[4].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[5].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_72() throws InterruptedException {
		String input = "automata Test = (a[-2..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a[-2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[-1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[0][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[3][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[4][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a[5][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_73() throws InterruptedException {
		String input = "automata Test = (a.[-2..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[-2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[-1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[0]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[4]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[5]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_74() throws InterruptedException {
		String input = "automata Test = (a.[-2..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[-2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[-1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[0]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[4]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[5]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_75() throws InterruptedException {
		String input = "automata Test = (a.[-2..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[-2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[-1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[0].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[4].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[5].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_76() throws InterruptedException {
		String input = "automata Test = (a.[-2..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"a.[-2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[-1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[0][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[3][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[4][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"a.[5][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_77() throws InterruptedException {
		String input = "automata Test = ([1][-2..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][-2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][-1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][0]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][1]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][4]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][5]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_78() throws InterruptedException {
		String input = "automata Test = ([1][-2..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][-2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][-1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][0]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][1]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][4]b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][5]b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_79() throws InterruptedException {
		String input = "automata Test = ([1][-2..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][-2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][-1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][0].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][1].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][4].b"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][5].b"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}

	@Test
	public void correctRangeIndexExpansionTest_80() throws InterruptedException {
		String input = "automata Test = ([1][-2..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<>();
		TerminalNode terminal = new TerminalNode("STOP", null);
		branches.push(constructSequenceNode(new String[]{"[1][-2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][-1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][0][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][1][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][2][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][3][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][4][3]"}, terminal));
		branches.push(constructSequenceNode(new String[]{"[1][5][3]"}, terminal));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructChoiceNode(branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equivalent");
		}
	}
}
