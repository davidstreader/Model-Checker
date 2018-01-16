package mc.compiler.parser;

import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.SequenceNode;
import mc.compiler.ast.TerminalNode;
import mc.exceptions.CompilationException;
import mc.plugins.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.fail;

public class FunctionTests extends ParserTests {

	@BeforeClass
	public static void initialise(){
		PluginManager.getInstance().registerPlugins();
	}

	public FunctionTests() throws InterruptedException {
	}

	@Test
	public void correctAbsTest() throws CompilationException, InterruptedException {
		String input = "processes Test = abs(a -> STOP).\nautomata Test.";
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
		String input = "processes Test = simp(a -> STOP).\nautomata Test.";
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
		String input = "processes Test = safe(a -> STOP).\nautomata Test.";
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
		String input = "processes Test = prune(a -> STOP).\nautomata Test.";
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
		String input = "processes Test = simp(abs(a -> STOP)).\nautomata Test.";
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
		String input = "processes Test = simp(abs(prune(a -> STOP))).\nautomata Test.";
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
		String input = "processes Test = safe(simp(abs(prune(a -> STOP)))).\npetrinet Test.";
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

}
