package mc.compiler;

import com.google.common.base.Function;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.token.Token;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.util.LogMessage;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Compiler {
  // fields
  private Lexer lexer;
  private Expander expander;
  private ReferenceReplacer replacer;
  private Interpreter interpreter;
  private OperationEvaluator evaluator;
  private EquationEvaluator eqEvaluator;
  private Parser parser;
  private Function<Object, Boolean> symbFunction;


  public Compiler() throws InterruptedException {
    this.lexer = new Lexer();                     //Tokenises the string input
    this.parser = new Parser();                   // AST created and Symbolic guards set up on the AST
    this.expander = new Expander();               // sets Guards on AST and expands non-hidden variables
    this.replacer = new ReferenceReplacer();      // AST to AST
    this.interpreter = new Interpreter();         // AST to Automaton or PetriNet
    this.eqEvaluator = new EquationEvaluator();   // Runs user created models through tests, permutating them. I.e A ~ B, with models D = t->STOP. V = f->STOP. would generate D ~ D, D ~ V, V ~ D, V  ~ V.
    this.evaluator = new OperationEvaluator();    // Tests set user models without permutation
  }

  /**
   * ONLY PUBLIC METHOD lexes-> parses to build AST then compiles the AST
   *
   * @param code          Source code text input
   * @param z3Context     z3 context for evaluation of code
   * @param messageQueue  used to implement concurrent logging while executing
   * @return              Returns a compilation object which is comprised of the results of any tests and the constructed models for display
   * @throws CompilationException
   * @throws InterruptedException
   *
   * lex parse compile
   */

  public CompilationObject compile(String code,
                                   com.microsoft.z3.Context z3Context,
                                   BlockingQueue<Object> messageQueue, Supplier<Boolean> symb)
    throws CompilationException, InterruptedException {
    //LEX ->PARSE->COMPILE
    List<Token> codeInput = lexer.tokenise(code);
    AbstractSyntaxTree ast = parser.parse(codeInput, z3Context);
      messageQueue.add(new LogMessage(Runtime.class.getPackage().getImplementationVersion()+ " XXXXXXXXX "));
      messageQueue.add(new LogMessage("Compile  starting  symbolic "+symb.get()));
      System.out.println(Runtime.class.getPackage().getImplementationVersion());
    //System.out.println("Compiler called parse that output " + ast.myString());
    return compile(ast, code,  z3Context, messageQueue,symb);
  }

  /**
   * ONLY start point for building automata
   * @param ast           The abstract syntax tree returned by the parser
   * @param code          The users code as a non-formatted string
   * @param z3Context     z3 Context for evaluating
   * @param messageQueue  ctx safe blocking queue for logging
   * @return              Returns a compilation object which is comprised of the results of any tests and the constructed models for display
   * @throws CompilationException
   * @throws InterruptedException
   */

  private CompilationObject compile(AbstractSyntaxTree ast, String code,
                                    com.microsoft.z3.Context z3Context,
                                    BlockingQueue<Object> messageQueue,
                                    Supplier< Boolean> symb)
    throws CompilationException, InterruptedException {
    HashMap<String, ProcessNode> processNodeMap = new HashMap<>();
  //  HashMap<String, ProcessNode> dependencyMap = new HashMap<>();

    //System.out.println("\nCOMPILER "+ast.myString()+" symb "+symb.get()+"\n");
    for (ProcessNode node : ast.getProcesses()) {
      //messageQueue.add(new LogMessage("Compile  starting "+node.getIdentifier()+" s "+symb.get()));
      //System.out.println("**COMPILER** Compiler Start node = "+ node.myString());
      processNodeMap.put(node.getIdentifier(), (ProcessNode) node.copy());
   //   dependencyMap.put(node.getIdentifier(), node);
    }

    AbstractSyntaxTree  symbAST = ast.copy();  // to be used to build symbolic models
    //??????

    //System.out.println("symb "+symb.get());
    if (!symb.get()) {
      ast = processAtomicAST(ast, z3Context, messageQueue);
    } else {
      //expander.expand(ast, messageQueue, z3Context); // use for error detection
      ast = symbAST;
      //ast = expander.expand(ast, messageQueue, z3Context);
      //System.out.println(" COMPILER Before ReferenceReplacer "+ast.processes2String());
      //ast = replacer.replaceReferences(ast, messageQueue);
      // If we replace the references then we lose the assignment information
      //System.out.println(" COMPILER After ReferenceReplacer "+ast.processes2String());

    }
    //
    //
    // ??????
     //store alphabet
    Set<String> alpha = ast.getAlphabet().stream().map(x->x.getAction()).collect(Collectors.toSet());
    //System.out.println("Compiler alph = "+alpha);

    //builds process and processMap
    /*System.out.println("**COMPILER** Entering interpreter with ast for processes -> Types "+
      ast.getProcesses().stream().map(x->"\n"+x.getIdentifier()+"->"+x.getType())
        .reduce("",(x,y)->x+" "+y)); */
    Map<String, ProcessModel> processMap = interpreter.interpret(ast,
        messageQueue, z3Context,alpha,symb.get());

    //System.out.println("     **COMPILER** before operation evaluation "+processMap.keySet());

    List<OperationResult> opResults = evaluator.evaluateOperations(
      ast.getOperations(), processMap,
        interpreter.getpetrinetInterpreter(), code, z3Context, messageQueue, alpha);
    //System.out.println("     **COMPILER** before equation evaluation "+processMap.keySet());

    // still has memory problem with many permutations
    this.eqEvaluator = new EquationEvaluator(); // need to reset equationEvaluator else !!!!
    EquationEvaluator.EquationReturn eqResults = eqEvaluator.evaluateEquations(
        processMap, interpreter.getpetrinetInterpreter(), ast.getEquations(),
        code, z3Context, messageQueue, alpha);



  /*  processesToRemoveFromDisplay.stream()
        .filter(processMap::containsKey)
        .forEach(processMap::remove); */

    return new CompilationObject(processMap, opResults, eqResults.getResults());
  }

private AbstractSyntaxTree processAtomicAST(AbstractSyntaxTree ast,
                                            com.microsoft.z3.Context z3Context,
                                            BlockingQueue<Object> messageQueue )
  throws CompilationException, InterruptedException {
    /*
   Expand the non hidden variables (indexes) and return an ast
   NOTE changes the ast in undefined way BUT this is required for the replacer to work
 */
  //System.out.println("lib "+ System.getProperty("java.library.path"));
  //System.out.println("class "+System.getProperty("java.class.path"));

  //System.out.println(" AtomicCOMPILER Before Expanding "+ast.myString());
  ast = expander.expand(ast, messageQueue, z3Context);
  /* replacer.replaceReferences  replaces references to local processes (P2)
   * Expands references i.e Initally we have: P1 = a->P2.
   *                                             P2 = b->c->x.
   *  With references replaced we have,       P1 = a->b->c->x.
   */
  //System.out.println(" AtomicCOMPILER After Expanding "+ast.processes2String());
  ast = replacer.replaceReferences(ast, messageQueue);
  //System.out.println(" AtomicCOMPILER After ReferenceReplacer "+ast.processes2String());


  //System.out.println("**AtomicCOMPILER** Hierarchy of processes: " + ast.getProcessHierarchy().getDependencies());

/*    List<String> processesToRemoveFromDisplay = new ArrayList<>();
    for (String processesName : processNodeMap.keySet()) {
      // Find if the dependencies have all been set correctly
      Set<String> dependencies = ast.getProcessHierarchy().getDependencies(processesName); // Dependencies for the current process
      ProcessNode currentProcess = dependencyMap.get(processesName);
      for (String currentDependencyName : dependencies) {
        ProcessNode currentDependency = dependencyMap.get(currentDependencyName);
        if (currentDependency.getType().size() == 0) {
          currentDependency.getType().addAll(currentProcess.getType());
          processesToRemoveFromDisplay.add(currentDependencyName);
          System.out.println("Removal list "+currentProcess.myString());
        }
      }
    } */
 return ast;
}


  /**
   * TODO: Remove this test.
   * @param processModels the processmodels in the current build cycle.
   */
  private static void printLocations(Collection<ProcessModel> processModels) {
    processModels.stream()
        .filter(Automaton.class::isInstance)
        .map(Automaton.class::cast)
        .map(a -> "Owners for " + a.getId() + " are: " + a.getOwners())
        .forEach(System.out::println);
  }

}
