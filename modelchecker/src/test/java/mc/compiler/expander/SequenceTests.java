package mc.compiler.expander;

import mc.Constant;
import mc.compiler.ast.*;
import org.junit.Test;

import java.util.Stack;

import static junit.framework.TestCase.fail;

public class SequenceTests extends ExpanderTests {

    @Test
    public void correctSequenceToTerminalTest_1(){
        String input = "automata Test = (a -> STOP).";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    @Test
    public void correctSequenceToTerminalTest_2(){
        String input = "automata Test = (a -> ERROR).";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", Constant.DEADLOCK};
        TerminalNode terminal = new TerminalNode("ERROR", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    @Test
    public void correctSequenceToChoiceTest(){
        String input = "automata Test = (a -> (a -> STOP | x -> STOP)).";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    @Test
    public void correctSequenceToCompositeTest(){
        String input = "automata Test = (a -> (a -> STOP || x -> STOP)).";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexSequenceTest_1(){
    	String input = "automata Test = ([1..2] -> [3..4] -> STOP).";
    	ProcessNode node = constructProcessNode(input);
    	ChoiceNode choice = constructChoiceNode(new String[]{"[3]"}, new String[]{"[4]"});
    	Stack<ASTNode> branches = new Stack<ASTNode>();
    	branches.push(constructSequenceNode(new String[]{"[1]"}, choice));
    	branches.push(constructSequenceNode(new String[]{"[2]"}, choice));

    	ASTNode expected = branches.pop();
    	while(!branches.isEmpty()){
    		expected = constructChoiceNode(branches.pop(), expected);
    	}

    	if(!expected.equals(node.getProcess())){
    		fail("expecting choice nodes to be equivalent");
    	}
    }

    @Test
    public void correctIndexSequenceTest_2(){
    	String input = "automata Test = ([1..2] -> [{a, b}] -> STOP).";
    	ProcessNode node = constructProcessNode(input);
    	ChoiceNode choice = constructChoiceNode(new String[]{"a"}, new String[]{"b"});
    	Stack<ASTNode> branches = new Stack<ASTNode>();
    	branches.push(constructSequenceNode(new String[]{"[1]"}, choice));
    	branches.push(constructSequenceNode(new String[]{"[2]"}, choice));

    	ASTNode expected = branches.pop();
    	while(!branches.isEmpty()){
    		expected = constructChoiceNode(branches.pop(), expected);
    	}

    	if(!expected.equals(node.getProcess())){
    		fail("expecting choice nodes to be equivalent");
    	}
    }

    @Test
    public void correctIndexSequenceTest_3(){
    	String input = "automata Test = ([{a, b}] -> [3..4] -> STOP).";
    	ProcessNode node = constructProcessNode(input);
    	ChoiceNode choice = constructChoiceNode(new String[]{"[3]"}, new String[]{"[4]"});
    	Stack<ASTNode> branches = new Stack<ASTNode>();
    	branches.push(constructSequenceNode(new String[]{"a"}, choice));
    	branches.push(constructSequenceNode(new String[]{"b"}, choice));

    	ASTNode expected = branches.pop();
    	while(!branches.isEmpty()){
    		expected = constructChoiceNode(branches.pop(), expected);
    	}

    	if(!expected.equals(node.getProcess())){
    		fail("expecting choice nodes to be equivalent");
    	}
    }

    @Test
    public void correctIndexSequenceTest_4(){
    	String input = "automata Test = ([{a, b}] -> [{c, d}] -> STOP).";
    	ProcessNode node = constructProcessNode(input);
    	ChoiceNode choice = constructChoiceNode(new String[]{"c"}, new String[]{"d"});
    	Stack<ASTNode> branches = new Stack<ASTNode>();
    	branches.push(constructSequenceNode(new String[]{"a"}, choice));
    	branches.push(constructSequenceNode(new String[]{"b"}, choice));

    	ASTNode expected = branches.pop();
    	while(!branches.isEmpty()){
    		expected = constructChoiceNode(branches.pop(), expected);
    	}

    	if(!expected.equals(node.getProcess())){
    		fail("expecting choice nodes to be equivalent");
    	}
    }
}
