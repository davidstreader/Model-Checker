package mc.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import mc.compiler.ast.AbstractSyntaxTree;
import mc.compiler.ast.ProcessNode;
import mc.compiler.token.Token;
import mc.exceptions.CompilationException;
import mc.processmodels.ProcessModel;
import mc.webserver.Context;

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
    this.lexer = new Lexer();
    parser = new Parser();
    this.expander = new Expander();
    this.replacer = new ReferenceReplacer();
    this.interpreter = new Interpreter();
    this.eqEvaluator = new EquationEvaluator();
    this.evaluator = new OperationEvaluator();
  }


  public CompilationObject compile(String code, Context context, com.microsoft.z3.Context z3Context, BlockingQueue<Object> messageQueue) throws CompilationException, InterruptedException {
    List<Token> codeInput = lexer.tokenise(code);
    AbstractSyntaxTree structedCode = parser.parse(codeInput, z3Context);

    CompilationObject compilerOutput = compile(structedCode, code, z3Context, context, messageQueue);

    CompilationObservable.getInstance().updateClient(compilerOutput);

    return compilerOutput;
  }

  private CompilationObject compile(AbstractSyntaxTree ast, String code, com.microsoft.z3.Context z3Context, Context context, BlockingQueue<Object> messageQueue) throws CompilationException, InterruptedException {
    HashMap<String, ProcessNode> processNodeMap = new HashMap<>();
    HashMap<String, ProcessNode> dependencyMap = new HashMap<>();

    for (ProcessNode node : ast.getProcesses()) {
      processNodeMap.put(node.getIdentifier(), (ProcessNode) node.copy());
      dependencyMap.put(node.getIdentifier(), node);
    }

    ast = expander.expand(ast, messageQueue, z3Context);
    /** replacer.replaceReferences
     * Expands references i.e Initally we are now at: P1 = a->P2.
     *                                                P2 = b->c->x.
     *  Then it expands it to, P1 = a->b->c->x. If it needs it
     */
    ast = replacer.replaceReferences(ast, messageQueue);


    System.out.println("Hierarchy of processes: " + ast.getProcessHierarchy().getDependencies());

    List<String> processesToRemoveFromDisplay = new ArrayList<>();
    for (String processesName : processNodeMap.keySet()) { // Find if the dependencies have all been set correctly
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

    Map<String, ProcessModel> processMap = interpreter.interpret(ast,
        new LocalCompiler(processNodeMap, expander, replacer, messageQueue),
        messageQueue, z3Context);
    List<OperationResult> opResults = evaluator.evaluateOperations(ast.getOperations(), processMap,
        interpreter, code, z3Context);
    EquationEvaluator.EquationReturn eqResults = eqEvaluator.evaluateEquations(
        new ArrayList<>(processMap.values()), ast.getEquations(),
        code, context, z3Context, messageQueue);

    processMap.putAll(eqResults.getToRender());

    processesToRemoveFromDisplay.stream()
        .filter(processMap::containsKey)
        .forEach(processMap::remove);

    return new CompilationObject(processMap, opResults, eqResults.getResults());
  }

}
