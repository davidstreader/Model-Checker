package mc.compiler.parser;

import org.junit.Test;

public class ProcessTests extends ParserTests {
	
	@Test
	public void parseProcessBlockTest_1(){
		String input1 = "automata Test = (a -> STOP).";
		String input2 = "automata { Test = (a -> STOP). }";
	}
	
}
