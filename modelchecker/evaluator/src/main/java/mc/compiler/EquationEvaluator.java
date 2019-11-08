package mc.compiler;

import com.microsoft.z3.Context;
import mc.compiler.ast.AndNode;
import mc.compiler.ast.ForAllNode;
import mc.compiler.ast.ImpliesNode;
import mc.compiler.ast.OperationNode;
import mc.compiler.interpreters.PetrinetInterpreter;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.petrinet.Petrinet;
import mc.util.LogMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

//import mc.compiler.interpreters.PetrinetInterpreter;

public class EquationEvaluator {

  private int equationId;

  static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new TreeMap<>();

  private Map<String, Integer> indexMap = new TreeMap<>(); //  automaton name -> index in models
  private List<OperationResult> results = new ArrayList<>();
  //private List<ImpliesResult> impResults = new ArrayList<>();
  int totalPermutations = 0;
  int totalPassed = 0;
  int totalTests = 0;
  List<ProcessModel> processes;
  private Map<String, List<ProcessModel>> domains = new TreeMap<>(); //map Domain to list of processes


  private void buildFreshDomains(Map<String, ProcessModel> processMap) {
    //build domains name 2 process list ,using processMap
      domains = new TreeMap<>();  // must be fresh each time
    for (String k : processMap.keySet()) {
      // if (allVariables.contains(k)) continue;  //do not add variable to domain
      String[] parts = StringUtils.split(k, ':');
      String dom = "";
      if (parts.length == 0) {
        return;
      } else if (parts.length == 1) {
        dom = "*";
      } else if (parts.length > 1) {
        dom = parts[1];
      }
      if (domains.containsKey(dom)) {
        domains.get(dom).add(processMap.get(k));
      } else {
        domains.put(dom, new ArrayList<>(Arrays.asList(processMap.get(k))));
      }
    }
      //System.out.println("buildFreshDaomains "+domains2String());
  }

  String domains2String() {
      StringBuilder sb = new StringBuilder();

      sb.append("Instantiate working on  domains ");
      domains.keySet().stream().forEach(x -> {
        sb.append("\n    " + x + "->");
        domains.get(x).stream().forEach(key -> sb.append(key.getId() + ", "));
      });
      return sb.toString();
  }
/*  public EquationEvaluator() {

    impResults = new ArrayList<>();
  } */

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
                                          PetrinetInterpreter petrinetInterpreter,
                                          List<OperationNode> operations,
                                          String code, Context z3Context,
                                          BlockingQueue<Object> messageQueue, Set<String> alpha)
    throws CompilationException, InterruptedException {
    reset();
    //System.out.println("EVAL EQUS \n" + processMap.keySet());
/*
   Uses processes in all domains?
 */
    //System.currentTimeMillis();
    Date start = new Date();
    messageQueue.add(new LogMessage("    ##Equations  Starting##\n  "+start.toString(), true,
      false, null, -1, Thread.currentThread()));
    processes = processMap.values().stream().collect(Collectors.toList());
    //System.out.println("evaluateEquations processMap "+processMap.keySet());
//processMap will have Var:Dom -> currentProcesses set
    buildFreshDomains(processMap);


    //System.out.println(sb.toString());*/
    //System.out.println( domains.keySet().stream().reduce("",(x,y) ->(x+(domains.get(x).stream().map(ProcessModel::getId).reduce("",(u,v)->u+v+" "))+"\n")));
    Map<String, ProcessModel> toRender = new ConcurrentSkipListMap<>();
    //System.out.println("evaluateEquations " + operations.size() + " processes " + processes.size() + " " + asString(processMap));
    for (int i = 0; i < processes.size(); i++) {
      indexMap.put(processes.get(i).getId(), i);
    }
    //System.out.println("Equation processes size " + processes.size());
/*
   For each equation => once for many ground equation
 */

    for (OperationNode operation : operations) {
      /*if (operation == null) System.out.println("XX operation==null in evaluateEquations");
      else System.out.println("XX "+operation.myString()); */
      Map<String, ProcessModel> pMap = new TreeMap<>();// MUST make copy as changed by call
      processMap.keySet().stream().forEach(x -> pMap.put(x, processMap.get(x)));

      evaluateEquation(pMap, petrinetInterpreter, operation, code, z3Context, messageQueue, alpha);
    }
    Date stop = new Date();

    String log = "    ##Equations## " + totalPassed + "/" + totalTests+ "  "+getDifferenceDays(start,stop)+ "\n"+stop;
    messageQueue.add(new LogMessage(log, true,
      false, null, -1, Thread.currentThread()));

      //System.out.println("processMap.size() = "+ processMap.size());
    return new EquationReturn(results, toRender);
  }

  public static String getDifferenceDays(Date d1, Date d2) {
    long diff = d2.getTime() - d1.getTime();
    long diffSeconds = diff / 1000 % 60;
    long diffMinutes = diff / (60 * 1000) % 60;
    long diffHours = diff / (60 * 60 * 1000);

    return  diffHours+":"+diffMinutes+":"+diffSeconds;
  }
  /*  forall{X} (P(X,Y,Z))  ==> Q(Y,Z)       forall{X} (Q(Y,Z)  ==> P(X,Y,Z))
                ==>                                     forall{X}
      forall{X}      Q(Y,Z)                                ==>
      P(X,Y,Z)                                     Q(Y,Z)       P(X,Y,Z)

     At the top level variables Y and Z need to be instantiated
     the ground variable X is only instantiated when the forall operation is evaluated.

    Evaluate a single equation. - Many operations - Many ground equations
     */
  private void evaluateEquation(Map<String, ProcessModel> processMap,
                                PetrinetInterpreter petrinetInterpreter,
                                OperationNode operation,
                                String code, com.microsoft.z3.Context z3Context,
                                BlockingQueue<Object> messageQueue,
                                Set<String> alpha)
    throws CompilationException, InterruptedException {

    // ONCE per equation! NOTE the state space needs to be clean
    Petrinet.netId = 0;  // hard to debug with long numbers and nothing stored
    ModelStatus status = new ModelStatus();
    Stack<String> trace = new Stack<>();

    //collect the free variables
    //System.out.println("\nSTART - evaluateEquation " + operation.myString()+ "\nProcess map "+processMap.keySet());
    List<String> globlFreeVariables = collectFreeVariables(operation, processMap.keySet());
    //System.out.println("globalFreeVariables " + globlFreeVariables);  //Var:Dom
    if (!validateDomains(globlFreeVariables))
      throw new CompilationException(this.getClass(), "Variable with Domain not defined ", operation.getLocation());

    if (globlFreeVariables.size() > 3) {
      messageQueue.add(new LogMessage("\nWith this many variables you'll be waiting the rest of your life for this to complete\n.... good luck"));
    }
    Map<String, ProcessModel> globalFreeVar2Model = new TreeMap<>();

    //sets up the domains or use in looping
    Instantiate inst = new Instantiate(processMap, globlFreeVariables, globlFreeVariables);
    globalFreeVar2Model = inst.peek();
    //System.out.println("new inst "+inst.myString());

    int totalPermutations = inst.permutationCount();

    //WORK Done here once per equation many ground equations evaluated
    List<String> failures;
    try {
      failures = testUserdefinedModel(
        processMap,   // id + var  2 process map
        petrinetInterpreter,
        status,
        operation,
        inst,
        z3Context,
        trace,
        messageQueue,
        alpha,     // Only used for broadcast semantics
        globalFreeVar2Model,
        true
      );
    } catch (CompilationException e) {
      String emes = e.getMessage();
      throw new CompilationException(e.getClazz(), emes + " globalOp " + operation.myString() +
        "\n free " + freeVar2String(globalFreeVar2Model));
    }
    // Process the results  DISPLAYED in UserInterfaceController.updateLogText
    String shortImplies;

    if (status.impliesConclusionTrue > 0 || status.impliesAssumptionFalse > 0)
      shortImplies = " (implies short circuit ass/conc " +
        status.impliesAssumptionFalse + "/" + status.impliesConclusionTrue + ") ";
    else shortImplies = " ()";
    //System.out.println("END - evaluateEquation "+ operation.myString()+" "+failures+ " "+status.myString());
    results.add(new OperationResult(failures, status.passCount == totalPermutations,
      status.passCount + "/" + totalPermutations + shortImplies, operation));
    String f;
    if (failures.size() == 0) f = "";
    else f = "  " + failures;
    String logged = " " + operation.myString() + "\n        Simulations Passed " + " " + status.passCount + "/" + totalPermutations + shortImplies + f;
    messageQueue.add(new LogMessage(logged, true,
      false, null, -1, Thread.currentThread()));
    totalPassed = totalPassed + status.passCount;
    totalTests = totalTests + totalPermutations;

      //System.out.println("Equs ends with "+totalPassed+"/"+totalTests+"  "+inst.myString());
    return;
  }

  private String freeVar2String(Map<String, ProcessModel> var2Model) {
    StringBuilder sb = new StringBuilder();
    for (String key : var2Model.keySet()) {
      sb.append(key + " " + var2Model.get(key).getId() + ", ");
    }
    return sb.toString();
  }

  public boolean validateDomains(List<String> globlFreeVariables) {
    for (String varDom : globlFreeVariables) {
      String[] parts = StringUtils.split(varDom, ':');
      String dom = parts[1];
      if (!domains.containsKey(dom)) return false;
    }
    return true;
  }

  /**
   * Called onece per equation with globalFreeVarMap
   * Iterate over model space
   * Recurse down operation tree passing the freeVarMap
   * when all vars instantiated evaluate
   *
   * @param processMap           This is the list of defined processes
   * @param status
   * @param operation            ONE Equation - operation = two processes and name of operation
   *                             OR ==>  and two Equation operations
   * @param context              trace
   * @param messageQueue
   * @param alpha                -- broadcast
   * @param outerFreeVariabelMap map of instianted variables
   * @return
   * @throws CompilationException
   */
  private List<String> testUserdefinedModel(Map<String, ProcessModel> processMap,
                                            PetrinetInterpreter petrinetInterpreter,
                                            ModelStatus status,  //used to RETURN results
                                            OperationNode operation,
                                            Instantiate inst,
                                            com.microsoft.z3.Context context,
                                            Stack<String> trace,
                                            BlockingQueue<Object> messageQueue,
                                            Set<String> alpha,
                                            Map<String, ProcessModel> outerFreeVariabelMap,  //used in forAll{x}
                                            boolean updateFreeVariables  //allows ==> to controll when to move on
  )
    throws CompilationException, InterruptedException {
    List<String> freeVariables = outerFreeVariabelMap.keySet().stream().collect(Collectors.toList());    // free variables

/*    System.out.println("Starting testUserDefinedModel id "+status.getId()+" "+operation.myString()
    + " outer " + asString(outerFreeVariabelMap)
    +
     "\n   processMap " + processMap.keySet() +
     "\n   outer " + asString(outerFreeVariabelMap) + " pass " + status.passCount+
     "\n   inst"+inst.myString());
*/
    boolean r = false;
    ArrayList<String> failedEquations = new ArrayList<>();
    // moved to inside while loop    Interpreter interpreter = new Interpreter();


    OperationEvaluator oE = new OperationEvaluator();
    int i = 0;
    status.passCount = 0;
    while (true) { //Once per ground equation (operation)  Assumes && hence short circuit on false
      // outer free variable have been instantiated
      //     recurse  and more free vars generated by forall +
      //     after recurseion the  outerFree variable must be changed
      //System.out.println("    while loop " + i++ +" "+ asString(outerFreeVariabelMap)+"  status.passCount "+status.passCount);

      if (operation instanceof ForAllNode) {    //  FORALL
        //System.out.println("-- ForAll " + operation.myString() +" with "+ asString(outerFreeVariabelMap));
        // add outer free Vars to Process model
        // build inner free Vars
        List<String> localBound = ((ForAllNode) operation).getBound();
        //System.out.println("forall "+localBound);
        localBound = localBound.stream().map(x -> {
          if (x.contains(":")) return x;
          else return x + ":*";
        }).collect(Collectors.toList());
        List<String> allVariables = localBound.stream().collect(Collectors.toList());

        //System.out.println("pMap "+asString(processMap));
        Map<String, ProcessModel> boundVariabelMap = new TreeMap<>(); //Only used to expand the variable map
        if (outerFreeVariabelMap.size() > 0) {
          for (String key : outerFreeVariabelMap.keySet()) {
            //System.out.println("KEY "+key);
            if (key.endsWith(":*")) {
              String[] parts = key.split(":");
              String var = parts[0];
              if (localBound.stream().filter(x -> x.startsWith(var)).collect(Collectors.toList()).size() != 0) {
                //System.out.println("found "+key);
                if (processMap.containsKey(key)) {
                  processMap.remove(key);
                  //System.out.println("after remove "+asString(processMap));
                }
                continue; //skip variables to be instantiated
              }
              ;
            }
            processMap.put(key, outerFreeVariabelMap.get(key));
            allVariables.add(key);
          }
        }
        //System.out.println("forall var2 "+allVariables);
        //System.out.println("forall localBound "+localBound);
        //System.out.println("AFTER processMap "+asString(processMap));
        //System.out.println("    localBound " + localBound + " allVariables "+allVariables);
//Bind the local variables in the term
        for (String local : ((ForAllNode) operation).getBound()) {
          //System.out.println("local = "+local);
          //System.out.println(operation.myString());
          if (local.contains(":")) {
            String[] parts = local.split(":");
            String var = parts[0];
            //((ForAllNode)operation).setOp(((ForAllNode) operation).getOp().instantiate(var+":*",local)) ;
            ((ForAllNode) operation).setOp(((ForAllNode) operation).getOp().instantiate(local, local));
            //System.out.println("WHY?");
            //System.out.println(operation.myString());
          }
        }

        //System.out.println("domain Bound operation "+operation.myString());
        // Sets up the domains
        Instantiate forinst = new Instantiate(processMap, localBound, allVariables);
        //System.out.println("new forinst "+forinst.myString());
        OperationNode localOp = ((ForAllNode) operation).getOp();
        ModelStatus localStatus = new ModelStatus();
/* build freeVar2Model for FIRST evaluation
        for (String b : localBound) {
          boundVariabelMap.put(b, processes.get(0));
        }*/
        boundVariabelMap = forinst.peek();
        //System.out.println("Evaluate forall  with free var " + asString(boundVariabelMap));
        List<String> failures;
        try {
            failures = testUserdefinedModel(processMap,  //Global variable will be in here
            //models,
            petrinetInterpreter,
            localStatus,
            localOp,
            forinst,
            context,
            trace,
            messageQueue,
            alpha,     // Only used for broadcast semantics never writen to
            boundVariabelMap, // bound variable instantiation will be added to processMap in call
            true //call test again with expanded variable map
          );
        } catch (CompilationException e) {
            String log = "("+totalPassed + "/" + totalTests+")    ##Error## " + localOp.myString() +"\n   "+asString(outerFreeVariabelMap);
            messageQueue.add(new LogMessage(log, true,
                false, null, -1, Thread.currentThread()));

            System.out.println("forall with "+ asString(outerFreeVariabelMap));
          String emes = e.getMessage();
          throw new CompilationException(e.getClazz(), emes + //" localOp " + localOp.myString() +
            " bound " + freeVar2String(boundVariabelMap)+"\n");
        }
// must pass
        //System.out.println("  forall END of testUserDefine"+"  localStatus "+localStatus.myString());
        if (localStatus.failCount > 0) {
          status.setFailCount(localStatus.failCount);  //Fail must return
          status.setPassCount(localStatus.passCount);
          // status.setPassCount(localStatus.passCount);  // pass count not passed up term
          failures.add(" free "+asString(outerFreeVariabelMap));
          //System.out.println("Returning from forall "+ failures+" " +status.myString());
          List<String> failure = new ArrayList<>();
          failure.add("Forall fail");
          failure.addAll(failures);
          return failure;
        } else {
          trace = new Stack<String>();
          status.passCount++;

        }
        /*  *******************************  IMPLIES IMPLIES */
        //System.out.println("  status.passCount "+status.passCount);
      } else if (operation instanceof ImpliesNode) {    //IMPLIES
        //System.out.println("-- Implies " + operation.myString() +" with "+ asString(outerFreeVariabelMap));
        ModelStatus status1 = new ModelStatus();
        ModelStatus status2 = new ModelStatus();
        //System.out.println("First" + ((ImpliesNode) operation).getFirstOperation().myString());
        if (((ImpliesNode) operation).getFirstOperation() instanceof ForAllNode) {
          //to asses short circuit evaluate 2 First
          trace = new Stack<>(); // past traces not needed
          OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
          //System.out.println("implies evaluate 2 first " + o2.myString());
          List<String> failures2 = testUserdefinedModel(processMap,
            //models,
            petrinetInterpreter,
            status2,
            o2,
            inst,
            context,
            trace,
            messageQueue,
            alpha,     // Only used for broadcast semantics
            outerFreeVariabelMap, false  // will update free var map  BUT needed in second call
          );
          //System.out.println("evaluated 2 status = " + status2.myString());
          if (status2.failCount == 0) { // ==> TRUE   ~~> true
            //System.out.println("  @@@@@@ Short circuit XXX ==> true is true");
            status.failCount = 0; //force success
            failedEquations = new ArrayList<>();
            status.impliesConclusionTrue++;
            //status.setPassCount(status2.passCount);
            status.passCount++;
          } else {  //  XX ==> false  ~~> Not(XX)
            OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
            //System.out.println("implies now evaluate 1 " + o1.myString());
            List<String> failures1 = testUserdefinedModel(processMap,
              //models,
              petrinetInterpreter,
              status1,
              o1,
              inst,
              context,
              trace,
              messageQueue,
              alpha,     // Only used for broadcast semantics
              outerFreeVariabelMap, false
            );
            //System.out.println("evaluated  status1 = " + status1.myString());

            //System.out.println(" "+ r + " "+ status.myString());
            // status.setPassCount(status1.passCount); //pass count not passed up tree
            if (status1.failCount == 0) {// true ==> false   is false
              status.setFailCount(status2.failCount);
              //System.out.println("  @@@@@@ Failing 1 Implies 2->1 " + operation.myString() + " " + asString(outerFreeVariabelMap)+" fail "+failures2);
              List<String> failures = new ArrayList<>();
              failures.add("true ==> false");
              failures.addAll(failures2);
              return failures; //Fail must return the failures from 2 NOT 1
            } else {  // true ==>  true is true
              status.passCount++;
            }
          }
        } else {  //forall NOT on first so evaluate 1 First
          OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
          //System.out.println("Implies now evaluate 1 first " + o1.myString());
          List<String> failures1 = testUserdefinedModel(processMap,
            //models,
            petrinetInterpreter,
            status1,
            o1,
            inst,
            context,
            trace,
            messageQueue,
            alpha,     // Only used for broadcast semantics
            outerFreeVariabelMap, false
          );

          //System.out.println("Eval Implies 1 "+((ImpliesNode) operation).myString() +" Returning " + status1.myString());
          //System.out.println("XXXXEval Implies 1 "+o1.myString() +" Returning " + status1.myString());
          if (status1.failCount != 0) {  // false ==> X  ~~> true
            //System.out.println("  @@@@@@ Short circuit Implies 1 == false hence  true");

            status.setFailCount(0);
            failedEquations.clear();
            status.passCount++;
            status.impliesAssumptionFalse++;
          } else { //  true ==>X   ~~> X
            OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
            //System.out.println("implies now evaluate 2  " + o2.myString());
            trace = new Stack<>(); // past traces not needed
            List<String> failures2 = testUserdefinedModel(processMap,
              //models,
              petrinetInterpreter,
              status2,
              o2,
              inst,
              context,
              trace,
              messageQueue,
              alpha,     // Only used for broadcast semantics
              outerFreeVariabelMap, false
            );
            //System.out.println("XXXXEval Implies 2 "+o2.myString() +" Returning " + status1.myString());
            status.setFailCount(status2.failCount);
            //status.setPassCount(status2.passCount);  //pass count not passed up term
            if (status2.failCount > 0) {  //true==> false ~~> false
              status.failCount = status2.failCount;
              List<String> failures = new ArrayList<>();
              failures.add("true ==> false");
              failures.addAll(failures2);
              //System.out.println("  @@@@@@ Failing 2 Implies 1->2" + operation.myString() + " " + asString(outerFreeVariabelMap) + " fail " + failures2);
              return failures; //Fail must return
            } else {   //true ==> true ~~> true
              status.passCount++;
            }
          }
        }
        /*  ******************************* AND AND */

        //System.out.println("  status.passCount "+status.passCount);
      } else if (operation instanceof AndNode) {    //AND   AND
        //System.out.println("-- And " + operation.myString()+" with "+ asString(outerFreeVariabelMap));
        ModelStatus status1 = new ModelStatus();
        ModelStatus status2 = new ModelStatus();
        //evaluate 1 First
        OperationNode o1 = (OperationNode) ((AndNode) operation).getFirstOperation();
        //System.out.println("Implies now evaluate 1 first " + o1.myString());
        List<String> failures1 = testUserdefinedModel(processMap,
          //models,
          petrinetInterpreter,
          status1,
          o1,
          inst,
          context,
          trace,
          messageQueue,
          alpha,     // Only used for broadcast semantics
          outerFreeVariabelMap, false
        );

        //System.out.println("XXXXEval And 1 "+o1.myString() +" Returning " + status1.myString());
        if (status1.failCount > 0) {  // Short Circuit  false AND XX is false
          //System.out.println("  @@@@@@ Short circuit And 1 == false hence  false");
          status.failCount = status1.failCount;
          List<String> failure = new ArrayList<>();
          failure.add("&& failure");
          failure.addAll(failures1);
          return failure;
        } else { //Not short Circuit so evaluate other part of And
          OperationNode o2 = (OperationNode) ((AndNode) operation).getSecondOperation();
          //System.out.println("and now evaluate 2  " + o2.myString());
          List<String> failures2 = testUserdefinedModel(processMap,
            //models,
            petrinetInterpreter,
            status2,
            o2,
            inst,
            context,
            trace,
            messageQueue,
            alpha,     // Only used for broadcast semantics
            outerFreeVariabelMap, false
          );
          //System.out.println("XXXXEval And 2 "+o2.myString() +" Returning " + status2.myString());
          status.setFailCount(status2.failCount);
          //status.setPassCount(status2.passCount);  //pass count not passed up term
          if (status2.failCount > 0) {   // true && false is false
            //System.out.println("  @@@@@@ Failing 2 And 1->2" + o2.myString() + " " + asString(outerFreeVariabelMap) + " fail " + failures2);
            status.failCount = status2.failCount;
            List<String> failure = new ArrayList<>();
            failure.add("&& failure");
            failure.addAll(failures2);
            return failure;//Fail must return

          } else status.passCount++;
        }


        //System.out.println("And  passed " + operation.myString() + "  " + status.myString());

        /*  WORK   ************************   WORK   */
      } else {  //DO THE WORK and evaluate an OPERATION
        for (String key : outerFreeVariabelMap.keySet()) {
          //System.out.println("adding "+key+"->"+outerFreeVariabelMap.get(key).getId());
          processMap.put(key, outerFreeVariabelMap.get(key));
        }
        //System.out.println("*** evalop  "+processMap.keySet().stream().map(x->x+"->"+processMap.get(x).getId()).reduce((x,y)->x+" "+y));
        try {
            r = oE.evalOp(operation, processMap, petrinetInterpreter, context, alpha, trace);
        } catch(CompilationException e) {
            System.out.println("\n-- EVAL " + operation.myString()+" with "+ asString(outerFreeVariabelMap));
            System.out.println("  Inst "+inst.myString());
            System.out.println("ProcessMap "+processMap.keySet()+
                inst.myString());
            System.out.println(e.toString());
            throw e;
            // r = oE.evalOp(operation, idMap, interpreter, context);
          }
        if (operation.isNegated()) {
          r = !r;
        }
        //Adding to results  NOTE must use the outerFreeVar2Modelelse {
        //trace.sort(Collections.reverseOrder());


        if (r) {
          status.passCount++;
        } else {
          status.failCount++;
          String exceptionInformation = operation.myString();
          exceptionInformation += " trace = " + trace.toString()+", var ";
          exceptionInformation += asString(outerFreeVariabelMap);

          failedEquations.add(exceptionInformation);
          //System.out.println("failOutput " + exceptionInformation);
          return failedEquations;
        }

        status.doneCount++;
        status.timeStamp = System.currentTimeMillis();
        //if all elements in the map are the same final element in models, then end the test.
        //System.out.println("Passed " + operation.myString() + " " + asString(outerFreeVariabelMap));
      }

      // Success only fall through so generate new permutation
      //System.out.println("  @@@@@@ Fallthrough tick "+ operation.myString());
      //System.out.println("Fallthrough " + status.myString()+" outerFV "+ asString(outerFreeVariabelMap));
      //System.out.println(inst.peek());
      if (freeVariables.size() == 0) return new ArrayList<>(); // called with a ground term so no looping
      if (updateFreeVariables) { //A ==> B first evaluation (AorB) must not update freevariable map
        if (inst.end()) return new ArrayList<>();
        outerFreeVariabelMap = inst.next();  //this will iterate untill end
        //System.out.println("looping "+i++ +inst.myString());
      } else { // not updating free Vars means do once as part of implies
        return new ArrayList<>();
      }
      //System.out.println(" operation "+ operation.myString()+" passed "+ status.passCount + " next "+ asString(outerFreeVariabelMap));

    }

  }

  private String getNextEquationId() {
    return "eq" + equationId++;
  }

  private void reset() {
    equationId = 0;
  }
 /*
     Collect free variables  by building the identifiers and removing thoes
     defined in processes
   */

  private List<String> collectFreeVariables(OperationNode operation, Set<String> processes)
    throws CompilationException{
    List<String> firstIds;
    List<String> secondIds;
    //System.out.println("collectFreeVariables " + operation.myString()+ "  "+processes);
    //System.out.println(operation.myString());
    if (operation instanceof ForAllNode) {
      firstIds = OperationEvaluator.collectIdentifiers(((ForAllNode) operation).getOp());
      secondIds = ((ForAllNode) operation).getBound();
      secondIds = secondIds.stream().map(x -> {
        if (x.contains(":")) return x;
        else return x + ":*";
      }).collect(Collectors.toList());
      //System.out.println("collect from forall bound " + secondIds + " of vars " + firstIds);

      //firstIds.removeAll(secondIds);
      firstIds = filterVar(firstIds, secondIds);
      //System.out.println("collect from forall bound " + secondIds + " of free " + firstIds);
      secondIds = new ArrayList<>();
    } else if (operation instanceof ImpliesNode) {
      //System.out.println(operation.myString());
      firstIds = collectFreeVariables((OperationNode) (((ImpliesNode) operation).getFirstOperation()), processes);
      secondIds = collectFreeVariables((OperationNode) (((ImpliesNode) operation).getSecondOperation()), processes);
      //   secondIds = OperationEvaluator.collectIdentifiers(((ImpliesNode) operation).getSecondOperation());
    } else if (operation instanceof AndNode) {
      //System.out.println(operation.myString());
      firstIds = collectFreeVariables((OperationNode) (((AndNode) operation).getFirstOperation()), processes);
      secondIds = collectFreeVariables((OperationNode) (((AndNode) operation).getSecondOperation()), processes);
      //   secondIds = OperationEvaluator.collectIdentifiers(((ImpliesNode) operation).getSecondOperation());
    } else {
      firstIds = OperationEvaluator.collectIdentifiers(operation.getFirstProcess());
      secondIds = OperationEvaluator.collectIdentifiers(operation.getSecondProcess());

    }
    //System.out.println("First " + firstIds + " second " + secondIds);

    List<String> identifiers = new ArrayList<>(); // The total number of unqiue  places in the equation
    firstIds.stream().filter(id -> !identifiers.contains(id)).forEach(identifiers::add);
    secondIds.stream().filter(id -> !identifiers.contains(id)).forEach(identifiers::add);
    // at this point we have the list of identifiers
    List<String> freeVariables = identifiers.stream().filter(x -> !processes.contains(x)).collect(Collectors.toList());

    //System.out.println("END of collectFreeVariables "+freeVariables);
    return freeVariables;
  }

  private List<String> filterVar(List<String> first, List<String> second) {
    List<String> ids = new ArrayList<>();
    for (String one : first) {
      String[] p1 = one.split(":");
      String v1 = p1[0];
      boolean drop = false;
      for (String two : second) {
        String[] p2 = two.split(":");
        String v2 = p2[0];
        if (v1.equals(v2)) {
          drop = true;
          break;
        }
      }
      if (!drop) ids.add(one);
    }
    return ids;
  }

  //USED for debugging
  public static String asString(Map<String, ProcessModel> in) {
    String out = "";
    for (String key : in.keySet()) {
      out += key + "->" + in.get(key).getId() + ", ";
    }
    return out;
  }


  static class EquationReturn {
    List<OperationResult> results = new ArrayList<>();
    //List<ImpliesResult> impResults;
    Map<String, ProcessModel> toRender = new TreeMap<>();  // Compiler reads to this!

    public EquationReturn(List<OperationResult> results, Map<String, ProcessModel> toRender) {
      this.results = results;
      this.toRender = toRender;
    }
    public EquationReturn() {}

    public List<OperationResult> getResults() {
      return this.results;
    }

    public Map<String, ProcessModel> getToRender() {
      return this.toRender;
    }
  }

  /**
   * Instantiate used to iterate over domains
   */
  private class Instantiate {
    private Map<String, ProcessModel> processMap = new TreeMap<>();
    private Map<String, Integer> indexes = new TreeMap<>();  //Map variable 2 Domain 2 index

    /**
     * For  Variables with different Domains!
     * provides the next() variable instantiation and
     * end() to indicate that all instantiations given
     *
     * @param processMap    Conceptually processes and variables are disjoint but
     * @param freeVariables pragmatically they overlap
     * @param allVariables  needed to prevent domains holding variables
     * @throws CompilationException
     */
    public Instantiate(Map<String, ProcessModel> processMap, List<String> freeVariables
      , List<String> allVariables) throws CompilationException {

      //System.out.println("*****Instantiate allVars "+allVariables+" free "+freeVariables);

      if (freeVariables.size() == 0) {
        throw new CompilationException(getClass(), "Empty Variable List");
      }
      //initialise indexes for freeVariables
      for (String var : freeVariables) {
        indexes.put(var, (Integer) 0);
      }

      //System.out.println("***Instantiate "+ PetrinetInterpreter.asString(processMap));
      //System.out.println("***inst " +asString(peek()));
      this.processMap = processMap;
        //System.out.println(" constructor "+this.myString());
    }


    public Map<String, ProcessModel> peek() {
      Map<String, ProcessModel> currentInstantiation = new TreeMap<>();
      //System.out.print("PEEK ");
      //System.out.println("  domains "+domains.keySet());
      for (String key : indexes.keySet()) {
        //System.out.println("key "+key);
        String[] parts = StringUtils.split(key, ':');
        String dom = parts[1];
        //System.out.println("dom "+dom);
        currentInstantiation.put(key, domains.get(dom).get(indexes.get(key)));
        //System.out.print("_"+key+"->"+currentInstantiation.get(key).getId()+", ");
      }
      //System.out.println();
      return currentInstantiation;
    }

    /**
     * at end do nothing So check for end prior to use!
     */
    public Map<String, ProcessModel> next() {
      for (String var : indexes.keySet()) {
        String[] parts = StringUtils.split(var, ':');
        String dom = parts[1];
        if (indexes.get(var) == (domains.get(dom).size() - 1)) {
          indexes.put(var, 0);
        } else {
          indexes.put(var, indexes.get(var) + 1);
          break;
        }
      }
      //System.out.println("**next " +asString(peek()));
      return peek();
    }

    public boolean end() {
      for (String var : indexes.keySet()) {
        String[] parts = StringUtils.split(var, ':');
        String dom = parts[1];
        if (indexes.get(var) != (domains.get(dom).size() - 1)) return false;
      }
      return true;
    }

    public Integer permutationCount() {
      int cnt = 1;
      for (String var : indexes.keySet()) {
        String[] parts = StringUtils.split(var, ':');
        String dom = parts[1];
        cnt = cnt * domains.get(dom).size();
      }
      return cnt;
    }

    public String myString() {
      StringBuilder sb = new StringBuilder();

  /*    sb.append("Instantiate working on  domains ");
      domains.keySet().stream().forEach(x -> {
        sb.append("\n    " + x + "->");
        domains.get(x).stream().forEach(key -> sb.append(key.getId() + ", "));
      });
      */
      sb.append(" Instantiation   indexes  are ");
      indexes.keySet().stream().forEach(x -> {
        sb.append("  " + x + "->" + indexes.get(x));

      });


      Map<String, ProcessModel> currentInstantiation = peek();
      sb.append("\n  currentInstantiation ");
      currentInstantiation.keySet().stream().forEach(x -> {
        sb.append("  " + x + "->" + currentInstantiation.get(x).getId());

      });
      return sb.toString();
    }
  }

}

