package mc.compiler.reference_replacer;

import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import mc.plugins.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by sheriddavi on 15/02/17.
 */
public class ExampleTests extends ReferenceReplacerTests {

    @BeforeClass
    public static void initialise(){
        PluginManager.getInstance().registerPlugins();
    }

    public ExampleTests() throws InterruptedException {

    }

    @Test
    public void correctSimpleTestExample_1() throws CompilationException, InterruptedException {
        String input =  "processes Simple = (takeTea -> STOP).\n" +
                        "automata Simple.";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"takeTea"}, terminal);
        sequence.addReference("Simple");
        ProcessNode expected = new ProcessNode("automata", "Simple", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctSimpleTestExample_2() throws CompilationException, InterruptedException {
        String input = "processes Two = (teaButton -> takeTea -> STOP).\n" +
                       "automata Two.";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"teaButton", "takeTea"}, terminal);
        sequence.addReference("Two");
        ProcessNode expected = new ProcessNode("automata", "Two", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctCoffeeMachineTestExample() throws CompilationException, InterruptedException {
        String input = "processes CM = (teaButton -> takeTea -> STOP | coffeeButton -> takeCoffee -> STOP).\nautomata CM.";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"teaButton", "takeTea"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coffeeButton", "takeCoffee"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        choice.addReference("CM");
        ProcessNode expected = new ProcessNode("automata", "CM", choice, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctVendingMachineTestExample_1() throws CompilationException, InterruptedException {
        String input = "processes VM = (coin -> (teaBtn -> tea -> STOP | coffeeBtn -> coffee -> STOP)).\nautomata VM.";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"teaBtn", "tea"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coffeeBtn", "coffee"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        SequenceNode sequence = constructSequenceNode(new String[]{"coin"}, choice);
        sequence.addReference("VM");
        ProcessNode expected = new ProcessNode("automata", "VM", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctVendingMachineTestExample_2() throws CompilationException, InterruptedException {
        String input = "processes VM2 = (coin -> teaBtn -> tea -> STOP | coin -> coffeeBtn -> coffee -> STOP).\nautomata VM2.";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"coin", "teaBtn", "tea"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coin", "coffeeBtn", "coffee"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        choice.addReference("VM2");
        ProcessNode expected = new ProcessNode("automata", "VM2", choice, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctBasicAbstractionTestExample() throws CompilationException, InterruptedException {
        List<ProcessNode> nodes = constructProcessList(constructBasicTestInput());

        TerminalNode terminal = new TerminalNode("STOP", null);
        List<LocalProcessNode> localProcesses = new ArrayList<>();

        // Basic
        SequenceNode sequence1 = constructSequenceNode(new String[]{"t", "b"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"c"}, terminal);
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        SequenceNode sequence = constructSequenceNode(new String[]{"a"}, choice);
        sequence.addReference("Basic");
        ProcessNode basic = new ProcessNode("automata", "Basic", sequence, localProcesses, null);

        // Bas
        HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<>(Collections.singletonList("t")), null), null);
        IdentifierNode basIdent = new IdentifierNode("Basic", null);
        basIdent.addReference("Bas");
        ProcessRootNode root = new ProcessRootNode(basIdent, null, null, hiding, null);
        ProcessNode bas = new ProcessNode("automata", "Bas", root, localProcesses, null);

        // B
        FunctionNode function = new FunctionNode("abs",
                Collections.singletonList(new IdentifierNode("Bas", null)), null);
        function.addReference("B");
        ProcessNode b = new ProcessNode("automata", "B", function, localProcesses, null);

        List<ProcessNode> expected = new ArrayList<>(Arrays.asList(basic, bas, b));

        if(!expected.equals(nodes)){
            fail("expecting process node lists to be equivalent");
        }
    }

    private String constructBasicTestInput(){
        return "processes {" +
            "Basic = (a -> (t -> b -> STOP | c -> STOP))." +
            "Bas = Basic\\{t}." +
            "B = abs(Bas)." +
            "}\n" +
                "automata Basic,Bas,B.";
    }

    @Test
    public void correctSimpleLoopTestExample_1() throws CompilationException, InterruptedException {
        String input = "processes Tt = (takeTea -> Tt).\nautomata Tt.";
        ProcessNode node = constructProcessNode(input);

        ReferenceNode reference = new ReferenceNode("Tt", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"takeTea"}, reference);
        sequence.addReference("Tt");
        ProcessNode expected = new ProcessNode("automata", "Tt", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctSimpleLoopTestExample_2() throws CompilationException, InterruptedException {
        String input = "processes BT = (teaButton -> takeTea -> BT).\nautomata BT.";
        ProcessNode node = constructProcessNode(input);

        ReferenceNode reference = new ReferenceNode("BT", null);
        SequenceNode sequence = constructSequenceNode(new String[]{"teaButton", "takeTea"}, reference);
        sequence.addReference("BT");
        ProcessNode expected = new ProcessNode("automata", "BT", sequence, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctSimpleLocalProcessTestExample() throws CompilationException, InterruptedException {
        String input = "processes P = (a -> Q), Q = (b -> P | c -> Q).\nautomata P.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence1 = constructSequenceNode(new String[]{"b"}, new ReferenceNode("P", null));
        SequenceNode sequence2 = constructSequenceNode(new String[]{"c"}, new ReferenceNode("P.Q", null));
        ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
        choice.addReference("P.Q");
        SequenceNode sequence3 = constructSequenceNode(new String[]{"a"}, choice);
        sequence3.addReference("P");

        ProcessNode expected = new ProcessNode("automata", "P", sequence3, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctTrafficLightTestExample() throws CompilationException, InterruptedException {
        String input = "processes TrRed = (red -> TrRed | turnGreen -> TrGreen), TrGreen = (green -> TrGreen | turnRed -> TrRed).\nautomata TrRed.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence1 = constructSequenceNode(new String[]{"red"}, new ReferenceNode("TrRed", null));
        SequenceNode sequence2 = constructSequenceNode(new String[]{"green"}, new ReferenceNode("TrRed.TrGreen", null));
        SequenceNode sequence3 = constructSequenceNode(new String[]{"turnRed"}, new ReferenceNode("TrRed", null));
        ChoiceNode choice1 = constructChoiceNode(sequence2, sequence3);
        choice1.addReference("TrRed.TrGreen");
        SequenceNode sequence4 = constructSequenceNode(new String[]{"turnGreen"}, choice1);
        ChoiceNode choice2 = constructChoiceNode(sequence1, sequence4);
        choice2.addReference("TrRed");

        ProcessNode expected = new ProcessNode("automata", "TrRed", choice2, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctParallelTestExample_1() throws CompilationException, InterruptedException {
        String input = "processes Parallel = ((a -> b -> c -> STOP) || (x -> y -> z -> STOP)).\nautomata Parallel.";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"a", "b", "c"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"x", "y", "z"}, terminal);
        CompositeNode composite = constructCompositeNode("||", sequence1, sequence2);
        composite.addReference("Parallel");

        ProcessNode expected = new ProcessNode("automata", "Parallel", composite, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctParallelTestExample_2() throws CompilationException, InterruptedException {
        String input = "processes Parallel2 = ((a -> m -> c -> STOP) || (x -> m -> z -> STOP))\\{m}.\nautomata Parallel2.";
        ProcessNode node = constructProcessNode(input);

        TerminalNode terminal = new TerminalNode("STOP", null);
        SequenceNode sequence1 = constructSequenceNode(new String[]{"a", "m", "c"}, terminal);
        SequenceNode sequence2 = constructSequenceNode(new String[]{"x", "m", "z"}, terminal);
        HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<>(Collections.singletonList("m")), null), null);
        CompositeNode composite = constructCompositeNode("||", sequence1, sequence2);
        composite.addReference("Parallel2");
        ProcessRootNode root = new ProcessRootNode(composite, null, null, hiding, null);

        ProcessNode expected = new ProcessNode("automata", "Parallel2", root, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctBufferTestExample() throws CompilationException, InterruptedException {
        List<ProcessNode> nodes = constructProcessList(constructBufferInput());

        List<ProcessNode> expected = new ArrayList<>();
        List<LocalProcessNode> localProcesses = new ArrayList<>();

        // Buff
        SequenceNode sequence = constructSequenceNode(new String[]{"in", "out"}, new ReferenceNode("Buff", null));
        sequence.addReference("Buff");
        expected.add(new ProcessNode("automata", "Buff", sequence, localProcesses, null));

        // B2
        ProcessRootNode root1 = new ProcessRootNode(new IdentifierNode("Buff", null), "one", null, null, null);
        ProcessRootNode root2 = new ProcessRootNode(new IdentifierNode("Buff", null), "two", null, null, null);
        CompositeNode composite1 = constructCompositeNode("||", root1, root2);
        composite1.addReference("B2");
        expected.add(new ProcessNode("automata", "B2", composite1, localProcesses, null));

        // B3
        RelabelElementNode element1 = new RelabelElementNode("move", "one.out", null);
        ProcessRootNode root3 = new ProcessRootNode(new IdentifierNode("Buff", null), "one", new RelabelNode(new ArrayList<>(Collections.singletonList(element1)), null), null, null);
        RelabelElementNode element2 = new RelabelElementNode("move", "two.in", null);
        ProcessRootNode root4 = new ProcessRootNode(new IdentifierNode("Buff", null), "two", new RelabelNode(new ArrayList<>(Collections.singletonList(element2)), null), null, null);
        CompositeNode composite2 = constructCompositeNode("||", root3, root4);
        composite2.addReference("B3");
        expected.add(new ProcessNode("automata", "B3", composite2, localProcesses, null));

        // B4
        HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<>(Collections.singletonList("move")), null), null);
        IdentifierNode ident = new IdentifierNode("B3", null);
        ident.addReference("B4");
        ProcessRootNode root5 = new ProcessRootNode(ident, null, null, hiding, null);
        expected.add(new ProcessNode("automata", "B4", root5, localProcesses, null));

        // B5
        FunctionNode function1 = new FunctionNode("abs",
                Collections.singletonList(new IdentifierNode("B4", null)), null);

        function1.addReference("B5");
        expected.add(new ProcessNode("automata", "B5", function1, localProcesses, null));

        // B6
        FunctionNode function2 = new FunctionNode("simp",
                Collections.singletonList(new IdentifierNode("B5", null)), null);
        expected.add(new ProcessNode("automata", "B6", function2, localProcesses, null));
        function2.addReference("B6");

        if(!expected.equals(nodes)){
            fail("expecting process node lists to be equivalent");
        }

    }

    private String constructBufferInput(){
        return "processes {" +
            "Buff = (in -> out -> Buff)." +
            "B2 = (one:Buff || two:Buff)." +
            "B3 = (one:Buff/{move/one.out} || two:Buff/{move/two.in})." +
            "B4 = B3\\{move}." +
            "B5 = abs(B4)." +
            "B6 = simp(B5)." +
            "}\n"+
                "automata Buff,B2,B3,B4,B5,B6."
                ;
    }

    @Test
    public void correctMoneyTestExample() throws CompilationException, InterruptedException {
        String input = "const Coins = 3. processes Money = C[1], C[i:1..Coins] = (when (i < Coins) coin -> C[i + 1] | when (i == Coins) coin -> C[1]).\nautomata Money.";
        ProcessNode node = constructProcessNode(input);

        SequenceNode sequence1 = constructSequenceNode(new String[]{"coin"}, new ReferenceNode("Money.C[1]", null));
        sequence1.addReference("Money.C[3]");
        SequenceNode sequence2 = constructSequenceNode(new String[]{"coin"}, sequence1);
        sequence2.addReference("Money.C[2]");
        SequenceNode sequence3 = constructSequenceNode(new String[]{"coin"}, sequence2);
        sequence3.addReference("Money.C[1]");
        sequence3.addReference("Money");

        ProcessNode expected = new ProcessNode("automata", "Money", sequence3, new ArrayList<>(), null);

        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctLockTestExample() throws CompilationException, InterruptedException {
        String input = "const Locks = 2. processes Lock = ([i:1..Locks].setLock -> L[i]), L[j:1..Locks] = ([i:1..Locks].enter -> (when (i == j) open -> close -> L[j] | when (i != j) error -> Lock)).\nautomata Lock.";
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
        ProcessNode expected = new ProcessNode("automata", "Lock", choice3, new ArrayList<>(), null);
        choice3.addReference("Lock");
        if(!expected.equals(node)){
            fail("expecting process nodes to be equivalent");
        }
    }

    @Test
    public void correctFarmTestExample() throws CompilationException, InterruptedException {
        List<ProcessNode> nodes = constructProcessList(constructFarmInput());

        List<ProcessNode> expected = new ArrayList<>();
        List<LocalProcessNode> emptyLocal = new ArrayList<>();

        // Worker
        SequenceNode sequence1 = constructSequenceNode(new String[]{"getTask", "doTask"}, new ReferenceNode("Worker", null));
        sequence1.addReference("Worker");
        expected.add(new ProcessNode("automata", "Worker", sequence1, emptyLocal, null));

        // Workers
        ProcessRootNode root1 = new ProcessRootNode(new IdentifierNode("Worker", null), "[1]", null, null, null);
        ProcessRootNode root2 = new ProcessRootNode(new IdentifierNode("Worker", null), "[2]", null, null, null);
        ProcessRootNode root3 = new ProcessRootNode(new IdentifierNode("Worker", null), "[3]", null, null, null);
        CompositeNode composite = constructCompositeNode("||", root1, constructCompositeNode("||", root2, root3));
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
        CompositeNode composite2 = constructCompositeNode("||", new IdentifierNode("Farmer", null), new IdentifierNode("Workers", null));
        composite2.addReference("Farm");
        expected.add(new ProcessNode("automata", "Farm", composite2, emptyLocal, null));


        assertEquals(expected,nodes);
    }

    private String constructFarmInput(){
        return "const W = 3. " +
            "processes {" +
            "Worker = (getTask -> doTask -> Worker)." +
            "Workers = (forall [i:1..W] ([i]:Worker))." +
            "Farmer = F[1], F[i:1..W] = (when (i < W) [i].getTask -> F[i + 1] | when (i >= W) [i].getTask -> F[1])." +
            "Farm = (Farmer || Workers)." +
            "}\n"+
                "automata Worker,Workers,Farmer,Farm.";
    }

}
