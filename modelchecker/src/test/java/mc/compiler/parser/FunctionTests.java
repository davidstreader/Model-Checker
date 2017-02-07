package mc.compiler.parser;

import static org.junit.Assert.*;

import org.junit.Test;

import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.SequenceNode;
import mc.compiler.ast.TerminalNode;

public class FunctionTests extends ParserTests {
	
	@Test
	public void correctAbsTest(){
		String input = "automata Test = abs(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("abs", sequence, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}
	
	@Test
	public void correctSimpTest(){
		String input = "automata Test = simp(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("simp", sequence, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}
	
	@Test
	public void correctSafeTest(){
		String input = "automata Test = safe(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("safe", sequence, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}
	
	@Test
	public void correctPruneTest(){
		String input = "automata Test = prune(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("prune", sequence, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}

	@Test
	public void correctNestedTest_1(){
		String input = "automata Test = simp(abs(a -> STOP)).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode function = new FunctionNode("abs", sequence, null);
		FunctionNode expected = new FunctionNode("simp", function, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}	
	}
	
	@Test
	public void correctNestedTest_2(){
		String input = "automata Test = simp(abs(prune(a -> STOP))).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode function1 = new FunctionNode("prune", sequence, null);
		FunctionNode function2 = new FunctionNode("abs", function1, null);
		FunctionNode expected = new FunctionNode("simp", function2, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}	
	}
	
	@Test
	public void correctNestedTest_3(){
		String input = "petrinet Test = safe(simp(abs(prune(a -> STOP)))).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode function1 = new FunctionNode("prune", sequence, null);
		FunctionNode function2 = new FunctionNode("abs", function1, null);
		FunctionNode function3 = new FunctionNode("simp", function2, null);
		FunctionNode expected = new FunctionNode("safe", function3, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}	
	}
	
	@Test
	public void correctAutomataCastTest(){
		String input = "automata Test = automata(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("automata", sequence, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}
	
	@Test
	public void correctPetriNetCastTest(){
		String input = "petrinet Test = petrinet(a -> STOP).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode expected = new FunctionNode("petrinet", sequence, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}
	
	@Test
	public void correctMixedCastTest_1(){
		String input = "automata Test = automata(petrinet(a -> STOP)).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode cast = new FunctionNode("petrinet", sequence, null);
		FunctionNode expected = new FunctionNode("automata", cast, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}
	
	@Test
	public void correctMixedCastTest_2(){
		String input = "petrinet Test = petrinet(automata(a -> STOP)).";
		ProcessNode node = constructProcessNode(input);
		SequenceNode sequence = constructSequenceNode(new String[]{"a"}, new TerminalNode("STOP", null));
		FunctionNode cast = new FunctionNode("automata", sequence, null);
		FunctionNode expected = new FunctionNode("petrinet", cast, null);
		if(!expected.equals(node.getProcess())){
			fail("expecting function nodes to be equivalent");
		}
	}
}
