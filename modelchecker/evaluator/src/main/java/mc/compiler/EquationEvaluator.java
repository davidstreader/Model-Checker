package mc.compiler;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import com.microsoft.z3.Context;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import mc.compiler.ast.ForAllNode;
import mc.compiler.ast.ImpliesNode;
import mc.compiler.ast.OperationNode;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.petrinet.Petrinet;
import mc.util.LogMessage;


public class EquationEvaluator {

  private int equationId;

  static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new TreeMap<>();

  private Map<String, Integer> indexMap = new TreeMap<>(); //  automaton name -> index in models
  private List<OperationResult> results = new ArrayList<>();
  private List<ImpliesResult> impResults = new ArrayList<>();
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
    //System.out.println("Equation processes size " + processes.size());
/*
   For each equation => once for many ground equation
 */
    for (OperationNode operation : operations) {
      System.out.println("XX");
      if (operation == null) System.out.println("operation==null in evaluateEquations");
      else System.out.println(operation.myString());


      evaluateEquation(processMap, operation, code, z3Context, messageQueue, alpha);
    }

    return new EquationReturn(results, impResults, toRender);
  }

  /*  forall{X} (P(X,Y,Z))  ==> Q(Y,Z)       forall{X} (Q(Y,Z)  ==> P(X,Y,Z))
                ==>                                     forall{X}
      forall{X}      Q(Y,Z)                                ==>
      P(X,Y,Z)                                     Q(Y,Z)       P(X,Y,Z)

     At the top level variables Y and Z need to be instantiated
     the ground variable X is only instantiated when the forall operation is evaluated.

    Evaluate a single equation. - Many operations - Many ground equations
     */
  private ModelStatus evaluateEquation(Map<String, ProcessModel> processMap,
                                       OperationNode operation,
                                       String code, com.microsoft.z3.Context z3Context,
                                       BlockingQueue<Object> messageQueue,
                                       Set<String> alpha)
    throws CompilationException, InterruptedException {
    // ONCE per equation! NOTE the state space needs to be clean
    Petrinet.netId = 0;  // hard to debug with long numbers and nothing stored
    ModelStatus status = new ModelStatus();
    List<ProcessModel> models = processMap.values().stream().collect(Collectors.toList());
    //At the top level forall is no more than making implicit forall explicit
    if (operation instanceof ForAllNode) {
      System.out.println("Top level forall is redundent!");
      return evaluateEquation(processMap,
        ((ForAllNode) operation).getOp(), code, z3Context, messageQueue, alpha);
    }
    //collect the free variables
    System.out.println("in  evaluateEquation " + operation.myString());
    List<String> globlFreeVariables = collectFreeVariables(operation);
    System.out.println("globalFreeVariables " + globlFreeVariables);
    if (globlFreeVariables.size() > 3) {
      messageQueue.add(new LogMessage("\nWith this many variables you'll be waiting the rest of your life for this to complete\n.... good luck"));
    }
    Map<String, ProcessModel> globalFreeVar2Model = new TreeMap<>();
    for (String variableId : globlFreeVariables) // Set up starting map all variables replaced by the first model
    {
      globalFreeVar2Model.put(variableId, models.get(0));  // start with all variable having the same model
      //System.out.println("idMap (" + variableId + ") = " + idMap.get(variableId).getId());
    }
    int totalPermutations = (int) Math.pow(processes.size(), globlFreeVariables.size());
    //WORK Done here once per equation many ground equations evaluated
    ArrayList<String> failures = testUserdefinedModel(processMap,
      models,
      status,
      operation,
      z3Context,
      messageQueue,
      alpha,     // Only used for broadcast semantics
      globalFreeVar2Model
    );

    // Process the results  DISPLAYED in UserInterfaceController.updateLogText
    String firstId;
    String secondId;


    if (operation instanceof ImpliesNode) {
      OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
      firstId = o1.myString(); // OperationEvaluator.findIdent(o1, code);
      OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
      secondId = o2.myString(); // OperationEvaluator.findIdent(o2, code);
      System.out.println("** op " + operation.myString() + " **first " + firstId + " second " + secondId + "\n**");

      results.add(new OperationResult(
        ((ImpliesNode) operation).getFirstOperation(),
        ((ImpliesNode) operation).getSecondOperation(),
        firstId, secondId,
        operation.getOperation(),
        failures, operation.isNegated(), status.passCount == totalPermutations,
        status.passCount + "/" + totalPermutations));

    } else {
      firstId = operation.getFirstProcess().myString(); // OperationEvaluator.findIdent(operation.getFirstProcess(), code);
      secondId = operation.getSecondProcess().myString(); // OperationEvaluator.findIdent(operation.getSecondProcess(), code);

      System.out.println("** op " + operation.myString() + " **first " + firstId + " second " + secondId + "\n**");
      results.add(new OperationResult(
        operation.getFirstProcess(),
        operation.getSecondProcess(),
        firstId, secondId,
        operation.getOperation(),
        failures, operation.isNegated(), status.passCount == totalPermutations,
        status.passCount + "/" + totalPermutations));
    }
    return status;
  }


  /**
   * Called onece per equation with globalFreeVarMap
   * Iterate over model space
   * Recurse down operation tree passing the freeVarMap
   * when all vars instantiated evaluate
   *
   * @param processMap   This is the list of defined processes
   * @param status
   * @param operation    ONE Equation - operation = two processes and name of operation
   *                     OR ==>  and two Equation operations
   * @param context
   * @param messageQueue
   * @return
   * @throws CompilationException
   */
  private ArrayList<String> testUserdefinedModel(Map<String, ProcessModel> processMap,  //Domain
                                                 List<ProcessModel> models,
                                                 ModelStatus status,  //used to RETURN results
                                                 OperationNode operation,
                                                 com.microsoft.z3.Context context,
                                                 BlockingQueue<Object> messageQueue,
                                                 Set<String> alpha,
                                                 Map<String, ProcessModel> outerFreeVariabelMap  //used in forAll{x}
  )
    throws CompilationException, InterruptedException {
    List<String> freeVariables = outerFreeVariabelMap.keySet().stream().collect(Collectors.toList());    // free variables

    System.out.println("Satrting testUserDefinedModel status.id " + status.getId() + " " + operation.myString() + " " + processMap.keySet() + " " + asString(outerFreeVariabelMap));
    String lastModel = models.get(models.size() - 1).getId();
    boolean r = false;
    ArrayList<String> failedEquations = new ArrayList<>();
    // moved to inside while loop    Interpreter interpreter = new Interpreter();


    OperationEvaluator oE = new OperationEvaluator();
    int i = 0;
    while (true) { //Once per ground equation (operation)  Assumes && hence short circuit on false
      System.out.println("testUserdefinedModel " + i++);

      if (operation instanceof ForAllNode) {    //  FORALL
        System.out.println("Local forALL ");
        // Free Variable -> process  substitution mapping
        Map<String, ProcessModel> inerfreeVar2Model = new TreeMap<>(); //Only used to expand the variable map
        if (outerFreeVariabelMap.size() > 0) {
          for (String key : outerFreeVariabelMap.keySet()) {
            inerfreeVar2Model.put(key, outerFreeVariabelMap.get(key));
          }
        }
        List<String> localBound = ((ForAllNode) operation).getBound();
        OperationNode localOp = ((ForAllNode) operation).getOp();
        ModelStatus localStatus = new ModelStatus();
// build freeVar2Model for FIRST evaluation
        System.out.println("Evaluate forall " + localBound);
        for (String b : localBound) {
          inerfreeVar2Model.put(b, models.get(0));
        }
        ArrayList<String> failures = testUserdefinedModel(processMap,
          models,
          localStatus,
          localOp,
          context,
          messageQueue,
          alpha,     // Only used for broadcast semantics never writen to
          inerfreeVar2Model  //call test again with expanded variable map
        );
// must pass
        System.out.println("Local Status fail " + localStatus.getFailCount() + " passed " + localStatus.passCount);
        System.out.println("Local Status failure " + failures);
        if (localStatus.failCount > 0) {
          status.setFailCount(localStatus.failCount);  //Fail must return
          return failedEquations;
        }
      } else if (operation instanceof ImpliesNode) {    //IMPLIES
        System.out.println("Implies " + operation.myString());
        ModelStatus status1 = new ModelStatus();
        ModelStatus status2 = new ModelStatus();
        boolean or1 = false;
        boolean or2 = false;
        if (((ImpliesNode) operation).getFirstOperation() instanceof ForAllNode) {
          //to asses short circuit evaluate 2 First
          OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
          System.out.println("implies evaluate 2 first " + o2.myString());
          List<String> failures2 = testUserdefinedModel(processMap,
            models,
            status2,
            o2,
            context,
            messageQueue,
            alpha,     // Only used for broadcast semantics
            outerFreeVariabelMap
          );
          System.out.println("status2 = " + status2.myString());
          or2 = status2.failCount == 0;
          if (or2 == true) {
            System.out.println("Short circuit Implies 2 == true");
            status.failCount = 0; //force success
            r = true;
          } else {
            OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
            System.out.println("implies now evaluate 1 " + o1.myString());
            List<String> failures1 = testUserdefinedModel(processMap,
              models,
              status1,
              o1,
              context,
              messageQueue,
              alpha,     // Only used for broadcast semantics
              outerFreeVariabelMap
            );
            System.out.println("status1 = " + status1.myString());
            or2 = status1.failCount == 0;
            r = (!or1) || or2;  // A -> B  EQUIV  not A OR B
            status.setFailCount(status1.failCount);
            if (status.failCount >0) return failedEquations; //Fail must return
          }
        } else {  //short circuit evaluate 1 First
          OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
          System.out.println("implies evaluate 1 first " + o1.myString());
          List<String> failures1 = testUserdefinedModel(processMap,
            models,
            status1,
            o1,
            context,
            messageQueue,
            alpha,     // Only used for broadcast semantics
            outerFreeVariabelMap
          );

          System.out.println(status1.myString());
          or1 = status1.failCount == 0;
          if (or1 == false) {  //Short Circuit
            System.out.println("Short circuit Implies 1 == false hence return true");
            r = true;
            status.setFailCount(0);
          } else { //Not shortr Circuit so evaluate other part of Implies
            OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
            System.out.println("implies now evaluate 2  " + o2.myString());
            List<String> failures2 = testUserdefinedModel(processMap,
              models,
              status2,
              o2,
              context,
              messageQueue,
              alpha,     // Only used for broadcast semantics
              outerFreeVariabelMap
            );
            System.out.println("status2 = " + status2.myString());
            or2 = status2.failCount == 0;
            r = (!or1) || or2;  // A -> B  EQUIV  not A OR B
            status.setFailCount(status2.failCount);
            if (status.failCount >0) return failedEquations; //Fail must return
          }
        }

        System.out.println("implies Node result " + r + "failcount " + status.myString());

      } else {  //OPERATION Evaluate

        // build the automata from the AST  or look up known automata
        Interpreter interpreter = new Interpreter();
        //  System.out.println("testing "+ operation.myString()+" "+ asString(freeVar2Model));
        r = oE.evalOp(operation, outerFreeVariabelMap, interpreter, context, alpha);
        // r = oE.evalOp(operation, idMap, interpreter, context);
        System.out.println("Processed operation " + operation.myString() + " " + r);

        if (operation.isNegated()) {
          r = !r;
        }
        //Adding to results  NOTE must use the outerFreeVar2Model
        String exceptionInformation = "";
        if (r) {
          status.passCount++;
        } else {
          status.failCount++;
          String failOutput = "";
          if (exceptionInformation.length() > 0)
            failOutput += exceptionInformation + "\n";
          for (String key : outerFreeVariabelMap.keySet()) {
            failOutput += key + "=" + outerFreeVariabelMap.get(key).getId() + ", ";
          }
          failedEquations.add(failOutput);
          System.out.println("failOutput " + failOutput);
        }

//If we've failed too many operation tests;
        if (status.failCount > 0) {
          System.out.println("Failing " + operation.myString() + " " + asString(outerFreeVariabelMap));

          return failedEquations;
        }  // end by failure

        status.doneCount++;
        status.timeStamp = System.currentTimeMillis();
        //if all elements in the map are the same final element in models, then end the test.

        System.out.println("Passed " + operation.myString() + " " + asString(outerFreeVariabelMap));
      }
      // Success only fall through so generate new permutation

      if (!(operation instanceof ForAllNode) && !(operation instanceof ImpliesNode)) {
        //Generate new permutation of provided models
        for (String variableId : freeVariables) {
          //System.out.println("free variable "+variableId+ " -> "+ freeVar2Model.get(variableId).getId());
          if (outerFreeVariabelMap.get(variableId).getId().equals(lastModel)) { // if last model
            if (freeVariables.get(outerFreeVariabelMap.size() - 1).equals(variableId)) {
              System.out.println("Passed All " + operation.myString());
              return failedEquations;  // stop if last variable points to last model
            } else {
              outerFreeVariabelMap.put(variableId, models.get(0)); // reset to first model
              //System.out.println("YYY Setting "+ variableId+"->"+idMap.get(variableId).getId());
            }
          } else {  // increase the first variable not pointing to last model
            int modelIndex = indexMap.get(outerFreeVariabelMap.get(variableId).getId());
            modelIndex++; // ERROR
            outerFreeVariabelMap.put(variableId, models.get(modelIndex));
            //System.out.println("XXX Setting "+ variableId+"->"+idMap.get(variableId).getId());
            break;  // only change one variable
          }
        }
      }

      //System.out.println("next  freeVar2Model "+ operation.myString()+" "+ asString(freeVar2Model));

    }

  }

  private String getNextEquationId() {
    return "eq" + equationId++;
  }

  private void reset() {
    equationId = 0;
  }
 /*
     Collect free variables
   */

  private List<String> collectFreeVariables(OperationNode operation) {
    List<String> firstIds;
    List<String> secondIds;
    System.out.println("collectFreeVariables " + operation.getClass().getSimpleName());
    if (operation instanceof ForAllNode) {
      firstIds = OperationEvaluator.collectIdentifiers(((ForAllNode) operation).getOp());
      secondIds = ((ForAllNode) operation).getBound();
      System.out.println("collect from forall bound " + secondIds + " of " + firstIds);
      firstIds.removeAll(secondIds);
      secondIds = new ArrayList<>();
    } else if (operation instanceof ImpliesNode) {
      System.out.println(operation.myString());
      firstIds = OperationEvaluator.collectIdentifiers(((ImpliesNode) operation).getFirstOperation());
      secondIds = OperationEvaluator.collectIdentifiers(((ImpliesNode) operation).getSecondOperation());
    } else {
      firstIds = OperationEvaluator.collectIdentifiers(operation.getFirstProcess());
      secondIds = OperationEvaluator.collectIdentifiers(operation.getSecondProcess());

    }
    System.out.println("First " + firstIds + " second " + secondIds);

    List<String> freeVariables = new ArrayList<>(); // The total number of unqiue  places in the equation
    firstIds.stream().filter(id -> !freeVariables.contains(id)).forEach(freeVariables::add);
    secondIds.stream().filter(id -> !freeVariables.contains(id)).forEach(freeVariables::add);
    // at this point we have the list of free variables
    return freeVariables;
  }

  //USED for debugging
  public static String asString(Map<String, ProcessModel> in) {
    String out = "";
    for (String key : in.keySet()) {
      out += key + "->" + in.get(key).getId() + ", ";
    }
    return out;
  }


  @Getter
  @AllArgsConstructor
  static class EquationReturn {
    List<OperationResult> results;
    List<ImpliesResult> impResults;
    Map<String, ProcessModel> toRender;  // I can find no where this is being writen to!
  }


}
