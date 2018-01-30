package mc.compiler.expander;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mc.compiler.ast.ChoiceNode;
import mc.compiler.ast.CompositeNode;
import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.HidingNode;
import mc.compiler.ast.IdentifierNode;
import mc.compiler.ast.IndexNode;
import mc.compiler.ast.LocalProcessNode;
import mc.compiler.ast.ProcessNode;
import mc.compiler.ast.ProcessRootNode;
import mc.compiler.ast.RangeNode;
import mc.compiler.ast.RangesNode;
import mc.compiler.ast.RelabelElementNode;
import mc.compiler.ast.RelabelNode;
import mc.compiler.ast.SequenceNode;
import mc.compiler.ast.SetNode;
import mc.compiler.ast.TerminalNode;
import mc.exceptions.CompilationException;
import mc.plugins.PluginManager;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by sheriddavi on 9/02/17.
 */
public class ExampleTests extends ExpanderTests {
  @BeforeClass
  public static void initialise() {
    PluginManager.getInstance().registerPlugins();
  }

  public ExampleTests() throws InterruptedException {
  }

  @Test
  public void correctSimpleTestProcessTypes_1() throws CompilationException, InterruptedException {
    String input = "processes Simple = (takeTea -> STOP).\nautomata Simple.";
    ProcessNode node = constructProcessNode(input);

    TerminalNode terminal = new TerminalNode("STOP", null);
    SequenceNode sequence = constructSequenceNode(new String[] {"takeTea"}, terminal);
    ProcessNode expected = new ProcessNode("Simple", sequence, new ArrayList<>(), null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctSimpleTestProcessTypes_2() throws CompilationException, InterruptedException {
    String input = "processes Two = (teaButton -> takeTea -> STOP).\nautomata Two.";
    ProcessNode node = constructProcessNode(input);

    TerminalNode terminal = new TerminalNode("STOP", null);
    SequenceNode sequence = constructSequenceNode(new String[] {"teaButton", "takeTea"}, terminal);
    ProcessNode expected = new ProcessNode("Two", sequence, new ArrayList<>(), null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctCoffeeMachineTestProcessTypes() throws CompilationException, InterruptedException {
    String input = "processes CM = (teaButton -> takeTea -> STOP | coffeeButton -> takeCoffee -> STOP).\nautomata CM.";
    ProcessNode node = constructProcessNode(input);

    TerminalNode terminal = new TerminalNode("STOP", null);
    SequenceNode sequence1 = constructSequenceNode(new String[] {"teaButton", "takeTea"}, terminal);
    SequenceNode sequence2 = constructSequenceNode(new String[] {"coffeeButton", "takeCoffee"}, terminal);
    ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
    ProcessNode expected = new ProcessNode("CM", choice, new ArrayList<>(), null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctVendingMachineTestProcessTypes_1() throws CompilationException, InterruptedException {
    String input = "processes VM = (coin -> (teaBtn -> tea -> STOP | coffeeBtn -> coffee -> STOP)).\nautomata VM.";
    ProcessNode node = constructProcessNode(input);

    TerminalNode terminal = new TerminalNode("STOP", null);
    SequenceNode sequence1 = constructSequenceNode(new String[] {"teaBtn", "tea"}, terminal);
    SequenceNode sequence2 = constructSequenceNode(new String[] {"coffeeBtn", "coffee"}, terminal);
    ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
    SequenceNode sequence = constructSequenceNode(new String[] {"coin"}, choice);
    ProcessNode expected = new ProcessNode("VM", sequence, new ArrayList<>(), null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctVendingMachineTestProcessTypes_2() throws CompilationException, InterruptedException {
    String input = "processes VM2 = (coin -> teaBtn -> tea -> STOP | coin -> coffeeBtn -> coffee -> STOP).\nautomata VM2.";
    ProcessNode node = constructProcessNode(input);

    TerminalNode terminal = new TerminalNode("STOP", null);
    SequenceNode sequence1 = constructSequenceNode(new String[] {"coin", "teaBtn", "tea"}, terminal);
    SequenceNode sequence2 = constructSequenceNode(new String[] {"coin", "coffeeBtn", "coffee"}, terminal);
    ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
    ProcessNode expected = new ProcessNode("VM2", choice, new ArrayList<>(), null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctBasicAbstractionTestProcessTypes() throws CompilationException, InterruptedException {
    List<ProcessNode> nodes = constructProcessList(constructBasicTestInput());

    TerminalNode terminal = new TerminalNode("STOP", null);
    List<LocalProcessNode> localProcesses = new ArrayList<>();

    // Basic
    SequenceNode sequence1 = constructSequenceNode(new String[] {"t", "b"}, terminal);
    SequenceNode sequence2 = constructSequenceNode(new String[] {"c"}, terminal);
    ChoiceNode choice = constructChoiceNode(sequence1, sequence2);
    SequenceNode sequence = constructSequenceNode(new String[] {"a"}, choice);
    ProcessNode basic = new ProcessNode("Basic", sequence, localProcesses, null);
    basic.addType("automata");

    // Bas
    HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<>(Collections.singletonList("t")), null), null);
    ProcessRootNode root = new ProcessRootNode(new IdentifierNode("Basic", null), null, null, hiding, null);
    ProcessNode bas = new ProcessNode("Bas", root, localProcesses, null);
    bas.addType("automata");

    // B
    FunctionNode function = new FunctionNode("abs",
        Collections.singletonList(new IdentifierNode("Bas", null)), null);
    ProcessNode b = new ProcessNode("B",function, localProcesses, null);
    b.addType("automata");

    List<ProcessNode> expected = new ArrayList<>(Arrays.asList(basic, bas, b));

    assertEquals(expected, nodes);
    if (!expected.equals(nodes)) {
      fail("expecting process node lists to be equivalent");
    }
  }

  private String constructBasicTestInput() {
    return "processes {" +
        "Basic = (a -> (t -> b -> STOP | c -> STOP))." +
        "Bas = Basic\\{t}." +
        "B = abs(Bas)." +
        "}\n" +
        "automata Basic,Bas,B.";
  }

  @Test
  public void correctSimpleLoopTestProcessTypes_1() throws CompilationException, InterruptedException {
    String input = "processes Tt = (takeTea -> Tt).\nautomata Tt.";
    ProcessNode node = constructProcessNode(input);

    IdentifierNode identifier = new IdentifierNode("Tt", null);
    SequenceNode sequence = constructSequenceNode(new String[] {"takeTea"}, identifier);
    ProcessNode expected = new ProcessNode("Tt", sequence, new ArrayList<>(), null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctSimpleLoopTestProcessTypes_2() throws CompilationException, InterruptedException {
    String input = "processes BT = (teaButton -> takeTea -> BT).\nautomata BT.";
    ProcessNode node = constructProcessNode(input);

    IdentifierNode identifier = new IdentifierNode("BT", null);
    SequenceNode sequence = constructSequenceNode(new String[] {"teaButton", "takeTea"}, identifier);
    ProcessNode expected = new ProcessNode("BT", sequence, new ArrayList<>(), null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctSimpleLocalProcessTestProcessTypes() throws CompilationException, InterruptedException {
    String input = "processes P = (a -> Q), Q = (b -> P | c -> Q).\nautomata P.";
    ProcessNode node = constructProcessNode(input);

    IdentifierNode ident = new IdentifierNode("P", null);
    IdentifierNode localIdent = new IdentifierNode("Q", null);
    SequenceNode sequence = constructSequenceNode(new String[] {"a"}, localIdent);
    List<LocalProcessNode> localProcesses = new ArrayList<>();
    SequenceNode process1 = constructSequenceNode(new String[] {"b"}, ident);
    SequenceNode process2 = constructSequenceNode(new String[] {"c"}, localIdent);
    ChoiceNode choice = constructChoiceNode(process1, process2);
    localProcesses.add(new LocalProcessNode("Q", null, choice, null));
    ProcessNode expected = new ProcessNode("P", sequence, localProcesses, null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctTrafficLightTestProcessTypes() throws CompilationException, InterruptedException {
    String input = "processes TrRed = (red -> TrRed | turnGreen -> TrGreen), TrGreen = (green -> TrGreen | turnRed -> TrRed).\nautomata TrRed.";
    ProcessNode node = constructProcessNode(input);

    IdentifierNode ident = new IdentifierNode("TrRed", null);
    IdentifierNode localIdent = new IdentifierNode("TrGreen", null);

    SequenceNode process1 = constructSequenceNode(new String[] {"red"}, ident);
    SequenceNode process2 = constructSequenceNode(new String[] {"turnGreen"}, localIdent);
    ChoiceNode choice1 = constructChoiceNode(process1, process2);

    List<LocalProcessNode> localProcesses = new ArrayList<>();
    SequenceNode process3 = constructSequenceNode(new String[] {"green"}, localIdent);
    SequenceNode process4 = constructSequenceNode(new String[] {"turnRed"}, ident);
    ChoiceNode choice2 = constructChoiceNode(process3, process4);
    localProcesses.add(new LocalProcessNode("TrGreen", null, choice2, null));

    ProcessNode expected = new ProcessNode("TrRed", choice1, localProcesses, null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctParallelTestProcessTypes_1() throws CompilationException, InterruptedException {
    String input = "processes Parallel = ((a -> b -> c -> STOP) || (x -> y -> z -> STOP)).\nautomata Parallel.";
    ProcessNode node = constructProcessNode(input);

    TerminalNode terminal = new TerminalNode("STOP", null);
    SequenceNode sequence1 = constructSequenceNode(new String[] {"a", "b", "c"}, terminal);
    SequenceNode sequence2 = constructSequenceNode(new String[] {"x", "y", "z"}, terminal);
    CompositeNode composite = constructCompositeNode("||", sequence1, sequence2);

    ProcessNode expected = new ProcessNode("Parallel", composite, new ArrayList<>(), null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctParallelTestProcessTypes_2() throws CompilationException, InterruptedException {
    String input = "processes Parallel2 = ((a -> m -> c -> STOP) || (x -> m -> z -> STOP))\\{m}.\nautomata Parallel2.";
    ProcessNode node = constructProcessNode(input);

    TerminalNode terminal = new TerminalNode("STOP", null);
    SequenceNode sequence1 = constructSequenceNode(new String[] {"a", "m", "c"}, terminal);
    SequenceNode sequence2 = constructSequenceNode(new String[] {"x", "m", "z"}, terminal);
    HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<>(Collections.singletonList("m")), null), null);
    CompositeNode composite = constructCompositeNode("||", sequence1, sequence2);
    ProcessRootNode root = new ProcessRootNode(composite, null, null, hiding, null);

    ProcessNode expected = new ProcessNode("Parallel2", root, new ArrayList<>(), null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctBufferTestProcessTypesProcessTypes() throws CompilationException, InterruptedException {
    List<ProcessNode> nodes = constructProcessList(constructBufferInput());

    List<ProcessNode> expected = new ArrayList<>();
    List<LocalProcessNode> localProcesses = new ArrayList<>();
    IdentifierNode ident = new IdentifierNode("Buff", null);

    // Buff
    SequenceNode sequence = constructSequenceNode(new String[] {"in", "out"}, ident);
    ProcessNode pn = new ProcessNode("Buff", sequence, localProcesses, null);
    pn.addType("automata");
    expected.add(pn);

    // B2
    ProcessRootNode root1 = new ProcessRootNode(ident, "one", null, null, null);
    ProcessRootNode root2 = new ProcessRootNode(ident, "two", null, null, null);
    CompositeNode composite1 = constructCompositeNode("||", root1, root2);
    pn = new ProcessNode("B2", composite1, localProcesses, null);
    pn.addType("automata");
    expected.add(pn);


    // B3
    RelabelElementNode element1 = new RelabelElementNode("move", "one.out", null);
    ProcessRootNode root3 = new ProcessRootNode(ident, "one", new RelabelNode(new ArrayList<>(Collections.singletonList(element1)), null), null, null);
    RelabelElementNode element2 = new RelabelElementNode("move", "two.in", null);
    ProcessRootNode root4 = new ProcessRootNode(ident, "two", new RelabelNode(new ArrayList<>(Collections.singletonList(element2)), null), null, null);
    CompositeNode composite2 = constructCompositeNode("||", root3, root4);
    pn = new ProcessNode("B3", composite2, localProcesses, null);
    pn.addType("automata");
    expected.add(pn);

    // B4
    HidingNode hiding = new HidingNode("includes", new SetNode(new ArrayList<>(Collections.singletonList("move")), null), null);
    ProcessRootNode root5 = new ProcessRootNode(new IdentifierNode("B3", null), null, null, hiding, null);
    pn = new ProcessNode("B4", root5, localProcesses, null);
    pn.addType("automata");
    expected.add(pn);

    // B5
    FunctionNode function1 = new FunctionNode("abs",
        Collections.singletonList(new IdentifierNode("B4", null)), null);
    pn = new ProcessNode("B5", function1, localProcesses, null);
    pn.addType("automata");
    expected.add(pn);

    // B6
    FunctionNode function2 = new FunctionNode("simp",
        Collections.singletonList(new IdentifierNode("B5", null)), null);
    pn = new ProcessNode("B6", function2, localProcesses, null);
    pn.addType("automata");
    expected.add(pn);

    if (!expected.equals(nodes)) {
      fail("expecting process node lists to be equivalent");
    }

  }

  private String constructBufferInput() {
    return "processes {" +
        "Buff = (in -> out -> Buff)." +
        "B2 = (one:Buff || two:Buff)." +
        "B3 = (one:Buff/{move/one.out} || two:Buff/{move/two.in})." +
        "B4 = B3\\{move}." +
        "B5 = abs(B4)." +
        "B6 = simp(B5)." +
        "}\n" +
        "automata Buff,B2,B3,B4,B5,B6.";
  }

  @Test
  public void correctMoneyTestProcessTypes() throws CompilationException, InterruptedException {
    String input = "const Coins = 3. processes Money = C[1], C[i:1..Coins] = (when (i < Coins) coin -> C[i + 1] | when (i == Coins) coin -> C[1]).\nautomata Money.";
    ProcessNode node = constructProcessNode(input);

    IndexNode index = new IndexNode("$i", new RangeNode(1, 3, null), null, null);
    RangesNode range = new RangesNode(new ArrayList<>(Collections.singletonList(index)), null);

    List<LocalProcessNode> localProcesses = new ArrayList<>();
    SequenceNode sequence1 = constructSequenceNode(new String[] {"coin"}, new IdentifierNode("C[2]", null));
    localProcesses.add(new LocalProcessNode("C[1]", range, sequence1, null));
    SequenceNode sequence2 = constructSequenceNode(new String[] {"coin"}, new IdentifierNode("C[3]", null));
    localProcesses.add(new LocalProcessNode("C[2]", range, sequence2, null));
    SequenceNode sequence3 = constructSequenceNode(new String[] {"coin"}, new IdentifierNode("C[1]", null));
    localProcesses.add(new LocalProcessNode("C[3]", range, sequence3, null));

    ProcessNode expected = new ProcessNode("Money", new IdentifierNode("C[1]", null), localProcesses, null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctLockTestProcessTypes() throws CompilationException, InterruptedException {
    String input = "const Locks = 2. processes Lock = ([i:1..Locks].setLock -> L[i]), L[j:1..Locks] = ([i:1..Locks].enter -> (when (i == j) open -> close -> L[j] | when (i != j) error -> Lock)).\nautomata Lock.";
    ProcessNode node = constructProcessNode(input);

    SequenceNode mainSequence1 = constructSequenceNode(new String[] {"[1].setLock"}, new IdentifierNode("L[1]", null));
    SequenceNode mainSequence2 = constructSequenceNode(new String[] {"[2].setLock"}, new IdentifierNode("L[2]", null));
    ChoiceNode mainChoice = constructChoiceNode(mainSequence1, mainSequence2);

    RangesNode range = new RangesNode(new ArrayList<>(Collections.singletonList(new IndexNode("$j", new RangeNode(1, 2, null), null, null))), null);
    List<LocalProcessNode> localProcesses = new ArrayList<>();
    SequenceNode sequence1 = constructSequenceNode(new String[] {"[1].enter", "open", "close"}, new IdentifierNode("L[1]", null));
    SequenceNode sequence2 = constructSequenceNode(new String[] {"[2].enter", "error"}, new IdentifierNode("Lock", null));
    ChoiceNode choice1 = constructChoiceNode(sequence1, sequence2);
    localProcesses.add(new LocalProcessNode("L[1]", range, choice1, null));
    SequenceNode sequence3 = constructSequenceNode(new String[] {"[1].enter", "error"}, new IdentifierNode("Lock", null));
    SequenceNode sequence4 = constructSequenceNode(new String[] {"[2].enter", "open", "close"}, new IdentifierNode("L[2]", null));
    ChoiceNode choice2 = constructChoiceNode(sequence3, sequence4);
    localProcesses.add(new LocalProcessNode("L[2]", range, choice2, null));


    ProcessNode expected = new ProcessNode("Lock", mainChoice, localProcesses, null);
    expected.addType("automata");

    if (!expected.equals(node)) {
      fail("expecting process nodes to be equivalent");
    }
  }

  @Test
  public void correctFarmTestProcessTypes() throws CompilationException, InterruptedException {
    List<ProcessNode> nodes = constructProcessList(constructFarmInput());

    List<ProcessNode> expected = new ArrayList<>();
    List<LocalProcessNode> emptyLocal = new ArrayList<>();

    // Worker
    SequenceNode sequence1 = constructSequenceNode(new String[] {"getTask", "doTask"}, new IdentifierNode("Worker", null));
    ProcessNode pn = new ProcessNode( "Worker", sequence1, emptyLocal, null);
    pn.addType("automata");
    expected.add(pn);

    // Workers
    ProcessRootNode root1 = new ProcessRootNode(new IdentifierNode("Worker", null), "[1]", null, null, null);
    ProcessRootNode root2 = new ProcessRootNode(new IdentifierNode("Worker", null), "[2]", null, null, null);
    ProcessRootNode root3 = new ProcessRootNode(new IdentifierNode("Worker", null), "[3]", null, null, null);
    CompositeNode composite = constructCompositeNode("||", root1, constructCompositeNode("||", root2, root3));
    pn = new ProcessNode("Workers", composite, emptyLocal, null);
    pn.addType("automata");
    expected.add(pn);

    // Farmer
    IndexNode index = new IndexNode("$i", new RangeNode(1, 3, null), null, null);
    RangesNode range = new RangesNode(new ArrayList<>(Collections.singletonList(index)), null);
    List<LocalProcessNode> localProcesses = new ArrayList<>();
    localProcesses.add(new LocalProcessNode("F[1]", range, constructSequenceNode(new String[] {"[1].getTask"}, new IdentifierNode("F[2]", null)), null));
    localProcesses.add(new LocalProcessNode("F[2]", range, constructSequenceNode(new String[] {"[2].getTask"}, new IdentifierNode("F[3]", null)), null));
    localProcesses.add(new LocalProcessNode("F[3]", range, constructSequenceNode(new String[] {"[3].getTask"}, new IdentifierNode("F[1]", null)), null));
    pn = new ProcessNode("Farmer", new IdentifierNode("F[1]", null), localProcesses, null);
    pn.addType("automata");
    expected.add(pn);

    // Farm
    CompositeNode composite2 = constructCompositeNode("||", new IdentifierNode("Farmer", null), new IdentifierNode("Workers", null));
    pn = new ProcessNode("Farm", composite2, emptyLocal, null);
    pn.addType("automata");
    expected.add(pn);


    if (!expected.equals(nodes)) {
      fail("expecting process node lists to be equivalent");
    }
  }

  private String constructFarmInput() {
    return "const W = 3. " +
        "processes {" +
        "Worker = (getTask -> doTask -> Worker)." +
        "Workers = (forall [i:1..W] ([i]:Worker))." +
        "Farmer = F[1], F[i:1..W] = (when (i < W) [i].getTask -> F[i + 1] | when (i >= W) [i].getTask -> F[1])." +
        "Farm = (Farmer || Workers)." +
        "}\n" +
        "automata Worker,Workers,Farmer,Farm.";
  }
}
