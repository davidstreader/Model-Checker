package mc.compiler;

import static mc.util.Utils.instantiateClass;

import com.microsoft.z3.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ChoiceNode;
import mc.compiler.ast.CompositeNode;
import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.IdentifierNode;
import mc.compiler.ast.IfStatementNode;
import mc.compiler.ast.OperationNode;
import mc.compiler.ast.SequenceNode;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.operations.AutomataOperations;
import mc.util.Location;

/**
 * Created by sheriddavi on 27/01/17.
 */
public class OperationEvaluator {

  private int operationId;

  private AutomataOperations automataOperations;
  static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new HashMap<>();

  public OperationEvaluator() {
    this.automataOperations = new AutomataOperations();
  }

  public List<OperationResult> evaluateOperations(List<OperationNode> operations,
                                                  Map<String, ProcessModel> processMap, Interpreter interpreter,
                                                  String code, Context context)
      throws CompilationException, InterruptedException {
    reset();
    List<OperationResult> results = new ArrayList<>();
    for (OperationNode operation : operations) {

      String firstId = findIdent(operation.getFirstProcess(), code);
      String secondId = findIdent(operation.getSecondProcess(), code);

      List<String> firstIds = collectIdentifiers(operation.getFirstProcess());
      List<String> secondIds = collectIdentifiers(operation.getSecondProcess());

      List<Automaton> automata = new ArrayList<>();
      List<String> missing = new ArrayList<>(firstIds);

      missing.addAll(secondIds);
      missing.removeAll(processMap.keySet());

      if (!missing.isEmpty()) {
        throw new CompilationException(OperationEvaluator.class, "Identifier " + missing.get(0) + " not found!", operation.getLocation());
      }

      automata.add((Automaton) interpreter.interpret("automata", operation.getFirstProcess(), getNextOperationId(), processMap, context));
      automata.add((Automaton) interpreter.interpret("automata", operation.getSecondProcess(), getNextOperationId(), processMap, context));


      IOperationInfixFunction funct = instantiateClass(operationsMap.get(operation.getOperation().toLowerCase()));
      if (funct == null) {
        throw new CompilationException(getClass(), "The given operation is invaid: "
            + operation.getOperation(), operation.getLocation());
      }

      boolean result = funct.evaluate(automata);

      if (operation.isNegated()) {
        result = !result;
      }
      results.add(new OperationResult(operation.getFirstProcess(), operation.getSecondProcess(), firstId, secondId, operation.getOperation(), null, operation.isNegated(), result, ""));
    }
    return results;
  }

  static List<String> collectIdentifiers(ASTNode process) {
    List<String> ids = new ArrayList<>();
    collectIdentifiers(process, ids);
    return ids;
  }

  /**
   * A recursive search for finding identifiers in an ast
   *
   * @param process the ast node that has identifiers in it that are to be collected
   * @param ids     the returned collection
   */
  private static void collectIdentifiers(ASTNode process, List<String> ids) {
    if (process instanceof IdentifierNode) {
      ids.add(((IdentifierNode) process).getIdentifier());
    }

    if (process instanceof ChoiceNode) {
      collectIdentifiers(((ChoiceNode) process).getFirstProcess(), ids);
      collectIdentifiers(((ChoiceNode) process).getSecondProcess(), ids);
    }
    if (process instanceof CompositeNode) {
      collectIdentifiers(((CompositeNode) process).getFirstProcess(), ids);
      collectIdentifiers(((CompositeNode) process).getSecondProcess(), ids);
    }
    if (process instanceof FunctionNode) {
      ((FunctionNode) process).getProcesses().forEach(p -> collectIdentifiers(p, ids));
    }
    if (process instanceof IfStatementNode) {
      collectIdentifiers(((IfStatementNode) process).getTrueBranch(), ids);
      if (((IfStatementNode) process).hasFalseBranch()) {
        collectIdentifiers(((IfStatementNode) process).getFalseBranch(), ids);
      }
    }

    if (process instanceof SequenceNode) {
      collectIdentifiers(((SequenceNode) process).getTo(), ids);
    }
  }

  static String findIdent(ASTNode firstProcess, String code) {
    Location loc = firstProcess.getLocation();
    String[] lines = code.split("\\n");
    lines = Arrays.copyOfRange(lines, loc.getLineStart() - 1, loc.getLineEnd());
    if (loc.getLineEnd() != loc.getLineStart()) {
      lines[0] = lines[0].substring(loc.getColStart() - 1);
      lines[lines.length - 1] = lines[lines.length - 1].substring(0, loc.getColEnd() - 2);
    } else {
      lines[0] = lines[0].substring(loc.getColStart(), loc.getColEnd());
    }
    return String.join("", lines);
  }

  private String getNextOperationId() {
    return "op" + operationId++;
  }

  private void reset() {
    operationId = 0;
  }
}
