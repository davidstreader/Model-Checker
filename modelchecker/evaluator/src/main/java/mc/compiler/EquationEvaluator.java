package mc.compiler;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import com.microsoft.z3.Context;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import mc.compiler.ast.ImpliesNode;
import mc.compiler.ast.OperationNode;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.petrinet.Petrinet;
import mc.util.LogMessage;


public class EquationEvaluator {

  private int equationId;

  static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new HashMap<>();

  private HashMap<String, Integer> indexMap = new HashMap<>(); //  automaton name -> index in models
  private List<OperationResult> results = new ArrayList<>();
  private  List<ImpliesResult> impResults = new ArrayList<>();
  int totalPermutations = 0;
  List<ProcessModel> processes;
  public EquationEvaluator() {

    impResults = new ArrayList<>();
  }

  /**
   * @param processMap   a list of automaton defined
   * @param operations   a list of the operations - one for each equation
   * @param code         used for error reporting
   * @param z3Context
   * @param messageQueue
   * @param alpha
   * @return The EquationReturn - list of results
   * @throws CompilationException
   */
  public EquationReturn evaluateEquations(Map<String, ProcessModel> processMap,
                                          List<OperationNode> operations,
                                          String code, Context z3Context,
                                          BlockingQueue<Object> messageQueue, Set<String> alpha)
    throws CompilationException, InterruptedException {
    reset();
    processes = processMap.values().stream().collect(Collectors.toList());
    String ps = processMap.values().stream().map(x -> x.getId()).collect(Collectors.joining(" "));
    Map<String, ProcessModel> toRender = new ConcurrentSkipListMap<>();
    System.out.println("evaluateEquations " + operations.size() + " processes " + processes.size() + " " + ps);
    // build Map automaton name -> index in models
// Stops us having to search for the model each time with a loop (
    for (int i = 0; i < processes.size(); i++) {
      indexMap.put(processes.get(i).getId(), i);
    }
    System.out.println("Equation processes size " + processes.size());
/*
   For each equation => once for many ground equation
 */
    for (OperationNode operation : operations) {
      System.out.println("XX");
      if (operation instanceof  ImpliesNode) System.out.println("IMP "+((ImpliesNode) operation).toString());
      else System.out.println("TREE "+operation.myString());
      System.out.println("YY");

      evaluateEquation(processMap,operation,code,z3Context,messageQueue,alpha);
    }

    return new EquationReturn(results,impResults, toRender);
  }

/*  forall{X} P(X,Y,Z)  ==> Q(Y,Z)
              ==>
    forall{X}      Q(Y,Z)
    P(X,Y,Z)

  forall{X} Q(Y,Z)  ==> P(X,Y,Z)
            forall{X}
              ==>
       Q(Y,Z)       P(X,Y,Z)



  Evaluate a single equation. - Many operations - Many ground equations
   */
  private ModelStatus evaluateEquation(Map<String, ProcessModel> processMap,
                                          OperationNode operation,
                                          String code, com.microsoft.z3.Context z3Context,
                                          BlockingQueue<Object> messageQueue,
                                       Set<String> alpha)
    throws CompilationException, InterruptedException {
    System.out.println("  op " + operation.getOperation() + " eE " + operation.getOperationType());
   // System.out.println(operation.getFirstProcess().toString());
    // ONCE per equation! NOTE the state space needs to be clean
    Petrinet.netId = 0;  // hard to debug with long numbers and nothing stored
    ModelStatus status = new ModelStatus();

    List<String> firstIds = new ArrayList<>();
    List<String> secondIds;
    System.out.println("PINGO "+operation.getOperation());
    if (operation instanceof ImpliesNode) {
      firstIds = OperationEvaluator.collectIdentifiers(((ImpliesNode) operation).getFirstOperation());
      secondIds = OperationEvaluator.collectIdentifiers(((ImpliesNode) operation).getSecondOperation());
    } else {
      firstIds = OperationEvaluator.collectIdentifiers(operation.getFirstProcess());
      secondIds = OperationEvaluator.collectIdentifiers(operation.getSecondProcess());

    }
    System.out.println("First "+firstIds+" second "+secondIds);


      List<String> testingSpace = new ArrayList<>(); // The total number of unqiue  places in the equation
      firstIds.stream().filter(id -> !testingSpace.contains(id)).forEach(testingSpace::add);
      secondIds.stream().filter(id -> !testingSpace.contains(id)).forEach(testingSpace::add);
// at this point we have the list of variables and
      //*** I think this could be moved to before call

      if (testingSpace.size() > 3) {
        messageQueue.add(new LogMessage("\nWith this many variables you'll be waiting the rest of your life for this to complete\n.... good luck"));
      }

      int totalPermutations = (int) Math.pow(processes.size(), testingSpace.size());
      //WORK Done here once per equation many ground equations evaluated
      ArrayList<String> failures = testUserdefinedModel(processMap, testingSpace,
        status, operation, z3Context, messageQueue,alpha);

      // Process the results
      String firstId;
      String secondId;
      if (operation instanceof ImpliesNode) {
        OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
        firstId = OperationEvaluator.findIdent(o1, code);
        OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
        secondId = OperationEvaluator.findIdent(o2, code);
        System.out.println("** op "+operation.getOperation()+" **first " + firstId + " second " + secondId + "\n**");

        results.add(new OperationResult(((ImpliesNode) operation).getFirstOperation(), ((ImpliesNode) operation).getSecondOperation(),
          firstId, secondId, operation.getOperation(),
          failures, operation.isNegated(), status.passCount == totalPermutations,
          status.passCount + "/" + totalPermutations));

      } else {
        firstId = OperationEvaluator.findIdent(operation.getFirstProcess(), code);
        secondId = OperationEvaluator.findIdent(operation.getSecondProcess(), code);

        System.out.println("** op "+operation.getOperation()+" **first " + firstId + " second " + secondId + "\n**");
        results.add(new OperationResult(operation.getFirstProcess(), operation.getSecondProcess(),
          firstId, secondId, operation.getOperation(),
          failures, operation.isNegated(), status.passCount == totalPermutations,
          status.passCount + "/" + totalPermutations));
      }
 return status;
  }
  /**
   * Called onece per equation
   *
   * @param processMap   This is the list of defined processes
   * @param testingSpace Variables in equation  X|Y~Y|X has testingSpace=[X,Y]
   * @param status
   * @param operation    ONE Equation - operation = two processes and name of operation
   *                     OR ==>  and two Equation operations
   * @param context
   * @param messageQueue
   * @return
   * @throws CompilationException
   */
  private ArrayList<String> testUserdefinedModel(Map<String, ProcessModel> processMap,
                                                 List<String> testingSpace,
                                                 ModelStatus status,
                                                 OperationNode operation,
                                                 com.microsoft.z3.Context context,
                                                 BlockingQueue<Object> messageQueue,
                                                 Set<String> alpha)
    throws CompilationException, InterruptedException {
    List<ProcessModel> models = processMap.values().stream().collect(Collectors.toList());
    boolean r = false;
    ArrayList<String> failedEquations = new ArrayList<>();
    // moved to inside while loop    Interpreter interpreter = new Interpreter();

//***
    // Variable -> process  substitution mapping
    HashMap<String, ProcessModel> idMap = new HashMap<>();
    System.out.println("testingSpace "+testingSpace);
    for (String variableId : testingSpace) // Set up starting map all variables replaced by the first model
    {
      idMap.put(variableId, models.get(0));  // start with all variable having the same model
      System.out.println("idMap ("+variableId+") = "+ idMap.get(variableId).getId());
    }
    OperationEvaluator oE = new OperationEvaluator();

    while (true) {
      //Once per ground equation (operation)
      //display
      String out = "";
      for (String key : idMap.keySet()) {
        out += key + "->" + idMap.get(key).getId()+" ";
      }
      System.out.println("idMap " + out);
      //end display
      Interpreter interpreter = new Interpreter(); // build the automata from the AST
      String exceptionInformation = "";

//evaluate the operation on a ground equation (once per operation)
      if (operation instanceof ImpliesNode) {

        OperationNode o1 =  (OperationNode) ((ImpliesNode) operation).getFirstOperation();
        OperationNode o2 =  (OperationNode) ((ImpliesNode) operation).getSecondOperation();
        boolean  or1 = oE.evalOp(o1,idMap, interpreter, context,alpha);
        boolean  or2 = oE.evalOp(o2,idMap, interpreter, context,alpha);
        r = (! or1) || or2;  // A -> B  EQUIV  not A OR B
        System.out.println("or1 res "+or1+"or2 res "+or2);
      } else {
        r = oE.evalOp(operation, idMap, interpreter, context,alpha);
      }
     // r = oE.evalOp(operation, idMap, interpreter, context);
      //System.out.println("operation "+ firstId+" "+operation.getOperation()+" "+secondId+" "+r);

      if (operation.isNegated()) {
        r = !r;
      }
      //if (!r) exceptionInformation += "opps";
      if (r) {
        status.passCount++;
      } else {

        status.failCount++;
        String failOutput = "";

        if (exceptionInformation.length() > 0)
          failOutput += exceptionInformation + "\n";
        for (String key : idMap.keySet()) {
          failOutput += "$" + key + "=" + idMap.get(key).getId() + ", ";
        }
        failedEquations.add(failOutput);
      }

//If we've failed too many tests;
      if (status.failCount > 1) {
        return failedEquations;
      }  // end by failure

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
      }  //end of process permutations

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
    List<ImpliesResult> impResults;
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
