package mc.compiler.parser;

import org.junit.Test;

public class ProcessTests extends ParserTests {

    public ProcessTests() throws InterruptedException {
    }

    @Test
	public void parseProcessBlockTest_1(){
		String input1 = "processes Test = (a -> STOP).\nautomata Test.";
		String input2 = "processes { Test = (a -> STOP). }";
	}

}
