package mc.compiler;

import static mc.util.Utils.instantiateClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import mc.compiler.ast.OperationNode;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;


public class EquationEvaluator {

  private int equationId;

  static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new HashMap<>();

  public EquationEvaluator() {

  }

  public EquationReturn evaluateEquations(List<ProcessModel> processes, List<OperationNode> operations, String code, com.microsoft.z3.Context z3Context, BlockingQueue<Object> messageQueue) throws CompilationException {
    reset();
    List<OperationResult> results = new ArrayList<>();
    Map<String, ProcessModel> toRender = new ConcurrentSkipListMap<>();


    for (OperationNode operation : operations) {
      ModelStatus status = new ModelStatus();
      //Generic ids defined in the equation block
            /*eg
                equation(...) {
                    X~Y.
                }

                where X and Y are the automaton/process
             */
      String firstId = OperationEvaluator.findIdent(operation.getFirstProcess(), code);
      String secondId = OperationEvaluator.findIdent(operation.getSecondProcess(), code);
      List<String> firstIds = OperationEvaluator.collectIdentifiers(operation.getFirstProcess());
      List<String> secondIds = OperationEvaluator.collectIdentifiers(operation.getSecondProcess());

      if (processes.size() > 0) {

        List<String> testingSpace = new ArrayList<>(); // The total number of unqiue automaton places in the equation
        firstIds.stream().filter(id -> !testingSpace.contains(id)).forEach(testingSpace::add);
        secondIds.stream().filter(id -> !testingSpace.contains(id)).forEach(testingSpace::add);

        int totalPermutations = (int) Math.pow(processes.size(), testingSpace.size());
        ArrayList<String> failures = testUserdefinedModel(processes, testingSpace, status, operation, z3Context);

        results.add(new OperationResult(operation.getFirstProcess(), operation.getSecondProcess(), firstId,
            secondId, operation.getOperation(), failures, operation.isNegated(), status.passCount == totalPermutations,
            status.passCount + "/" + totalPermutations));

      } else {
        throw new CompilationException(getClass(), "No processes defined for equation block to work with");
      }
    }

    return new EquationReturn(results, toRender);
  }


  private ArrayList<String> testUserdefinedModel(List<ProcessModel> models, List<String> testingSpace, ModelStatus status,
                                                 OperationNode operation, com.microsoft.z3.Context z3Context)
      throws CompilationException {

    ArrayList<String> failedEquations = new ArrayList<>();

    Interpreter interpreter = new Interpreter();

    if (testingSpace.size() > 3) {
      throw new CompilationException(getClass(), "Too many variables defined in equation block (>3)");
    }

    if (testingSpace.size() > models.size()) {
      throw new CompilationException(getClass(), "Not enough defined automaton to fill test space");
    }

    HashMap<String, ProcessModel> idMap = new HashMap<>(); // Which model substitutes for which equation automaton
    for (String currentId : testingSpace) // Set up starting state
    {
      idMap.put(currentId, models.get(0));
    }

    HashMap<String, Integer> indexMap = new HashMap<>(); // Stops us having to search for the model each time with a loop
    for (int i = 0; i < models.size(); i++) {
      indexMap.put(models.get(i).getId(), i);
    }


    while (true) {
      String exceptionInformation = "";
      ArrayList<Automaton> createdAutomaton = new ArrayList<>();
      try {

        createdAutomaton.add((Automaton) interpreter.interpret("automata", operation.getFirstProcess(), getNextEquationId(), idMap, z3Context));
        createdAutomaton.add((Automaton) interpreter.interpret("automata", operation.getSecondProcess(), getNextEquationId(), idMap, z3Context));
      } catch (Exception e) {
        e.printStackTrace();
        exceptionInformation = e.getClass().getSimpleName();
      }

      //Using the name of the operation, this finds the appropriate function to use in operations/src/main/java/mc/operations/
      String currentOperation = operation.getOperation().toLowerCase();

      boolean result = exceptionInformation.length() == 0 && instantiateClass(operationsMap.get(currentOperation)).evaluate(createdAutomaton);

      if (operation.isNegated()) {
        result = !result;
      }

      if (result) {
        status.passCount++;
      } else {
        status.failCount++;
        String failOutput = "";


        if(exceptionInformation.length() > 0)
            failOutput += exceptionInformation + " ";

        for (String key : idMap.keySet()) {
          failOutput += "$" + key + "=" + idMap.get(key).getId() + ", ";
        }

        failedEquations.add(failOutput);
      }


      if (status.failCount > 2) {
        //If we've failed too many tests;
        return failedEquations;
      }

      status.doneCount++;

      status.timeStamp = System.currentTimeMillis();


      //if all elements in the map are the same final element in models, then end the test.
      boolean done = true;
      for (ProcessModel val : idMap.values()) {
        if (!val.equals(models.get(models.size() - 1))) {
          done = false;
          break;
        }
      }

      if (done) {
        return failedEquations;
      }

      //Generate new permutation of provided models
      for (String currentId : testingSpace) {

        if (idMap.get(currentId).equals(models.get(models.size() - 1))) {
          if (testingSpace.get(testingSpace.size() - 1).equals(currentId)) {

            break;
          } else {
            idMap.put(currentId, models.get(0));
          }
        } else {
          int modelIndex = indexMap.get(idMap.get(currentId).getId());
          modelIndex++;

          idMap.put(currentId, models.get(modelIndex));
          break;
        }
      }

    }

  }

  private String getNextEquationId() {
    return "eq" + equationId++;
  }

  private void reset() {
    equationId = 0;
  }

  @Getter
  @AllArgsConstructor
  static class EquationReturn {
    List<OperationResult> results;
    Map<String, ProcessModel> toRender;
  }

  @Data
  private static class ModelStatus {
    int passCount;
    int failCount;
    int id;
    int doneCount;
    long timeStamp;
  }
}
