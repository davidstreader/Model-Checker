package mc.compiler;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.token.Token;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;

public class Compiler {
  // fields
  private Lexer lexer;
  private Expander expander;
  private ReferenceReplacer replacer;
  private Interpreter interpreter;
  private OperationEvaluator evaluator;
  private EquationEvaluator eqEvaluator;
  private Parser parser;


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
                                   BlockingQueue<Object> messageQueue)
    throws CompilationException, InterruptedException {
    //LEX ->PARSE->COMPILE
    List<Token> codeInput = lexer.tokenise(code);
    AbstractSyntaxTree ast = parser.parse(codeInput, z3Context);

    return compile(ast, code,  z3Context, messageQueue);
  }

  /**
   * ONLY start point for building automata
   * @param ast           The abstract syntax tree returned by the parser
   * @param code          The users code as a non-formatted string
   * @param z3Context     z3 Context for evaluating
   * @param messageQueue  thread safe blocking queue for logging
   * @return              Returns a compilation object which is comprised of the results of any tests and the constructed models for display
   * @throws CompilationException
   * @throws InterruptedException
   */

  private CompilationObject compile(AbstractSyntaxTree ast, String code,
                                    com.microsoft.z3.Context z3Context,
                                    BlockingQueue<Object> messageQueue)
    throws CompilationException, InterruptedException {
    HashMap<String, ProcessNode> processNodeMap = new HashMap<>();
    HashMap<String, ProcessNode> dependencyMap = new HashMap<>();

    for (ProcessNode node : ast.getProcesses()) {
      System.out.println("**COMPILER** Compiler Start node = "+ node.getIdentifier());
      processNodeMap.put(node.getIdentifier(), (ProcessNode) node.copy());
      dependencyMap.put(node.getIdentifier(), node);
    }

    //System.out.println("Before Expanding "+ast.toString());
/*
   Expand the non hidden variables (indexes) and return an ast
   NOTE changes the ast in undefined way BUT this is required for the replacer to work
 */
    ast = expander.expand(ast, messageQueue, z3Context);
    /* replacer.replaceReferences
     * Expands references i.e Initally we are now at: P1 = a->P2.
     *                                                P2 = b->c->x.
     *  Then it expands it to, P1 = a->b->c->x. If it needs it
     *  BEWARE expanding an prune events that are not needed for the finite state
     *  size chosen  hence symbolic processes must eb built prior to expansion!
     *
     *  This is not needed in the petrinet interpreter Hence when we ditch the automata
     *  interprter we may be able to ditch the ref replacer
     */

    //System.out.println("After Expanding "+ast.toString());
    ast = replacer.replaceReferences(ast, messageQueue);


    System.out.println("**COMPILER** Hierarchy of processes: " + ast.getProcessHierarchy().getDependencies());

    List<String> processesToRemoveFromDisplay = new ArrayList<>();
    for (String processesName : processNodeMap.keySet()) {
      // Find if the dependencies have all been set correctly
      Set<String> dependencies = ast.getProcessHierarchy().getDependencies(processesName); // Dependencies for the current process
      ProcessNode currentProcess = dependencyMap.get(processesName);
      for (String currentDependencyName : dependencies) {
        ProcessNode currentDependency = dependencyMap.get(currentDependencyName);
        if (currentDependency.getType().size() == 0) {
          currentDependency.getType().addAll(currentProcess.getType());
          processesToRemoveFromDisplay.add(currentDependencyName);
        }
      }
    }
    //store alphabet
    Set<String> alpha = ast.getAlphabet().stream().map(x->x.getAction()).collect(Collectors.toSet());
//builds process and processMap
    System.out.println("**COMPILER** Entering interpreter with ast for processes -> Types "+
      ast.getProcesses().stream().map(x->"\n"+x.getIdentifier()+"->"+x.getType())
        .reduce("",(x,y)->x+" "+y));
    Map<String, ProcessModel> processMap = interpreter.interpret(ast,
        messageQueue, z3Context,alpha);

    System.out.println("     **COMPILER** before operation evaluation "+processMap.size());

    List<OperationResult> opResults = evaluator.evaluateOperations(
      ast.getOperations(), processMap,
        interpreter, code, z3Context, messageQueue, alpha);
   // List<ImpliesResult> impResults = evaluator.getImpRes();
   // System.out.println("     **COMPILER** before equation evaluation "+processMap.size()+ " op impRes "+impResults.size());

    // system currently has memory hence EE can give different results each time
    // still has memory problem with many permutations
    this.eqEvaluator = new EquationEvaluator(); // need to reset equationEvaluator else !!!!
    EquationEvaluator.EquationReturn eqResults = eqEvaluator.evaluateEquations(
        processMap, ast.getEquations(),
        code, z3Context, messageQueue, alpha);

    processMap.putAll(eqResults.getToRender());

   // printLocations(processMap.values());

    processesToRemoveFromDisplay.stream()
        .filter(processMap::containsKey)
        .forEach(processMap::remove);

    return new CompilationObject(processMap, opResults, eqResults.getResults());
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
