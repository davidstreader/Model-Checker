package mc.compiler.parser;

import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.SequenceNode;
import mc.compiler.ast.TerminalNode;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.fail;

public class FunctionTests extends ParserTests {

	public FunctionTests() throws InterruptedException {
	}

	@Test
	public void correctAbsTest() throws CompilationException, InterruptedException {
		String input = "automata Test = abs(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("abs",
				Collections.singletonList(sequence), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctSimpTest() throws CompilationException, InterruptedException {
		String input = "automata Test = simp(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("simp",
				Collections.singletonList(sequence), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctSafeTest() throws CompilationException, InterruptedException {
		String input = "automata Test = safe(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("safe",
				Collections.singletonList(sequence), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctPruneTest() throws CompilationException, InterruptedException {
		String input = "automata Test = prune(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("prune",
				Collections.singletonList(sequence), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctNestedTest_1() throws CompilationException, InterruptedException {
		String input = "automata Test = simp(abs(a -> STOP)).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode function = new FunctionNode("abs",
				Collections.singletonList(sequence), null);
		FunctionNode expected = new FunctionNode("simp",
				Collections.singletonList(function), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctNestedTest_2() throws CompilationException, InterruptedException {
		String input = "automata Test = simp(abs(prune(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode function1 = new FunctionNode("prune",
				Collections.singletonList(sequence), null);
		FunctionNode function2 = new FunctionNode("abs",
				Collections.singletonList(function1), null);
		FunctionNode expected = new FunctionNode("simp",
				Collections.singletonList(function2), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctNestedTest_3() throws CompilationException, InterruptedException {
		String input = "petrinet Test = safe(simp(abs(prune(a -> STOP)))).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode function1 = new FunctionNode("prune",
				Collections.singletonList(sequence), null);
		FunctionNode function2 = new FunctionNode("abs",
				Collections.singletonList(function1), null);
		FunctionNode function3 = new FunctionNode("simp",
				Collections.singletonList(function2), null);
		FunctionNode expected = new FunctionNode("safe",
				Collections.singletonList(function3), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctAutomataCastTest() throws CompilationException, InterruptedException {
		String input = "automata Test = automata(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("automata",
				Collections.singletonList(sequence), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctPetriNetCastTest() throws CompilationException, InterruptedException {
		String input = "petrinet Test = petrinet(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("petrinet",
				Collections.singletonList(sequence), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctMixedCastTest_1() throws CompilationException, InterruptedException {
		String input = "automata Test = automata(petrinet(a -> STOP)).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode cast = new FunctionNode("petrinet",
				Collections.singletonList(sequence), null);
		FunctionNode expected = new FunctionNode("automata",
				Collections.singletonList(cast), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctMixedCastTest_2() throws CompilationException, InterruptedException {
		String input = "petrinet Test = petrinet(automata(a -> STOP)).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode cast = new FunctionNode("automata",
				Collections.singletonList(sequence), null);
		FunctionNode expected = new FunctionNode("petrinet",
				Collections.singletonList(cast), null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}
}
