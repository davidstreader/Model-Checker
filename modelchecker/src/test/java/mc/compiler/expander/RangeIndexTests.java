package mc.compiler.expander;

import static org.junit.Assert.*;

import java.util.Stack;

import org.junit.Test;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.TerminalNode;

public class RangeIndexTests extends ExpanderTests {

	@Test
	public void correctRangeIndexExpansionTest_1(){
		String input = "automata Test = ([1..2] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_2(){
		String input = "automata Test = ([1..2]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_3(){
		String input = "automata Test = ([1..2].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_4(){
		String input = "automata Test = ([1..2][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_5(){
		String input = "automata Test = (a[1..2] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_6(){
		String input = "automata Test = (a[1..2]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_7(){
		String input = "automata Test = (a[1..2].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_8(){
		String input = "automata Test = (a[1..2][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_9(){
		String input = "automata Test = (a.[1..2] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_10(){
		String input = "automata Test = (a.[1..2]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_11(){
		String input = "automata Test = (a.[1..2].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_12(){
		String input = "automata Test = (a.[1..2][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_13(){
		String input = "automata Test = ([1][1..2] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_14(){
		String input = "automata Test = ([1][1..2]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_15(){
		String input = "automata Test = ([1][1..2].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_16(){
		String input = "automata Test = ([1][1..2][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_17(){
		String input = "automata Test = ([1..3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_18(){
		String input = "automata Test = ([1..3]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_19(){
		String input = "automata Test = ([1..3].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_20(){
		String input = "automata Test = ([1..3][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_21(){
		String input = "automata Test = (a[1..3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_22(){
		String input = "automata Test = (a[1..3]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_23(){
		String input = "automata Test = (a[1..3].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_24(){
		String input = "automata Test = (a[1..3][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_25(){
		String input = "automata Test = (a.[1..3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_26(){
		String input = "automata Test = (a.[1..3]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_27(){
		String input = "automata Test = (a.[1..3].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_28(){
		String input = "automata Test = (a.[1..3][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_29(){
		String input = "automata Test = ([1][1..3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_30(){
		String input = "automata Test = ([1][1..3]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_31(){
		String input = "automata Test = ([1][1..3].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_32(){
		String input = "automata Test = ([1][1..3][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_33(){
		String input = "automata Test = ([1..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_34(){
		String input = "automata Test = ([1..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_35(){
		String input = "automata Test = ([1..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_36(){
		String input = "automata Test = ([1..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_37(){
		String input = "automata Test = (a[1..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_38(){
		String input = "automata Test = (a[1..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_39(){
		String input = "automata Test = (a[1..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_40(){
		String input = "automata Test = (a[1..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_41(){
		String input = "automata Test = (a.[1..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_42(){
		String input = "automata Test = (a.[1..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_43(){
		String input = "automata Test = (a.[1..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_44(){
		String input = "automata Test = (a.[1..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_45(){
		String input = "automata Test = ([1][1..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_46(){
		String input = "automata Test = ([1][1..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_47(){
		String input = "automata Test = ([1][1..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_48(){
		String input = "automata Test = ([1][1..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_49(){
		String input = "automata Test = ([10..20] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_50(){
		String input = "automata Test = ([10..20]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_51(){
		String input = "automata Test = ([10..20].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_52(){
		String input = "automata Test = ([10..20][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_53(){
		String input = "automata Test = (a[10..20] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_54(){
		String input = "automata Test = (a[10..20]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_55(){
		String input = "automata Test = (a[10..20].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_56(){
		String input = "automata Test = (a[10..20][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_57(){
		String input = "automata Test = (a.[10..20] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_58(){
		String input = "automata Test = (a.[10..20]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_59(){
		String input = "automata Test = (a.[10..20].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_60(){
		String input = "automata Test = (a.[10..20][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_61(){
		String input = "automata Test = ([1][10..20] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_62(){
		String input = "automata Test = ([1][10..20]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_63(){
		String input = "automata Test = ([1][10..20].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_64(){
		String input = "automata Test = ([1][10..20][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_65(){
		String input = "automata Test = ([-2..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_66(){
		String input = "automata Test = ([-2..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_67(){
		String input = "automata Test = ([-2..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_68(){
		String input = "automata Test = ([-2..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_69(){
		String input = "automata Test = (a[-2..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_70(){
		String input = "automata Test = (a[-2..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_71(){
		String input = "automata Test = (a[-2..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_72(){
		String input = "automata Test = (a[-2..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_73(){
		String input = "automata Test = (a.[-2..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_74(){
		String input = "automata Test = (a.[-2..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_75(){
		String input = "automata Test = (a.[-2..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_76(){
		String input = "automata Test = (a.[-2..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_77(){
		String input = "automata Test = ([1][-2..5] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_78(){
		String input = "automata Test = ([1][-2..5]b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_79(){
		String input = "automata Test = ([1][-2..5].b -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
	public void correctRangeIndexExpansionTest_80(){
		String input = "automata Test = ([1][-2..5][3] -> STOP).";
		ProcessNode node = constructProcessNode(input);
		Stack<ASTNode> branches = new Stack<ASTNode>();
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
