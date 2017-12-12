package mc.compiler.parser;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Created by sheriddavi on 8/02/17.
 */
public class ExampleTests extends ParserTests {

    public ExampleTests() throws InterruptedException {
    }

    @Test
    public void correctSimpleTest_1() throws CompilationException, InterruptedException {
        String input = "automata Simple = (takeTea -> STOP).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"takeTea"}, terminal);
        ProcessNode expected = new ProcessNode("automata", "Simple", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctSimpleTest_2() throws CompilationException, InterruptedException {
        String input = "automata Two = (teaButton -> takeTea -> STOP).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"teaButton", "takeTea"}, terminal);
        ProcessNode expected = new ProcessNode("automata", "Two", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctCoffeeMachineTest() throws CompilationException, InterruptedException {
        String input = "automata CM = (teaButton -> takeTea -> STOP | coffeeButton -> takeCoffee -> STOP).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"teaButton", "takeTea"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coffeeButton", "takeCoffee"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        ProcessNode expected = new ProcessNode("automata", "CM", choice, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctVendingMachineTest_1() throws CompilationException, InterruptedException {
        String input = "automata VM = (coin -> (teaBtn -> tea -> STOP | coffeeBtn -> coffee -> STOP)).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"teaBtn", "tea"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coffeeBtn", "coffee"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        SequenceNode sequence = constructSequenceNode(new String[]{"coin"}, choice);
        ProcessNode expected = new ProcessNode("automata", "VM", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctVendingMachineTest_2() throws CompilationException, InterruptedException {
        String input = "automata VM2 = (coin -> teaBtn -> tea -> STOP | coin -> coffeeBtn -> coffee -> STOP).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"coin", "teaBtn", "tea"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coin", "coffeeBtn", "coffee"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        ProcessNode expected = new ProcessNode("automata", "VM2", choice, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctBasicAbstractionTest() throws CompilationException, InterruptedException {
        List<ProcessNode> nodes = constructProcessList(constructBasicTestInput());

        TerminalNode terminal = new TerminalNode("STOP", null);
        List<LocalProcessNode> localProcesses = new ArrayList<>();

        // Basic
        SequenceNode sequence1 = constructSequenceNode(new String[]{"t", "b"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"c"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, choice);
        ProcessNode basic = new ProcessNode("automata", "Basic", sequence, localProcesses, null);

        // Bas
        HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<>(Collections.singletonList("t")), null), null);
        ProcessRootNode root = new ProcessRootNode(new IdentifierNode("Basic", null), null, null, hiding, null);
        ProcessNode bas = new ProcessNode("automata", "Bas", root, localProcesses, null);

        // B
        FunctionNode function = new FunctionNode("abs",
                Collections.singletonList(new IdentifierNode("Bas", null)), null);
        ProcessNode b = new ProcessNode("automata", "B", function, localProcesses, null);

        List<ProcessNode> expected = new ArrayList<>(Arrays.asList(basic, bas, b));

        if(!expected.equals(nodes)){
            fail("expecting process node lists to be equivalent");
        }
    }

    private String constructBasicTestInput(){
        return "automata {" +
            "Basic = (a -> (t -> b -> STOP | c -> STOP))." +
            "Bas = Basic\\{t}." +
            "B = abs(Bas)." +
            "}";
    }

    @Test
    public void correctSimpleLoopTest_1() throws CompilationException, InterruptedException {
        String input = "automata Tt = (takeTea -> Tt).";
        ProcessNode node = constructProcessNode(input);

        IdentifierNode identifier = new IdentifierNode("Tt", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"takeTea"}, identifier);
        ProcessNode expected = new ProcessNode("automata", "Tt", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctSimpleLoopTest_2() throws CompilationException, InterruptedException {
        String input = "automata BT = (teaButton -> takeTea -> BT).";
        ProcessNode node = constructProcessNode(input);

        IdentifierNode identifier = new IdentifierNode("BT", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"teaButton", "takeTea"}, identifier);
        ProcessNode expected = new ProcessNode("automata", "BT", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctSimpleLocalProcessTest() throws CompilationException, InterruptedException {
        String input = "automata P = (a -> Q), Q = (b -> P | c -> Q).";
        ProcessNode node = constructProcessNode(input);

        IdentifierNode ident = new IdentifierNode("P", null);
        IdentifierNode localIdent = new IdentifierNode("Q", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, localIdent);
        List<LocalProcessNode> localProcesses = new ArrayList<>();
        SequenceNode process1 = constructSequenceNode(new String[]{"b"}, ident);
        SequenceNode process2 = constructSequenceNode(new String[]{"c"}, localIdent);
        ChoiceNode choice = constructChoiceNode(process1, process2);
        localProcesses.add(new LocalProcessNode("Q", null, choice, null));
        ProcessNode expected = new ProcessNode("automata", "P", sequence, localProcesses, null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctTrafficLightTest() throws CompilationException, InterruptedException {
        String input = "automata TrRed = (red -> TrRed | turnGreen -> TrGreen), TrGreen = (green -> TrGreen | turnRed -> TrRed).";
        ProcessNode node = constructProcessNode(input);

        IdentifierNode ident = new IdentifierNode("TrRed", null);
        IdentifierNode localIdent = new IdentifierNode("TrGreen", null);

        SequenceNode process1 = constructSequenceNode(new String[]{"red"}, ident);
        SequenceNode process2 = constructSequenceNode(new String[]{"turnGreen"}, localIdent);
        ChoiceNode choice1 = constructChoiceNode(process1, process2);

        List<LocalProcessNode> localProcesses = new ArrayList<>();
        SequenceNode process3 = constructSequenceNode(new String[]{"green"}, localIdent);
        SequenceNode process4 = constructSequenceNode(new String[]{"turnRed"}, ident);
        ChoiceNode choice2 = constructChoiceNode(process3, process4);
        localProcesses.add(new LocalProcessNode("TrGreen", null, choice2, null));

        ProcessNode expected = new ProcessNode("automata", "TrRed", choice1, localProcesses, null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctParallelTest_1() throws CompilationException, InterruptedException {
        String input = "automata Parallel = ((a -> b -> c -> STOP) || (x -> y -> z -> STOP)).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"a", "b", "c"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"x", "y", "z"}, terminal);
        CompositeNode composite = constructCompositeNode("||", sequence1, sequence2);

        ProcessNode expected = new ProcessNode("automata", "Parallel", composite, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctParallelTest_2() throws CompilationException, InterruptedException {
        String input = "automata Parallel2 = ((a -> m -> c -> STOP) || (x -> m -> z -> STOP))\\{m}.";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"a", "m", "c"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"x", "m", "z"}, terminal);
        HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<>(Collections.singletonList("m")), null), null);
        CompositeNode composite = constructCompositeNode("||", sequence1, sequence2);
        ProcessRootNode root = new ProcessRootNode(composite, null, null, hiding, null);

        ProcessNode expected = new ProcessNode("automata", "Parallel2", root, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctBufferTest() throws CompilationException, InterruptedException {
        List<ProcessNode> nodes = constructProcessList(constructBufferInput());

        List<ProcessNode> expected = new ArrayList<>();
        List<LocalProcessNode> localProcesses = new ArrayList<>();
        IdentifierNode ident = new IdentifierNode("Buff", null);

        // Buff
        SequenceNode sequence = constructSequenceNode(new String[]{"in", "out"}, ident);
        expected.add(new ProcessNode("automata", "Buff", sequence, localProcesses, null));

        // B2
        ProcessRootNode root1 = new ProcessRootNode(ident, "one", null, null, null);
        ProcessRootNode root2 = new ProcessRootNode(ident, "two", null, null, null);
        CompositeNode composite1 = constructCompositeNode("||", root1, root2);
        expected.add(new ProcessNode("automata", "B2", composite1, localProcesses, null));

        // B3
        RelabelElementNode element1 = new RelabelElementNode("move", "one.out", null);
        ProcessRootNode root3 = new ProcessRootNode(ident, "one", new RelabelNode(new ArrayList<>(Collections.singletonList(element1)), null), null, null);
        RelabelElementNode element2 = new RelabelElementNode("move", "two.in", null);
        ProcessRootNode root4 = new ProcessRootNode(ident, "two", new RelabelNode(new ArrayList<>(Collections.singletonList(element2)), null), null, null);
        CompositeNode composite2 = constructCompositeNode("||",root3, root4);
        expected.add(new ProcessNode("automata", "B3", composite2, localProcesses, null));

        // B4
        HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<>(Collections.singletonList("move")), null), null);
        ProcessRootNode root5 = new ProcessRootNode(new IdentifierNode("B3", null), null, null, hiding, null);
        expected.add(new ProcessNode("automata", "B4", root5, localProcesses, null));

        // B5
        FunctionNode function1 = new FunctionNode("abs",
                Collections.singletonList(new IdentifierNode("B4", null)), null);
        expected.add(new ProcessNode("automata", "B5", function1, localProcesses, null));

        // B6
        FunctionNode function2 = new FunctionNode("simp",
                Collections.singletonList(new IdentifierNode("B5", null)), null);
        expected.add(new ProcessNode("automata", "B6", function2, localProcesses, null));

        if(!expected.equals(nodes)){
            fail("expecting process node lists to be equivalent");
        }

    }

    private String constructBufferInput(){
        return "automata {" +
            "Buff = (in -> out -> Buff)." +
            "B2 = (one:Buff || two:Buff)." +
            "B3 = (one:Buff/{move/one.out} || two:Buff/{move/two.in})." +
            "B4 = B3\\{move}." +
            "B5 = abs(B4)." +
            "B6 = simp(B5)." +
            "}";
    }

    @Test
    public void correctMoneyTest() throws CompilationException, InterruptedException {
        String input = "const Coins = 3 automata Money = C[1], C[i:1..Coins] = (when (i < Coins) coin -> C[i + 1] | when (i == Coins) coin -> C[1]).";
        ProcessNode node = constructProcessNode(input);

        List<LocalProcessNode> localProcesses = new ArrayList<>();
        IndexNode index = new IndexNode("$i", new RangeNode(1, 3, null), null, null);
        RangesNode range = new RangesNode(new ArrayList<>(Collections.singletonList(index)), null);
        BoolExpr expr1 = context.mkBVSLT(context.mkBVConst("$i",32), context.mkBV(3,32));
        SequenceNode sequence1 = constructSequenceNode(new String[]{"coin"}, new IdentifierNode("C[$v1]", null));
        IfStatementNode branch1 = new IfStatementNode(expr1, sequence1, null,context);
        BoolExpr expr2 = context.mkEq(context.mkBVConst("$i",32), context.mkBV(3,32));
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coin"}, new IdentifierNode("C[1]", null));
        IfStatementNode branch2 = new IfStatementNode(expr2, sequence2, null,context);
        ChoiceNode choice = constructChoiceNode(branch1, branch2);

        localProcesses.add(new LocalProcessNode("C", range, choice, null));

        ProcessNode expected = new ProcessNode("automata", "Money", new IdentifierNode("C[1]", null), localProcesses, null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctLockTest() throws CompilationException, InterruptedException {
        String input = "const Locks = 2 automata Lock = ([i:1..Locks].setLock -> L[i]), L[j:1..Locks] = ([i:1..Locks].enter -> (when (i == j) open -> close -> L[j] | when (i != j) error -> Lock)).";
        ProcessNode node = constructProcessNode(input);

        SequenceNode mainSequence = constructSequenceNode(new String[]{"[$i].setLock"}, new IdentifierNode("L[$i]", null));
        IndexNode mainIndex = new IndexNode("$i", new RangeNode(1, 2, null), mainSequence, null);

        List<LocalProcessNode> localProcesses = new ArrayList<>();

        BitVecExpr i = context.mkBVConst("$i",32);
        BitVecExpr j = context.mkBVConst("$i",32);
        BoolExpr expr1 = context.mkEq(i, j);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"open", "close"}, new IdentifierNode("L[$j]", null));
        IfStatementNode branch1 = new IfStatementNode(expr1, sequence1, null,context);
        BoolExpr expr2 = context.mkNot(context.mkEq(i, j));
        SequenceNode sequence2 = constructSequenceNode(new String[]{"error"}, new IdentifierNode("Lock", null));
        IfStatementNode branch2 = new IfStatementNode(expr2, sequence2, null,context);
        ChoiceNode choice = constructChoiceNode(branch1, branch2);

        SequenceNode localSequence = constructSequenceNode(new String[]{"[$i].enter"}, choice);
        IndexNode localIndex = new IndexNode("$i", new RangeNode(1, 2, null), localSequence, null);

        RangesNode range = new RangesNode(new ArrayList<>(Collections.singletonList(new IndexNode("$j", new RangeNode(1, 2, null), null, null))), null);
        localProcesses.add(new LocalProcessNode("L", range, localIndex, null));

        ProcessNode expected = new ProcessNode("automata", "Lock", mainIndex, localProcesses, null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctFarmTest() throws CompilationException, InterruptedException {
        List<ProcessNode> nodes = constructProcessList(constructFarmInput());

        List<ProcessNode> expected = new ArrayList<>();
        List<LocalProcessNode> emptyLocal = new ArrayList<>();

        // Worker
        SequenceNode sequence1 = constructSequenceNode(new String[]{"getTask", "doTask"}, new IdentifierNode("Worker", null));
        expected.add(new ProcessNode("automata", "Worker", sequence1, emptyLocal, null));

        // Workers
        IndexNode index = new IndexNode("$i", new RangeNode(1, 3, null), null, null);
        RangesNode range = new RangesNode(new ArrayList<>(Collections.singletonList(index)), null);
        ProcessRootNode root1 = new ProcessRootNode(new IdentifierNode("Worker", null), "[$i]", null, null, null);
        ForAllStatementNode forall = new ForAllStatementNode(range, root1, null);
        expected.add(new ProcessNode("automata", "Workers", forall, emptyLocal, null));

        // Farmer
        List<LocalProcessNode> localProcesses = new ArrayList<>();
        BoolExpr expr1 = context.mkBVSLT(context.mkBVConst("$i",32), context.mkBV(3,32));
        SequenceNode sequence2 = constructSequenceNode(new String[]{"[$i].getTask"}, new IdentifierNode("F[$v1]", null));
        IfStatementNode branch1 = new IfStatementNode(expr1, sequence2, null,context);
        BoolExpr expr2 = context.mkBVSGT(context.mkBVConst("$i",32), context.mkBV(3,32));
        SequenceNode sequence3 = constructSequenceNode(new String[]{"[$i].getTask"}, new IdentifierNode("F[1]", null));
        IfStatementNode branch2 = new IfStatementNode(expr2, sequence3, null,context);
        ChoiceNode choice = constructChoiceNode(branch1, branch2);
        localProcesses.add(new LocalProcessNode("F", range, choice, null));
        expected.add(new ProcessNode("automata", "Farmer", new IdentifierNode("F[1]", null), localProcesses, null));

        // Farm
        CompositeNode composite = constructCompositeNode("||",new IdentifierNode("Farmer", null), new IdentifierNode("Workers", null));
        expected.add(new ProcessNode("automata", "Farm", composite, emptyLocal, null));

        if(!expected.equals(nodes)){
            fail("expecting process node lists to be equivalent");
        }
    }

    private String constructFarmInput(){
        return "const W = 3 " +
            "automata {" +
            "Worker = (getTask -> doTask -> Worker)." +
            "Workers = (forall [i:1..W] ([i]:Worker))." +
            "Farmer = F[1], F[i:1..W] = (when (i < W) [i].getTask -> F[i + 1] | when (i >= W) [i].getTask -> F[1])." +
            "Farm = (Farmer || Workers)." +
            "}";
    }
}
