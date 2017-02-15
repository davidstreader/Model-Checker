package mc.compiler.reference_replacer;

import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Created by sheriddavi on 15/02/17.
 */
public class ExampleTests extends ReferenceReplacerTests {

    @Test
    public void correctSimpleTest_1() throws CompilationException {
        String input = "automata Simple = (takeTea -> STOP).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"takeTea"}, terminal);
        sequence.addReference("Simple");
        ProcessNode expected = new ProcessNode("automata", "Simple", sequence, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctSimpleTest_2() throws CompilationException {
        String input = "automata Two = (teaButton -> takeTea -> STOP).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"teaButton", "takeTea"}, terminal);
        sequence.addReference("Two");
        ProcessNode expected = new ProcessNode("automata", "Two", sequence, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctCoffeeMachineTest() throws CompilationException {
        String input = "automata CM = (teaButton -> takeTea -> STOP | coffeeButton -> takeCoffee -> STOP).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"teaButton", "takeTea"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coffeeButton", "takeCoffee"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        choice.addReference("CM");
        ProcessNode expected = new ProcessNode("automata", "CM", choice, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctVendingMachineTest_1() throws CompilationException {
        String input = "automata VM = (coin -> (teaBtn -> tea -> STOP | coffeeBtn -> coffee -> STOP)).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"teaBtn", "tea"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coffeeBtn", "coffee"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        SequenceNode sequence = constructSequenceNode(new String[]{"coin"}, choice);
        sequence.addReference("VM");
        ProcessNode expected = new ProcessNode("automata", "VM", sequence, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctVendingMachineTest_2() throws CompilationException {
        String input = "automata VM2 = (coin -> teaBtn -> tea -> STOP | coin -> coffeeBtn -> coffee -> STOP).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"coin", "teaBtn", "tea"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coin", "coffeeBtn", "coffee"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        choice.addReference("VM2");
        ProcessNode expected = new ProcessNode("automata", "VM2", choice, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctBasicAbstractionTest() throws CompilationException {
        List<ProcessNode> nodes = constructProcessList(constructBasicTestInput());

        TerminalNode terminal = new TerminalNode("STOP", null);
        List<LocalProcessNode> localProcesses = new ArrayList<LocalProcessNode>();

        // Basic
        SequenceNode sequence1 = constructSequenceNode(new String[]{"t", "b"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"c"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, choice);
        sequence.addReference("Basic");
        ProcessNode basic = new ProcessNode("automata", "Basic", sequence, localProcesses, null);

        // Bas
        HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<String>(Arrays.asList("t")), null), null);
        IdentifierNode basIdent = new IdentifierNode("Basic", null);
        basIdent.addReference("Bas");
        ProcessRootNode root = new ProcessRootNode(basIdent, null, null, hiding, null);
        ProcessNode bas = new ProcessNode("automata", "Bas", root, localProcesses, null);

        // B
        FunctionNode function = new FunctionNode("abs", new IdentifierNode("Bas", null), null);
        function.addReference("B");
        ProcessNode b = new ProcessNode("automata", "B", function, localProcesses, null);

        List<ProcessNode> expected = new ArrayList<ProcessNode>(Arrays.asList(basic, bas, b));

        if(!expected.equals(nodes)){
            fail("expecting process node lists to be equivalent");
        }
    }

    private String constructBasicTestInput(){
        StringBuilder builder = new StringBuilder();
        builder.append("automata {");
        builder.append("Basic = (a -> (t -> b -> STOP | c -> STOP)).");
        builder.append("Bas = Basic\\{t}.");
        builder.append("B = abs(Bas).");
        builder.append("}");
        return builder.toString();
    }

    @Test
    public void correctSimpleLoopTest_1() throws CompilationException {
        String input = "automata Tt = (takeTea -> Tt).";
        ProcessNode node = constructProcessNode(input);

        ReferenceNode reference = new ReferenceNode("Tt", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"takeTea"}, reference);
        sequence.addReference("Tt");
        ProcessNode expected = new ProcessNode("automata", "Tt", sequence, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctSimpleLoopTest_2() throws CompilationException {
        String input = "automata BT = (teaButton -> takeTea -> BT).";
        ProcessNode node = constructProcessNode(input);

        ReferenceNode reference = new ReferenceNode("BT", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"teaButton", "takeTea"}, reference);
        sequence.addReference("BT");
        ProcessNode expected = new ProcessNode("automata", "BT", sequence, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctSimpleLocalProcessTest() throws CompilationException {
        String input = "automata P = (a -> Q), Q = (b -> P | c -> Q).";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence1 = constructSequenceNode(new String[]{"b"}, new ReferenceNode("P", null));
        SequenceNode sequence2 = constructSequenceNode(new String[]{"c"}, new ReferenceNode("P.Q", null));
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        choice.addReference("P.Q");
        SequenceNode sequence3 = constructSequenceNode(new String[]{"a"}, choice);
        sequence3.addReference("P");

        ProcessNode expected = new ProcessNode("automata", "P", sequence3, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctTrafficLightTest() throws CompilationException {
        String input = "automata TrRed = (red -> TrRed | turnGreen -> TrGreen), TrGreen = (green -> TrGreen | turnRed -> TrRed).";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence1 = constructSequenceNode(new String[]{"red"}, new ReferenceNode("TrRed", null));
        SequenceNode sequence2 = constructSequenceNode(new String[]{"green"}, new ReferenceNode("TrRed.TrGreen", null));
        SequenceNode sequence3 = constructSequenceNode(new String[]{"turnRed"}, new ReferenceNode("TrRed", null));
        ChoiceNode choice1 = constructChoiceNode(sequence2, sequence3);
        choice1.addReference("TrRed.TrGreen");
        SequenceNode sequence4 = constructSequenceNode(new String[]{"turnGreen"}, choice1);
        ChoiceNode choice2 = constructChoiceNode(sequence1, sequence4);
        choice2.addReference("TrRed");

        ProcessNode expected = new ProcessNode("automata", "TrRed", choice2, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctParallelTest_1() throws CompilationException {
        String input = "automata Parallel = ((a -> b -> c -> STOP) || (x -> y -> z -> STOP)).";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"a", "b", "c"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"x", "y", "z"}, terminal);
        CompositeNode composite = constructCompositeNode(sequence1, sequence2);
        composite.addReference("Parallel");

        ProcessNode expected = new ProcessNode("automata", "Parallel", composite, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctParallelTest_2() throws CompilationException {
        String input = "automata Parallel2 = ((a -> m -> c -> STOP) || (x -> m -> z -> STOP))\\{m}.";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"a", "m", "c"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"x", "m", "z"}, terminal);
        HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<String>(Arrays.asList("m")), null), null);
        CompositeNode composite = constructCompositeNode(sequence1, sequence2);
        composite.addReference("Parallel2");
        ProcessRootNode root = new ProcessRootNode(composite, null, null, hiding, null);

        ProcessNode expected = new ProcessNode("automata", "Parallel2", root, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctBufferTest() throws CompilationException {
        List<ProcessNode> nodes = constructProcessList(constructBufferInput());

        List<ProcessNode> expected = new ArrayList<ProcessNode>();
        List<LocalProcessNode> localProcesses = new ArrayList<LocalProcessNode>();

        // Buff
        SequenceNode sequence = constructSequenceNode(new String[]{"in", "out"}, new ReferenceNode("Buff", null));
        sequence.addReference("Buff");
        expected.add(new ProcessNode("automata", "Buff", sequence, localProcesses, null));

        // B2
        ProcessRootNode root1 = new ProcessRootNode(new IdentifierNode("Buff", null), "one", null, null, null);
        ProcessRootNode root2 = new ProcessRootNode(new IdentifierNode("Buff", null), "two", null, null, null);
        CompositeNode composite1 = constructCompositeNode(root1, root2);
        composite1.addReference("B2");
        expected.add(new ProcessNode("automata", "B2", composite1, localProcesses, null));

        // B3
        RelabelElementNode element1 = new RelabelElementNode("move", "one.out", null);
        ProcessRootNode root3 = new ProcessRootNode(new IdentifierNode("Buff", null), "one", new RelabelNode(new ArrayList<RelabelElementNode>(Arrays.asList(element1)), null), null, null);
        RelabelElementNode element2 = new RelabelElementNode("move", "two.in", null);
        ProcessRootNode root4 = new ProcessRootNode(new IdentifierNode("Buff", null), "two", new RelabelNode(new ArrayList<RelabelElementNode>(Arrays.asList(element2)), null), null, null);
        CompositeNode composite2 = constructCompositeNode(root3, root4);
        composite2.addReference("B3");
        expected.add(new ProcessNode("automata", "B3", composite2, localProcesses, null));

        // B4
        HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<String>(Arrays.asList("move")), null), null);
        IdentifierNode ident = new IdentifierNode("B3", null);
        ident.addReference("B4");
        ProcessRootNode root5 = new ProcessRootNode(ident, null, null, hiding, null);
        expected.add(new ProcessNode("automata", "B4", root5, localProcesses, null));

        // B5
        FunctionNode function1 = new FunctionNode("abs", new IdentifierNode("B4", null), null);
        function1.addReference("B5");
        expected.add(new ProcessNode("automata", "B5", function1, localProcesses, null));

        // B6
        FunctionNode function2 = new FunctionNode("simp", new IdentifierNode("B5", null), null);
        expected.add(new ProcessNode("automata", "B6", function2, localProcesses, null));
        function2.addReference("B6");

        if(!expected.equals(nodes)){
            fail("expecting process node lists to be equivalent");
        }

    }

    private String constructBufferInput(){
        StringBuilder builder = new StringBuilder();
        builder.append("automata {");
        builder.append("Buff = (in -> out -> Buff).");
        builder.append("B2 = (one:Buff || two:Buff).");
        builder.append("B3 = (one:Buff/{move/one.out} || two:Buff/{move/two.in}).");
        builder.append("B4 = B3\\{move}.");
        builder.append("B5 = abs(B4).");
        builder.append("B6 = simp(B5).");
        builder.append("}");
        return builder.toString();
    }

    @Test
    public void correctMoneyTest() throws CompilationException {
        String input = "const Coins = 3 automata Money = C[1], C[i:1..Coins] = (when (i < Coins) coin -> C[i + 1] | when (i == Coins) coin -> C[1]).";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence1 = constructSequenceNode(new String[]{"coin"}, new ReferenceNode("Money.C[1]", null));
        sequence1.addReference("Money.C[3]");
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coin"}, sequence1);
        sequence2.addReference("Money.C[2]");
        SequenceNode sequence3 = constructSequenceNode(new String[]{"coin"}, sequence2);
        sequence3.addReference("Money.C[1]");
        sequence3.addReference("Money");

        ProcessNode expected = new ProcessNode("automata", "Money", sequence3, new ArrayList<LocalProcessNode>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctLockTest() throws CompilationException {
        String input = "const Locks = 2 automata Lock = ([i:1..Locks].setLock -> L[i]), L[j:1..Locks] = ([i:1..Locks].enter -> (when (i == j) open -> close -> L[j] | when (i != j) error -> Lock)).";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence1 = constructSequenceNode(new String[]{"[1].enter", "open", "close"}, new ReferenceNode("Lock.L[1]", null));
        SequenceNode sequence2 = constructSequenceNode(new String[]{"[2].enter", "error"}, new ReferenceNode("Lock", null));
        ChoiceNode choice1 = constructChoiceNode(sequence1, sequence2);
        SequenceNode sequence3 = constructSequenceNode(new String[]{"[1].setLock"}, choice1);
        choice1.addReference("Lock.L[1]");

        SequenceNode sequence4 = constructSequenceNode(new String[]{"[1].enter", "error"}, new ReferenceNode("Lock", null));
        SequenceNode sequence5 = constructSequenceNode(new String[]{"[2].enter", "open", "close"}, new ReferenceNode("Lock.L[2]", null));
        ChoiceNode choice2 = constructChoiceNode(sequence4, sequence5);
        choice2.addReference("Lock.L[2]");
        SequenceNode sequence6 = constructSequenceNode(new String[]{"[2].setLock"}, choice2);

        ChoiceNode choice3 = constructChoiceNode(sequence3, sequence6);
        ProcessNode expected = new ProcessNode("automata", "Lock", choice3, new ArrayList<LocalProcessNode>(), null);
        choice3.addReference("Lock");
        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctFarmTest() throws CompilationException {
        List<ProcessNode> nodes = constructProcessList(constructFarmInput());

        List<ProcessNode> expected = new ArrayList<ProcessNode>();
        List<LocalProcessNode> emptyLocal = new ArrayList<LocalProcessNode>();

        // Worker
        SequenceNode sequence1 = constructSequenceNode(new String[]{"getTask", "doTask"}, new ReferenceNode("Worker", null));
        sequence1.addReference("Worker");
        expected.add(new ProcessNode("automata", "Worker", sequence1, emptyLocal, null));

        // Workers
        ProcessRootNode root1 = new ProcessRootNode(new IdentifierNode("Worker", null), "[1]", null, null, null);
        ProcessRootNode root2 = new ProcessRootNode(new IdentifierNode("Worker", null), "[2]", null, null, null);
        ProcessRootNode root3 = new ProcessRootNode(new IdentifierNode("Worker", null), "[3]", null, null, null);
        CompositeNode composite = constructCompositeNode(root1, constructCompositeNode(root2, root3));
        composite.addReference("Workers");
        expected.add(new ProcessNode("automata", "Workers", composite, emptyLocal, null));

        // Farmer
        SequenceNode sequence2 = constructSequenceNode(new String[]{"[3].getTask"}, new ReferenceNode("Farmer.F[1]", null));
        sequence2.addReference("Farmer.F[3]");
        SequenceNode sequence3 = constructSequenceNode(new String[]{"[2].getTask"}, sequence2);
        sequence3.addReference("Farmer.F[2]");
        SequenceNode sequence4 = constructSequenceNode(new String[]{"[1].getTask"}, sequence3);
        sequence4.addReference("Farmer.F[1]");
        sequence4.addReference("Farmer");
        expected.add(new ProcessNode("automata", "Farmer", sequence4, emptyLocal, null));

        // Farm
        CompositeNode composite2 = constructCompositeNode(new IdentifierNode("Farmer", null), new IdentifierNode("Workers", null));
        composite2.addReference("Farm");
        expected.add(new ProcessNode("automata", "Farm", composite2, emptyLocal, null));


        if(!expected.equals(nodes)){
            fail("expecting process node lists to be equivalent");
        }
    }

    private String constructFarmInput(){
        StringBuilder builder = new StringBuilder();
        builder.append("const W = 3 ");
        builder.append("automata {");
        builder.append("Worker = (getTask -> doTask -> Worker).");
        builder.append("Workers = (forall [i:1..W] ([i]:Worker)).");
        builder.append("Farmer = F[1], F[i:1..W] = (when (i < W) [i].getTask -> F[i + 1] | when (i >= W) [i].getTask -> F[1]).");
        builder.append("Farm = (Farmer || Workers).");
        builder.append("}");
        return builder.toString();
    }

}
