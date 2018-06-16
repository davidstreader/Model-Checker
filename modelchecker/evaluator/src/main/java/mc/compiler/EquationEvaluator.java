package mc.compiler;

import static mc.util.Utils.instantiateClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import mc.compiler.ast.OperationNode;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;
import mc.util.LogMessage;


public class EquationEvaluator {

  private int equationId;

  static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new HashMap<>();

  private HashMap<String, Integer> indexMap = new HashMap<>(); //  automaton name -> index in models
  public EquationEvaluator() {

  }

  /**
   *
   * @param processMap  a list of automaton defined
   * @param operations  a list of the operations - one for each equation
   * @param code        used for error reporting
   * @param z3Context
   * @param messageQueue
   * @return   The EquationReturn - list of results
   * @throws CompilationException
   */
  public EquationReturn evaluateEquations(Map<String, ProcessModel> processMap,
                                          List<OperationNode> operations,
                                          String code, com.microsoft.z3.Context z3Context, BlockingQueue<Object> messageQueue)
    throws CompilationException, InterruptedException {
    reset();
      List<ProcessModel> processes = processMap.values().stream().collect(Collectors.toList());
      String ps = processMap.values().stream().map(x->x.getId()).collect(Collectors.joining(" "));
    List<OperationResult> results = new ArrayList<>();
    Map<String, ProcessModel> toRender = new ConcurrentSkipListMap<>();
      System.out.println("evaluateEquations "+operations.size()+" processes "+processes.size()+" " +ps);
      // build Map automaton name -> index in models
// Stops us having to search for the model each time with a loop (
      for (int i = 0; i < processes.size(); i++) {
          indexMap.put(processes.get(i).getId(), i);
      }
/*
   For each equation
 */
      for (OperationNode operation : operations) {
          System.out.println("  op "+ operation.getOperation()+" "+operation.getOperationType());
      // ONCE per equation! NOTE the state space needs to be clean
        Petrinet.netId = 0;  // hard to debug with long numbers and nothing stored
      ModelStatus status = new ModelStatus();
      //Generic ids defined in the equation block
            /*eg
                equation {
                    X~Y.
                }
                where X and Y are the automaton/process
             */
      String firstId = OperationEvaluator.findIdent(operation.getFirstProcess(), code);
      String secondId = OperationEvaluator.findIdent(operation.getSecondProcess(), code);
      List<String> firstIds = OperationEvaluator.collectIdentifiers(operation.getFirstProcess());
      List<String> secondIds = OperationEvaluator.collectIdentifiers(operation.getSecondProcess());

      if (processes.size() > 0) {

        List<String> testingSpace = new ArrayList<>(); // The total number of unqiue  places in the equation
        firstIds.stream().filter(id -> !testingSpace.contains(id)).forEach(testingSpace::add);
        secondIds.stream().filter(id -> !testingSpace.contains(id)).forEach(testingSpace::add);
// at this point we have the list of variables and
          //*** I think this could be moved to before call
          if (testingSpace.size() > 3) {
              messageQueue.add(new LogMessage("\nWith this many variables you'll be waiting the rest of your life for this to complete\n.... good luck"));
          }
          if (testingSpace.size() > processes.size()) {
              throw new CompilationException(getClass(), "Not enough defined automaton to fill test space");
          }
       int totalPermutations = (int) Math.pow(processes.size(), testingSpace.size());
        ArrayList<String> failures = testUserdefinedModel(processMap, testingSpace,
                           status, operation, z3Context, messageQueue);

        results.add(new OperationResult(operation.getFirstProcess(), operation.getSecondProcess(),
             firstId, secondId, operation.getOperation(),
             failures, operation.isNegated(), status.passCount == totalPermutations,
            status.passCount + "/" + totalPermutations));

      } else {
        throw new CompilationException(getClass(), "No processes defined for equation block to work with");
      }
    }
      System.out.println("Equation processes size "+processes.size());
    return new EquationReturn(results, toRender);
  }


  /**
   *  Called onece per equation
   * @param processMap       This is the list of defined processes
   * @param testingSpace Variables in equation  X|Y~Y|X has testingSpace=[X,Y]
   * @param status
   * @param operation  ONE Equation - operation = two processes and name of operation
   * @param context
   * @param messageQueue
   * @return
   * @throws CompilationException
   */
  private ArrayList<String> testUserdefinedModel(Map<String, ProcessModel> processMap,
                                                 List<String> testingSpace,
                                                 ModelStatus status,
                                                OperationNode operation,
                                                 com.microsoft.z3.Context context, BlockingQueue<Object> messageQueue)
      throws CompilationException, InterruptedException {
      List<ProcessModel> models = processMap.values().stream().collect(Collectors.toList());
     boolean r = false;
    ArrayList<String> failedEquations = new ArrayList<>();
 // moved to inside while loop    Interpreter interpreter = new Interpreter();




//***

    HashMap<String, ProcessModel> idMap = new HashMap<>(); // Which model substitutes for which equation automaton
    for (String variableId : testingSpace) // Set up starting map all variables replaced by the first model
    { idMap.put(variableId, models.get(0));}
      OperationEvaluator oE = new OperationEvaluator();
    while (true) {
      //Once per ground equation (operation)

        System.out.println("testUDM "+idMap.keySet());
      Interpreter interpreter = new Interpreter(); // build the automata from the AST
        String exceptionInformation = "";
  /*
      ArrayList<ProcessModel> createdAutomaton = new ArrayList<>();
      try {
        createdAutomaton.add((Automaton) interpreter.interpret("automata", operation.getFirstProcess(), getNextEquationId(), idMap, z3Context));
        createdAutomaton.add((Automaton) interpreter.interpret("automata", operation.getSecondProcess(), getNextEquationId(), idMap, z3Context));

      } catch (Exception e) {
        e.printStackTrace();
        exceptionInformation = e.getClass().getName();
      }

      //Using the name of the operation, this finds the appropriate function to use in operations/src/main/java/mc/operations/
      String currentOperation = operation.getOperation().toLowerCase();

 boolean temp = instantiateClass(operationsMap.get(currentOperation)).evaluate(createdAutomaton);
      boolean result = exceptionInformation.length() == 0 && temp;
*/


        r = oE.evalOp(operation,idMap,interpreter,context);
        //System.out.println("operation "+ firstId+" "+operation.getOperation()+" "+secondId+" "+r);
        if (!r) exceptionInformation += "opps";
      if (operation.isNegated()) { r = !r; }

      if (r) {
        status.passCount++;
      } else {

        status.failCount++;
        String failOutput = "";

        if(exceptionInformation.length() > 0)
            failOutput += exceptionInformation + "\n";
        for (String key : idMap.keySet()) {
          failOutput += "$" + key + "=" + idMap.get(key).getId() + ", ";
        }
        failedEquations.add(failOutput);
      }

//If we've failed too many tests;
      if (status.failCount > 1) { return failedEquations;}  // end by failure

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
      if (done) { return failedEquations; }  //end of process permutations

      //Generate new permutation of provided models
      for (String variableId : testingSpace) {
        if (idMap.get(variableId).equals(models.get(models.size() - 1))) {
          if (testingSpace.get(testingSpace.size() - 1).equals(variableId)) {
            break;
          } else {
            idMap.put(variableId, models.get(0));
          }
        } else {
          int modelIndex = indexMap.get(idMap.get(variableId).getId());
          modelIndex++;
          idMap.put(variableId, models.get(modelIndex));
// System.out.println("Changing " +variableId+"->"+models.get(modelIndex).getId());
          break;  // only change one variable
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
    Map<String, ProcessModel> toRender;  // I can find no where this is being writen to!
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
