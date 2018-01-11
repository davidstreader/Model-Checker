package mc.compiler.parser;

import mc.compiler.ast.IndexNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.SequenceNode;
import mc.compiler.ast.TerminalNode;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.fail;

/**
 * Created by sheriddavi on 2/02/17.
 */
public class IndexTests extends ParserTests {

    public IndexTests() throws InterruptedException {
    }

    @Test
    public void correctIndexRangeTest_1() throws CompilationException, InterruptedException {
        String input = "processes Test = ([1..2] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexRangeTest_2() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:1..2] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexRangeTest_3() throws CompilationException, InterruptedException {
        String input = "processes Test = ([1..1 + 1] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexRangeTest_4() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:1..1 + 1] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexRangeTest_5() throws CompilationException, InterruptedException {
        String input = "processes Test = ([2 + 1..5] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 3;
        int end = 5;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexRangeTest_6() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:2 + 1..5] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 3;
        int end = 5;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexRangeTest_7() throws CompilationException, InterruptedException {
        String input = "processes Test = ([2 + 3..11 - 4] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 5;
        int end = 7;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexRangeTest_8() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:2 + 3..11 - 4] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 5;
        int end = 7;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexListTest_1() throws CompilationException, InterruptedException {
        String input = "processes Test = ([{a}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        List<String> set = new ArrayList<>();
        set.add("a");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexListTest_2() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{a}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        List<String> set = new ArrayList<>();
        set.add("a");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexListTest_3() throws CompilationException, InterruptedException {
        String input = "processes Test = ([{a, b}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        List<String> set = new ArrayList<>();
        set.add("a");
        set.add("b");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexListTest_4() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{a, b}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        List<String> set = new ArrayList<>();
        set.add("a");
        set.add("b");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexListTest_5() throws CompilationException, InterruptedException {
        String input = "processes Test = ([{a, b, c}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        List<String> set = new ArrayList<>();
        set.add("a");
        set.add("b");
        set.add("c");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctIndexListTest_6() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:{a, b, c}] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        List<String> set = new ArrayList<>();
        set.add("a");
        set.add("b");
        set.add("c");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }


    @Test
    public void correctMultipleIndexTest_1() throws CompilationException, InterruptedException {
        String input = "processes Test = ([1..2][3..4] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[$v0][$v1]"}, terminal);
        IndexNode index2 = constructIndexNode("$v1", 3, 4, process);
        IndexNode index1 = constructIndexNode("$v0", 1, 2, index2);
        if(!index1.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctMultipleIndexTest_2() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:1..2][3..4] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[$i][$v0]"}, terminal);
        IndexNode index2 = constructIndexNode("$v0", 3, 4, process);
        IndexNode index1 = constructIndexNode("$i", 1, 2, index2);
        if(!index1.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctMultipleIndexTest_3() throws CompilationException, InterruptedException {
        String input = "processes Test = ([1..2][j:3..4] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[$v0][$j]"}, terminal);
        IndexNode index2 = constructIndexNode("$j", 3, 4, process);
        IndexNode index1 = constructIndexNode("$v0", 1, 2, index2);
        if(!index1.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctMultipleIndexTest_4() throws CompilationException, InterruptedException {
        String input = "processes Test = ([i:1..2][j:3..4] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[$i][$j]"}, terminal);
        IndexNode index2 = constructIndexNode("$j", 3, 4, process);
        IndexNode index1 = constructIndexNode("$i", 1, 2, index2);
        if(!index1.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }


    @Test
    public void correctConstIndexRangeTest_1() throws CompilationException, InterruptedException {
        String input = "const N = 1 processes Test = ([N..2] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_2() throws CompilationException, InterruptedException {
        String input = "const N = 1 processes Test = ([i:N..2] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_3() throws CompilationException, InterruptedException {
        String input = "const N = 2 processes Test = ([1..N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_4() throws CompilationException, InterruptedException {
        String input = "const N = 2 processes Test = ([i:1..N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_5() throws CompilationException, InterruptedException {
        String input = "const N = 1 const M = 2 processes Test = ([N..M] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_6() throws CompilationException, InterruptedException {
        String input = "const N = 1 const M = 2 processes Test = ([i:N..M] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_7() throws CompilationException, InterruptedException {
        String input = "const N = 1 processes Test = ([N..1 + 1] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_8() throws CompilationException, InterruptedException {
        String input = "const N = 1 processes Test = ([i:N..1 + 1] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_9() throws CompilationException, InterruptedException {
        String input = "const N = 1 + 1 processes Test = ([1..N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_10() throws CompilationException, InterruptedException {
        String input = "const N = 1 + 1 processes Test = ([i:1..N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 1;
        int end = 2;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_11() throws CompilationException, InterruptedException {
        String input = "const N = 2 + 3 const M = 11 - 4 processes Test = ([N..M] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        int start = 5;
        int end = 7;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexRangeTest_12() throws CompilationException, InterruptedException {
        String input = "const N = 2 + 3 const M = 11 - 4 processes Test = ([i:N..M] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        int start = 5;
        int end = 7;
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, start, end, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexListTest_1() throws CompilationException, InterruptedException {
        String input = "set N = {a}. processes Test = ([N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        List<String> set = new ArrayList<>();
        set.add("a");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexListTest_2() throws CompilationException, InterruptedException {
        String input = "set N = {a}. processes Test = ([i:N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        List<String> set = new ArrayList<>();
        set.add("a");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexListTest_3() throws CompilationException, InterruptedException {
        String input = "set N = {a, b}. processes Test = ([N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        List<String> set = new ArrayList<>();
        set.add("a");
        set.add("b");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexListTest_4() throws CompilationException, InterruptedException {
        String input = "set N = {a, b}. processes Test = ([i:N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        List<String> set = new ArrayList<>();
        set.add("a");
        set.add("b");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexListTest_5() throws CompilationException, InterruptedException {
        String input = "set N = {a, b, c}. processes Test = ([N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$v0";
        List<String> set = new ArrayList<>();
        set.add("a");
        set.add("b");
        set.add("c");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }

    @Test
    public void correctConstIndexListTest_6() throws CompilationException, InterruptedException {
        String input = "set N = {a, b, c}. processes Test = ([i:N] -> STOP).\nautomata Test.";
        ProcessNode node = constructProcessNode(input);
        String variable = "$i";
        List<String> set = new ArrayList<>();
        set.add("a");
        set.add("b");
        set.add("c");
        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode process = constructSequenceNode(new String[]{"[" + variable + "]"}, terminal);
        IndexNode index = constructIndexNode(variable, set, process);
        if(!index.equals(node.getProcess())){
            fail("expecting index nodes to be equivalent");
        }
    }
}
