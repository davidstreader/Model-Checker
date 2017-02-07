package mc.compiler.parser;

import mc.compiler.ast.*;
import org.junit.Test;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 1/02/17.
 */
public class SequenceTests extends ParserTests {

    /**
     * Should be able to parse the input "automata Test = (a -> STOP)." correctly.
     */
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

    /**
     * Should be able to parse the input "automata Test = (a -> ERROR)." correctly.
     */
    @Test
    public void correctSequenceToTerminalTest_2(){
        String input = "automata Test = (a -> ERROR).";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "DEADLOCK"};
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
    public void correctSequenceToTerminalTest_3(){
        String input = "automata Test = (a -> b -> STOP).";
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
    public void correctSequenceToTerminalTest_4(){
        String input = "automata Test = (a -> b -> ERROR).";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "DEADLOCK"};
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
    public void correctSequenceToTerminalTest_5(){
        String input = "automata Test = (a -> b -> c -> STOP).";
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
    public void correctSequenceToTerminalTest_6(){
        String input = "automata Test = (a -> b -> c -> ERROR).";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "DEADLOCK"};
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
    public void correctSequenceToTerminalTest_7(){
        String input = "automata Test = (a -> b -> c -> d -> STOP).";
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
    public void correctSequenceToTerminalTest_8(){
        String input = "automata Test = (a -> b -> c -> d -> ERROR).";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "DEADLOCK"};
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
    public void correctSequenceToTerminalTest_9(){
        String input = "automata Test = (a -> b -> c -> d -> e -> STOP).";
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
    public void correctSequenceToTerminalTest_10(){
        String input = "automata Test = (a -> b -> c -> d -> e -> ERROR).";
        ProcessNode node = constructProcessNode(input);
        String[] sequence = new String[]{"a", "b", "c", "d", "e", "DEADLOCK"};
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
    public void correctSequenceToChoiceTest_1(){
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

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> STOP | x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToChoiceTest_2(){
        String input = "automata Test = (a -> (a -> b -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_3(){
        String input = "automata Test = (a -> (a -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_4(){
        String input = "automata Test = (a -> (a -> b -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_5(){
        String input = "automata Test = (a -> (a -> b -> c -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_6(){
        String input = "automata Test = (a -> (a -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_7(){
        String input = "automata Test = (a -> (a -> b -> c -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_8(){
        String input = "automata Test = (a -> (a -> b -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_9(){
        String input = "automata Test = (a -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_10(){
        String input = "automata Test = (a -> b -> (a -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_11(){
        String input = "automata Test = (a -> b -> (a -> b -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_12(){
        String input = "automata Test = (a -> b -> (a -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_13(){
        String input = "automata Test = (a -> b -> (a -> b -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_14(){
        String input = "automata Test = (a -> b -> (a -> b -> c -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_15(){
        String input = "automata Test = (a -> b -> (a -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_16(){
        String input = "automata Test = (a -> b -> (a -> b -> c -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_17(){
        String input = "automata Test = (a -> b -> (a -> b -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_18(){
        String input = "automata Test = (a -> b -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_19(){
        String input = "automata Test = (a -> b -> c -> (a -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_20(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_21(){
        String input = "automata Test = (a -> b -> c -> (a -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_22(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_23(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> c -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_24(){
        String input = "automata Test = (a -> b -> c -> (a -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_25(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> c -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_26(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_27(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_28(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_29(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_30(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_31(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_32(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_33(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_34(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_35(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_36(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_37(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_38(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_39(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_40(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_41(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP | x -> STOP)).";
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
    public void correctSequenceToChoiceTest_42(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_43(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP | x -> y -> STOP)).";
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
    public void correctSequenceToChoiceTest_44(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToChoiceTest_45(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP | x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_1(){
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

    /**
     * Should be able to parse the input "automata Test = (a -> (a -> b -> STOP || x -> STOP))." correctly.
     */
    @Test
    public void correctSequenceToCompositeTest_2(){
        String input = "automata Test = (a -> (a -> b -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_3(){
        String input = "automata Test = (a -> (a -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_4(){
        String input = "automata Test = (a -> (a -> b -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_5(){
        String input = "automata Test = (a -> (a -> b -> c -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_6(){
        String input = "automata Test = (a -> (a -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_7(){
        String input = "automata Test = (a -> (a -> b -> c -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_8(){
        String input = "automata Test = (a -> (a -> b -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_9(){
        String input = "automata Test = (a -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_10(){
        String input = "automata Test = (a -> b -> (a -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_11(){
        String input = "automata Test = (a -> b -> (a -> b -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_12(){
        String input = "automata Test = (a -> b -> (a -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_13(){
        String input = "automata Test = (a -> b -> (a -> b -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_14(){
        String input = "automata Test = (a -> b -> (a -> b -> c -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_15(){
        String input = "automata Test = (a -> b -> (a -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_16(){
        String input = "automata Test = (a -> b -> (a -> b -> c -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_17(){
        String input = "automata Test = (a -> b -> (a -> b -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_18(){
        String input = "automata Test = (a -> b -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_19(){
        String input = "automata Test = (a -> b -> c -> (a -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_20(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_21(){
        String input = "automata Test = (a -> b -> c -> (a -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_22(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_23(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> c -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_24(){
        String input = "automata Test = (a -> b -> c -> (a -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_25(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> c -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_26(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_27(){
        String input = "automata Test = (a -> b -> c -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_28(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_29(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_30(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_31(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_32(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_33(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_34(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_35(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_36(){
        String input = "automata Test = (a -> b -> c -> d -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_37(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_38(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_39(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_40(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_41(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP || x -> STOP)).";
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
    public void correctSequenceToCompositeTest_42(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_43(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP || x -> y -> STOP)).";
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
    public void correctSequenceToCompositeTest_44(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> STOP || x -> y -> z -> STOP)).";
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
    public void correctSequenceToCompositeTest_45(){
        String input = "automata Test = (a -> b -> c -> d -> e -> (a -> b -> c -> STOP || x -> y -> z -> STOP)).";
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