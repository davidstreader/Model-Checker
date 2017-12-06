package mc.compiler.parser;

import mc.Constant;
import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import org.junit.Test;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 1/02/17.
 */
public class SequenceTests extends ParserTests {

    public SequenceTests() throws InterruptedException {
    }

    /**
     * Should be able to parse the input "automata Test = (a -> STOP)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_1() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> ERROR)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_2() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> ERROR).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", Constant.DEADLOCK};
        TerminalNode terminal = new TerminalNode("ERROR", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> STOP)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_3() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> ERROR)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_4() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> ERROR).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", Constant.DEADLOCK};
        TerminalNode terminal = new TerminalNode("ERROR", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> STOP)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_5() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> ERROR)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_6() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> ERROR).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", Constant.DEADLOCK};
        TerminalNode terminal = new TerminalNode("ERROR", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> STOP)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_7() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> ERROR)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_8() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> ERROR).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", Constant.DEADLOCK};
        TerminalNode terminal = new TerminalNode("ERROR", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> STOP)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_9() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> ERROR)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_10() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> ERROR).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e", Constant.DEADLOCK};
        TerminalNode terminal = new TerminalNode("ERROR", null);
        SequenceNode expected = constructSequenceNode(sequence, terminal);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_1() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> STOP | x -> STOP)).\nautomata Test.";
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

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_2() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_3() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_4() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> c -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_5() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> c -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_6() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> c -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_7() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> c -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_8() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> c -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_9() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_10() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_11() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_12() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_13() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> c -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_14() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> c -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_15() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> c -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_16() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> c -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_17() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> c -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_18() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_19() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_20() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_21() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_22() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> c -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_23() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> c -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_24() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> c -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_25() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> c -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_26() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> c -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_27() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_28() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_29() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_30() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_31() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_32() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> c -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_33() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_34() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> c -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_35() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_36() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_37() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_38() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_39() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_40() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_41() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP | x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_42() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP | x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_43() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP | x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_44() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP | x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_45() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        ChoiceNode base = constructChoiceNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_1() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> STOP || x -> STOP)).\nautomata Test.";
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

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_2() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_3() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_4() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> c -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_5() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> c -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_6() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> c -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_7() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> c -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_8() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> c -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_9() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_10() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_11() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_12() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_13() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> c -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_14() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> c -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_15() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> c -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_16() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> c -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_17() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> (a -> b -> c -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_18() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_19() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_20() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_21() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_22() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> c -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_23() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> c -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_24() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> c -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_25() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> c -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_26() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> (a -> b -> c -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_27() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_28() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_29() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_30() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_31() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_32() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> c -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_33() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_34() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> c -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_35() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_36() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_37() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_38() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_39() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_40() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_41() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP || x -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_42() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP || x -> y -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_43() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP || x -> y -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_44() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }

    /**
     * Should be able to parse the input "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP || x -> y -> z -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_45() throws CompilationException, InterruptedException {
        String input = "processes Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e"};
        String[] sequence1 = new String[]{"a", "b", "c"};
        String[] sequence2 = new String[]{"x", "y", "z"};
        CompositeNode base = constructCompositeNode(sequence1, sequence2);
        SequenceNode expected = constructSequenceNode(sequence, base);
        if(!expected.equals(node.getProcess())){
            fail("expecting the sequence nodes to be equivalent");
        }
    }
}
