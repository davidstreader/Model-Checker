package mc.compiler.parser;

import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import org.junit.Test;

import static org.junit.Assert.fail;

public class IdentifierTests extends ParserTests {

    public IdentifierTests() throws InterruptedException {
    }

    @Test
	public void basicIdentifierTest() throws CompilationException, InterruptedException {
		String input = "automata Test = (a -> STOP). automata Test2 = Test.";
		ProcessNode node = constructProcessNode(input, 1);
		IdentifierNode expected = new IdentifierNode("Test", null);
		if(!expected.equals(node.getProcess())){
			fail("expecting identifer nodes to be equilvalent");
		}
	}

	@Test
	public void mixedIdentifierTest_1() throws CompilationException, InterruptedException {
		String input = "automata Test1 = (a -> STOP). automata Test2 = (Test1 | b -> STOP).";
		ProcessNode node = constructProcessNode(input, 1);
		IdentifierNode identifier = new IdentifierNode("Test1", null);
		SequenceNode sequence = constructSequenceNode(new String[]{"b"}, new TerminalNode("STOP", null));
		ChoiceNode expected = new ChoiceNode(identifier, sequence, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equilvalent");
		}
	}

	@Test
	public void mixedIdentifierTest_2() throws CompilationException, InterruptedException {
		String input = "automata Test1 = (b -> STOP). automata Test2 = (a -> STOP | Test1).";
		ProcessNode node = constructProcessNode(input, 1);
		IdentifierNode identifier = new IdentifierNode("Test1", null);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		ChoiceNode expected = new ChoiceNode(sequence, identifier, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equilvalent");
		}
	}

	@Test
	public void mixedIdentifierTest_3() throws CompilationException, InterruptedException {
		String input = "automata Test1 = (a -> STOP). automata Test2 = (b -> STOP). automata Test3 = (Test1 | Test2).";
		ProcessNode node = constructProcessNode(input, 2);
		IdentifierNode identifier1 = new IdentifierNode("Test1", null);
		IdentifierNode identifier2 = new IdentifierNode("Test2", null);
		ChoiceNode expected = new ChoiceNode(identifier1, identifier2, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting choice nodes to be equilvalent");
		}
	}

	@Test
	public void mixedIdentifierTest_4() throws CompilationException, InterruptedException {
		String input = "automata Test1 = (a -> STOP). automata Test2 = (Test1 || b -> STOP).";
		ProcessNode node = constructProcessNode(input, 1);
		IdentifierNode identifier = new IdentifierNode("Test1", null);
		SequenceNode sequence = constructSequenceNode(new String[]{"b"}, new TerminalNode("STOP", null));
		CompositeNode expected = new CompositeNode(identifier, sequence, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void mixedIdentifierTest_5() throws CompilationException, InterruptedException {
		String input = "automata Test1 = (b -> STOP). automata Test2 = (a -> STOP || Test1).";
		ProcessNode node = constructProcessNode(input, 1);
		IdentifierNode identifier = new IdentifierNode("Test1", null);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		CompositeNode expected = new CompositeNode(sequence, identifier, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}

	@Test
	public void mixedIdentifierTest_6() throws CompilationException, InterruptedException {
		String input = "automata Test1 = (a -> STOP). automata Test2 = (b -> STOP). automata Test3 = (Test1 || Test2).";
		ProcessNode node = constructProcessNode(input, 2);
		IdentifierNode identifier1 = new IdentifierNode("Test1", null);
		IdentifierNode identifier2 = new IdentifierNode("Test2", null);
		CompositeNode expected = new CompositeNode(identifier1, identifier2, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting composite nodes to be equilvalent");
		}
	}
}
