package mc.compiler;

import mc.compiler.ast.OperationNode;
import mc.exceptions.CompilationException;
import mc.process_models.ProcessModel;
import mc.process_models.automata.Automaton;
import mc.process_models.automata.operations.AutomataOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by sheriddavi on 27/01/17.
 */
public class OperationEvaluator {

  private int operationId;

  private AutomataOperations automataOperations;

  public OperationEvaluator(){
    this.automataOperations = new AutomataOperations();
  }

  public List<OperationResult> evaluateOperations(List<OperationNode> operations, Map<String, ProcessModel> processMap, Interpreter interpreter) throws CompilationException {
    reset();
    List<OperationResult> results = new ArrayList<OperationResult>();

    for(OperationNode operation : operations){
      boolean firstFound = processMap.containsKey(OperationResult.getIdent(operation.getFirstProcess()));
      boolean secondFound = processMap.containsKey(OperationResult.getIdent(operation.getSecondProcess()));
      if (firstFound && secondFound) {
        List<Automaton> automata = new ArrayList<Automaton>();
        automata.add((Automaton) interpreter.interpret("automata", operation.getFirstProcess(), getNextOperationId(), processMap));
        automata.add((Automaton) interpreter.interpret("automata", operation.getSecondProcess(), getNextOperationId(), processMap));
        if (Objects.equals(operation.getOperation(), "traceequivilant")) {
            List<Automaton> automata1 = new ArrayList<>();
            for (Automaton a: automata) {
                automata1.add(new AutomataOperations().nfa2dfa(a));
            }
            automata = automata1;
        }
        boolean result = automataOperations.bisimulation(automata);

        if (operation.isNegated()) {
          result = !result;
        }
        results.add(new OperationResult(operation.getFirstProcess(),operation.getSecondProcess(),operation.getOperation(),result));
      } else {
        results.add(new OperationResult(operation.getFirstProcess(),operation.getSecondProcess(),operation.getOperation(),firstFound,secondFound));
      }
    }
    return results;
  }

  private String getNextOperationId(){
    return "op" + operationId++;
  }

  private void reset(){
    operationId = 0;
  }
}
