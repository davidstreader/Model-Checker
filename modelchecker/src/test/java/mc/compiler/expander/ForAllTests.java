package mc.compiler.expander;

import static org.junit.Assert.*;

import java.util.Stack;

import org.junit.Test;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.ProcessRootNode;
import mc.compiler.ast.TerminalNode;

public class ForAllTests extends ExpanderTests {
	
	@Test
	public void correctForAllExpansionTest_1(){
		String input = "automata Test = (forall [i:1..2] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[1]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[2]", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
	
	@Test
	public void correctForAllExpansionTest_2(){
		String input = "automata Test = (forall [i:1..3] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[1]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[2]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[3]", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
	
	@Test
	public void correctForAllExpansionTest_3(){
		String input = "automata Test = (forall [i:{a}] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
	
	@Test
	public void correctForAllExpansionTest_4(){
		String input = "automata Test = (forall [i:{a, b}] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "b", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
	
	@Test
	public void correctForAllExpansionTest_5(){
		String input = "automata Test = (forall [i:{a, b, c}] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "b", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "c", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
	
	@Test
	public void correctForAllExpansionTest_6(){
		String input = "range N = 1..2 automata Test = (forall [i:N] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[1]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[2]", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
	
	@Test
	public void correctForAllExpansionTest_7(){
		String input = "range N = 1..3 automata Test = (forall [i:N] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[1]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[2]", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "[3]", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
	
	@Test
	public void correctForAllExpansionTest_8(){
		String input = "set N = {a} automata Test = (forall [i:N] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
	
	@Test
	public void correctForAllExpansionTest_9(){
		String input = "set N = {a, b} automata Test = (forall [i:N] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "b", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
	
	@Test
	public void correctForAllExpansionTest_10(){
		String input = "set N = {a, b, c} automata Test = (forall [i:N] ([i]:(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		TerminalNode terminal = new TerminalNode("STOP", null);
		Stack<ASTNode> branches = new Stack<ASTNode>();
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "a", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "b", null, null, null));
		branches.push(new ProcessRootNode(constructSequenceNode(new String[]{"a"}, terminal), "c", null, null, null));
		
		ASTNode expected = branches.pop();
		while(!branches.isEmpty()){
			expected = constructCompositeNode(branches.pop(), expected);
		}
		
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
}
