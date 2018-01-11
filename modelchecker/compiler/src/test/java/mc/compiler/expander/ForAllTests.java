package mc.compiler.expander;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.ProcessRootNode;
import mc.compiler.ast.TerminalNode;
import org.junit.Test;

import java.util.Stack;

import static org.junit.Assert.fail;

public class ForAllTests extends ExpanderTests {

    public ForAllTests() throws InterruptedException {
    }

    @Test
	public void correctForAllExpansionTest_1() throws InterruptedException {
		String input = "processes Test = (forall [i:1..2] ([i]:(a -> STOP))).\nautomata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[1]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[2]", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void correctForAllExpansionTest_2() throws InterruptedException {
		String input = "processes Test = (forall [i:1..3] ([i]:(a -> STOP))).\nautomata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[1]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[2]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[3]", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void correctForAllExpansionTest_3() throws InterruptedException {
		String input = "processes Test = (forall [i:{a}] ([i]:(a -> STOP))).\n" +
				"automata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void correctForAllExpansionTest_4() throws InterruptedException {
		String input = "processes Test = (forall [i:{a, b}] ([i]:(a -> STOP))).\n" +
				"automata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "b", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void correctForAllExpansionTest_5() throws InterruptedException {
		String input = "processes Test = (forall [i:{a, b, c}] ([i]:(a -> STOP))).\n" +
				"automata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "b", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "c", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void correctForAllExpansionTest_6() throws InterruptedException {
		String input = "range N = 1..2. processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
				"automata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[1]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[2]", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void correctForAllExpansionTest_7() throws InterruptedException {
		String input = "range N = 1..3. processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
				"automata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[1]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[2]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[3]", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void correctForAllExpansionTest_8() throws InterruptedException {
		String input = "set N = {a}. processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
				"automata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void correctForAllExpansionTest_9() throws InterruptedException {
		String input = "set N = {a, b}. processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
				"automata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "b", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void correctForAllExpansionTest_10() throws InterruptedException {
		String input = "set N = {a, b, c}. processes Test = (forall [i:N] ([i]:(a -> STOP))).\n" +
				"automata Test.";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "b", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "c", null, null, null));

		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode("||", branches.pop(), expected);
		}

		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
}
