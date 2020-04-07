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
import java.util.concurrent.*;
import java.util.stream.Collectors;

//import mc.compiler.interpreters.PetrinetInterpreter;

public class EquationEvaluator {

    private int equationId;

    static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new TreeMap<>();

    private Map<String, Integer> indexMap = new TreeMap<>(); //  automaton name -> index in models
    private List<OperationResult> results = new ArrayList<>(); // one per equation
    //private List<ImpliesResult> impResults = new ArrayList<>();
    int totalPermutations = 0;
    int totalPassed = 0;
    int totalTests = 0;
    List<ProcessModel> processes;
    PetrinetInterpreter petrinetInterpreter;
    Context z3Context;
    BlockingQueue<Object> messageQueue; // link to GUI
    BlockingQueue<Object> poolQueue;  // link from pool threads
    // domains only used in Instantiate but built from processMap

    /* each time compile is pushed a new EquationEvaluator object is built and
      a new thread pool started. Each equation evaluates many ground equations and
      their evaluation is spread over different threads in the pool.
         When a thread finds an error or the user presses stop work
      an  stops the Executor
     */
    private final ExecutorService workPool = Executors.newWorkStealingPool();

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }



/*  public EquationEvaluator() {

    impResults = new ArrayList<>();
  } */

    /**
     * @param processMap a list of automaton defined
     * @param operations a list of the operations - one for each equation
     *                   //* @param code         used for error reporting
     * @param z3Cont
     * @param messQ      a blocking  queue to the GUI thread
     * @param alpha
     * @return The EquationReturn - list of results
     * @throws CompilationException
     */
    public EquationReturn evaluateEquations(Map<String, ProcessModel> processMap,
                                            PetrinetInterpreter petInt,
                                            List<OperationNode> operations,
                                            Context z3Cont,
                                            BlockingQueue<Object> messQ, Set<String> alpha)
        throws CompilationException, InterruptedException {
        reset();
        petrinetInterpreter = petInt;
        z3Context = z3Cont;
        messageQueue = messQ;
        //System.out.println("EVAL EQUS \n" + processMap.keySet());


        //System.currentTimeMillis();
        Date start = new Date();
        messageQueue.add(new LogMessage("    ##Equations  Starting##\n  " + start.toString(), true,
            false, null, -1, Thread.currentThread()));
        processes = processMap.values().stream().collect(Collectors.toList());
        //System.out.println("evaluateEquations processMap "+processMap.keySet());
//processMap will have Var:Dom -> currentProcesses set
        Instantiate.buildFreshDomains(processMap);


        //System.out.println(sb.toString());*/
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

            evaluateEquation(pMap, operation, alpha);
        }
        Date stop = new Date();

        String log = "    ##Equations## " + totalPassed + "/" + totalTests + "  " + getDifferenceDays(start, stop) + "\n" + stop;
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

        return diffHours + ":" + diffMinutes + ":" + diffSeconds;
    }

    /* process terms may contain both
       1. referances that are looked up in the processMap
       2. variables that are instantiated by adding them to the processMap and
          then treating them like referances
     forall{X} (P(X,Y,Z))  ==> Q(Y,Z)       forall{X} (Q(Y,Z)  ==> P(X,Y,Z))
                  ==>                                     forall{X}
        forall{X}      Q(Y,Z)                                ==>
        P(X,Y,Z)                                     Q(Y,Z)       P(X,Y,Z)

       At the top level variables Y and Z need to be instantiated
       the ground variable X is only instantiated when the forall operation is evaluated.

      Evaluates a SINGLE EQUATION. - Many operations - Many ground equations
       */
    private void evaluateEquation(Map<String, ProcessModel> processMap,
                                  OperationNode operation, //
                                  Set<String> alpha)
        throws CompilationException, InterruptedException {

        /*    11/19 TODO
         * One equation may have many thousand ground instances
         *  a thread pool was set up when this object was cobstructed
         *  to process the the ground instances on all available  cores
         * force early termination using the java Interupt
         * Note because of the forall the workload may be very variable
         * hence use workStealingPool
         * */


        // ONCE per equation! NOTE the state space needs to be clean
        Petrinet.netId = 0;  // hard to debug with long numbers and nothing stored
        ModelStatus status = new ModelStatus();
        //Stack<String> trace = new Stack<>();

        //collect the free variables
        System.out.println("\nSTART - evaluateEquation " + operation.myString() + "\nProcess map " + processMap.keySet());
        List<String> globlFreeVariables = collectFreeVariables(operation, processMap.keySet());
        //System.out.println("globalFreeVariables " + globlFreeVariables);  //Var:Dom
        if (!Instantiate.validateDomains(globlFreeVariables))
            throw new CompilationException(this.getClass(), "Variable with Domain not defined ", operation.getLocation());

        if (globlFreeVariables.size() > 3) {
            messageQueue.add(new LogMessage("\nWith this many variables you'll be waiting the rest of your life for this to complete\n.... good luck"));
        }
   /*       11/19 TODO
     if more than one variable then split of one varaible and for each instatiation
     add instantiated variable  to processMap The remaining free variables
     define a SLICE of the equation use a pool thread  to evaluate the slice.
        When one operation fails
          stop the thread evaluating that Slice and softly stop the Executor.
        When the Slice finishes then add results to the final result
     When all Slices finish return final result
    */

        //if (globlFreeVariables.size() ==1) {

        Instantiate inst = new Instantiate(globlFreeVariables);
        //System.out.println("new inst "+inst.myString());

        int totalPermutations = inst.permutationCount();

        //WORK Done here once per equation many ground equations evaluated
        List<String> failures;
        try {
            status = startTest(  // recursive call down equation term
                processMap,   // map (id + var)  2 process
                operation,   // equation term
                inst,
                alpha,         // Only used for broadcast semantics
                true
            );
        } catch (CompilationException e) {
            String emes = e.getMessage();
            throw new CompilationException(e.getClazz(), emes + " globalOp " + operation.myString() +
                "\n free " + freeVar2String(inst.peek()));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // Process the results  DISPLAYED in UserInterfaceController.updateLogText
        String shortImplies;
        System.out.println("evaluateEqu "+operation.myString()+" "+status.myString());
        if (status.impliesConclusionTrue > 0 || status.impliesAssumptionFalse > 0)
            shortImplies = " (implies short circuit ass/conc " +
                status.impliesAssumptionFalse + "/" + status.impliesConclusionTrue + ") ";
        else shortImplies = " ()";
        //System.out.println("END - evaluateEquation "+ operation.myString()+" "+failures+ " "+status.myString());
        results.add(new OperationResult(status.getFailures(), status.passCount == totalPermutations,
            status.passCount + "/" + totalPermutations + shortImplies, operation));
        String f;
        if (status.getFailures().size() == 0) f = "";
        else f = "  " + status.getFailures();
        String logged = " " + operation.myString() + "\n        Simulations Passed " + " " + status.passCount + "/" + totalPermutations + shortImplies + f;
        messageQueue.add(new LogMessage(logged, true,
            false, null, -1, Thread.currentThread()));
        totalPassed = totalPassed + status.passCount;
        totalTests = totalTests + totalPermutations;
/*       return;
   } else {
      String sliceVar =  globlFreeVariables.get(0);
      String dom = sliceVar.split(":")[1];
      globlFreeVariables.remove(0);
       List<ProcessModel> processesToSlice  = Instantiate.getDomain(dom);

   } */
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

    /*
     called once per equation loops around
           instantiating global variables
           calling processTest for each instantiation.

     */
    private ModelStatus startTest(Map<String, ProcessModel> processMap,
                                  OperationNode operation,  // the equation
                                  Instantiate inst,   //The variables used in the instantiation
                                  Set<String> alpha,
                                  boolean updateVariables                         //Only true initially and for forAll.
    )
        throws CompilationException, InterruptedException, ExecutionException {

        int i = 0;
        System.out.println("Starting tstarttest " + operation.myString() +
            "\n   processMap " + processMap.keySet() +
            "\n   outer " + asString(inst.peek()) +
            "\n   inst" + inst.myString());
        ModelStatus globalStatus = new ModelStatus();
        while (true) {  // once per instantiation
            System.out.println("\n  Looping "+operation.myString()+" "  +asString(inst.peek()));
            String pro = asString(inst.peek());
            ModelStatus status = processTest(processMap, operation, inst, alpha, updateVariables);
            System.out.println("  @@@@@@ process          " + operation.myString() + " " + pro+ " status "+ status.myString());
            if (status.failCount == 0) {
                if (status.impliesAssumptionFalse >0) globalStatus.impliesAssumptionFalse++;
                if (status.impliesConclusionTrue >0) globalStatus.impliesConclusionTrue++;
                globalStatus.passCount++;
                System.out.println("  @@@@@@ process passed " + operation.myString() + " " + pro+ " globalStatus "+ globalStatus.myString());
                if (inst.end()) {
                    globalStatus.setFailures(new ArrayList<String>());
                     System.out.println("  LoopEnd "+operation.myString()+" "+asString(inst.peek())+" "+globalStatus.myString());
                    return globalStatus;
                }
                inst.next();  //this will iterate untill end
                //System.out.println("looping "+i++ +inst.myString());
            } else { // FAILED
                System.out.println("  @@@@@@ process failed " + operation.myString() + " " + pro+" "+globalStatus.myString());
                return globalStatus;
            }
            System.out.println(" Operation " + i++ + "  " + operation.myString() +
                " " + pro +
                " passed " + globalStatus.passCount +
                " next " + asString(inst.peek()));
        }

    }

    /**
     * Called once per ground equation
     * Then Recurse down operation tree passing in Instatniation
     * returned results in ModelStatus
     * <p>
     * To evaluate in speerate thread need results returned in one place
     * and design method to collect results from different threads
     *
     * @param processMap This is the list of defined processes
     *                   //* @param status
     * @param operation  ONE Equation - operation = two processes and name of operation
     *                   OR ==>  and two Equation operations
     *                   // * @param context              trace
     *                   //* @param messageQueue
     * @param alpha      -- broadcast
     *                   //* @param outerFreeVariabelMap map of instianted variables
     * @return
     * @throws CompilationException
     */
    private ModelStatus processTest(Map<String, ProcessModel> processMap,
                                    OperationNode operation,  // the equation
                                    Instantiate inst,   //The variables used in the instantiation
                                    Set<String> alpha,
                                    boolean updateVariables                         //Only true initially and for forAll.
    )
        throws CompilationException, InterruptedException, ExecutionException {

        ModelStatus status = new ModelStatus();
        Map<String, ProcessModel> outerFreeVariabelMap = inst.peek();
        Map<String, ProcessModel> localFreeVariableMap = new TreeMap<>();

        boolean r = false;
        ArrayList<String> failedEquations = new ArrayList<>();
        // moved to inside while loop    Interpreter interpreter = new Interpreter();


        OperationEvaluator oE = new OperationEvaluator();
        int i = 0;
        //status.passCount = 0;
        while (true) { //Once per ground equation (operation)  Assumes && hence short circuit on false
            // outer free variable have been instantiated
            //     recurse  and more free vars generated by forall +
            //     after recurseion the  outerFree variable must be changed
            System.out.println("    while loop " + i++ + " " + asString(outerFreeVariabelMap) + "  status.passCount " + status.passCount);

            if (operation instanceof ForAllNode) {    //  FORALL
                System.out.println("-- ForAll " + operation.myString() + " with " + asString(outerFreeVariabelMap));
                // add outer free Vars to Process model
                // build inner free Vars

                List<String>localBound = ((ForAllNode) operation).getBound();
                //System.out.println("forall "+localBound);
                localBound = localBound.stream().map(x -> {
                    if (x.contains(":")) return x;
                    else return x + ":*";
                }).collect(Collectors.toList());
                List<String> allVariables = localBound.stream().collect(Collectors.toList());

                //System.out.println("pMap "+asString(processMap));
                //Map<String, ProcessModel> boundVariabelMap; //Only used to expand the variable map
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
                        processMap.put(key, outerFreeVariabelMap.get(key)); // add outervar to processMap
                        allVariables.add(key);
                    }
                }
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
                Instantiate forinst = new Instantiate(localBound);
                localFreeVariableMap = forinst.peek();
                //System.out.println("new forinst "+forinst.myString());
                OperationNode localOp = ((ForAllNode) operation).getOp();
                ModelStatus localStatus;

                //boundVariabelMap = forinst.peek();
                //System.out.println("Evaluate forall  with free var " + asString(boundVariabelMap));
                List<String> failures;
                try {
                    localStatus = startTest(processMap,  //Global variable will be in here
                        localOp, // recursivly process sub term
                        forinst,
                        alpha,      // Only used for broadcast semantics never writen to
                        true //call test again with expanded variable map
                    );
                } catch (CompilationException e) {
                    String log = "(" + totalPassed + "/" + totalTests + ")    ##Error## " + localOp.myString() + "\n   " + asString(outerFreeVariabelMap);
                    messageQueue.add(new LogMessage(log, true,
                        false, null, -1, Thread.currentThread()));

                    //System.out.println("forall with "+ asString(outerFreeVariabelMap));
                    String emes = e.getMessage();
                    throw new CompilationException(e.getClazz(), emes + //" localOp " + localOp.myString() +
                        " bound " + freeVar2String(forinst.peek()) + "\n");
                }
                localFreeVariableMap = new TreeMap<>();
// must pass
                //System.out.println("  forall END of testUserDefine"+"  localStatus "+localStatus.myString());
                if (localStatus.failCount > 0) {
                    // status.setPassCount(localStatus.passCount);  // pass count not passed up term
                    status.failCount++;
                    status.addFailure("Forall fail");
                    status.addFailure(" free " + asString(outerFreeVariabelMap));
                    status.setTrace(localStatus.getTrace());
                    //System.out.println("Returning from forall "+ failures+" " +status.myString());
                    localBound.clear();
                    return status;
                } else {
                    status.setTrace(new Stack<>());
                    status.passCount++;
                    if (localStatus.impliesAssumptionFalse>0) status.impliesAssumptionFalse++;
                    if (localStatus.impliesConclusionTrue>0) status.impliesConclusionTrue++;
                    System.out.println("  @=passed=@   Forall" + operation.myString() + " " + asString(inst.peek()) + " status " + status.myString());
                    localBound.clear();
                    return status;
                }
                /*  *******************************  IMPLIES IMPLIES */
                //System.out.println("  status.passCount "+status.passCount);
            } else if (operation instanceof ImpliesNode) {    //IMPLIES
                System.out.println("-- Implies " + operation.myString() + " with " + asString(outerFreeVariabelMap));
                ModelStatus status1;
                ModelStatus status2;
                //System.out.println("First" + ((ImpliesNode) operation).getFirstOperation().myString());
                if (((ImpliesNode) operation).getFirstOperation() instanceof ForAllNode) {
                    //to asses short circuit evaluate 2 First
                    //trace = new Stack<>(); // past traces not needed
                    OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
                    //System.out.println("implies evaluate 2 first " + o2.myString());
                    status2 = processTest(processMap,
                        o2,
                        inst,
                        alpha,     // Only used for broadcast semantics
                        false  // will update free var map  BUT needed in second call
                    );
                    //System.out.println("evaluated 2 status = " + status2.myString());
                    if (status2.failCount == 0) { // ==> TRUE   ~~> true
                        //System.out.println("  @@@@@@ Short circuit XXX ==> true is true");
                        status.failCount = 0; //force success
                        failedEquations = new ArrayList<>();
                        status.impliesConclusionTrue++;
                        //status.setPassCount(status2.passCount);
                        status.setPassCount(1);
                    } else {  //  XX ==> false  ~~> Not(XX)
                        OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
                        System.out.println("implies now evaluate 1 " + o1.myString());
                        status1 = processTest(processMap,
                            o1,
                            inst,
                            alpha,     // Only used for broadcast semantics
                            false
                        );
                        //System.out.println("evaluated  status1 = " + status1.myString());

                        //System.out.println(" "+ r + " "+ status.myString());
                        // status.setPassCount(status1.passCount); //pass count not passed up tree
                        if (status1.failCount == 0) {// true ==> false   is false
                            status.setFailCount(status2.failCount);
                            List<String> failures = new ArrayList<>();
                            failures.add("true ==> false");
                            failures.addAll(status2.getFailures());
                            status.setFailures(failures);
                            System.out.println("  ======= Failing 1 Implies 2->1 " + operation.myString() + " " + asString(outerFreeVariabelMap) + " fail " + status.getFailures());
                            return status; //Fail must return the failures from 2 NOT 1
                        } else {  // true ==>  true is true

                            status.setPassCount(1);
                        }
                    }
                    System.out.println("  @=passed=@  1 Implies 1->2" + operation.myString() + " " + asString(inst.peek()) + " " + status.myString());
                } else {  //forall NOT on first so evaluate 1 First
                    OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
                    System.out.println("Implies evaluate 1 first " + o1.myString());
                    status1 = processTest(processMap,
                        o1,
                        inst,
                        alpha,     // Only used for broadcast semantics
                        false
                    );

                    //System.out.println("Eval Implies 1 "+((ImpliesNode) operation).myString() +" Returning " + status1.myString());
                    //System.out.println("XXXXEval Implies 1 "+o1.myString() +" Returning " + status1.myString());
                    if (status1.failCount != 0) {  // false ==> X  ~~> true
                        //System.out.println("  ==== Short circuit Implies 1 == false hence  true");

                        status.setFailCount(0);
                        failedEquations.clear();
                        status.setPassCount(1);
                        status.impliesAssumptionFalse++;
                    } else { //  true ==>X   ~~> X
                        OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
                        //System.out.println(" ==== implies now evaluate 2  " + o2.myString());
                        //trace = new Stack<>(); // past traces not needed
                        status2 = processTest(processMap,
                            o2,
                            inst,
                            alpha,     // Only used for broadcast semantics
                            false
                        );
                        //System.out.println("XXXXEval Implies 2 "+o2.myString() +" Returning " + status1.myString());
                        status.setFailCount(status2.failCount);
                        //status.setPassCount(status2.passCount);  //pass count not passed up term
                        if (status2.failCount > 0) {  //true==> false ~~> false
                            status.failCount = status2.failCount;
                            List<String> failures = new ArrayList<>();
                            failures.add("true ==> false");
                            failures.addAll(status2.getFailures());
                            status.setFailures(failures);
                            System.out.println("  @====@ Failing 2 Implies 1->2" + operation.myString() + " " + asString(inst.peek()) + " fail " + status2.getFailures());

                            return status; //Fail must return
                        } else {   //true ==> true ~~> true
                            status.setPassCount(1);
                            status.setFailCount(0);
                        }
                    }
                    System.out.println("  @=passed=@  2 Implies 1->2" + operation.myString() + " " + asString(inst.peek()) + " " + status.myString());
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
                status1 = processTest(processMap,
                    o1,
                    inst,
                    alpha,     // Only used for broadcast semantics
                    false
                );

                //System.out.println("XXXXEval And 1 "+o1.myString() +" Returning " + status1.myString());
                if (status1.failCount > 0) {  // Short Circuit  false AND XX is false
                    //System.out.println("  @@@@@@ Short circuit And 1 == false hence  false");
                    status.failCount = status1.failCount;
                    List<String> failure = new ArrayList<>();
                    failure.add("&& failure");
                    failure.addAll(status1.getFailures());
                    status.setFailures(failure);
                    return status;
                } else { //Not short Circuit so evaluate other part of And
                    OperationNode o2 = (OperationNode) ((AndNode) operation).getSecondOperation();
                    //System.out.println("and now evaluate 2  " + o2.myString());
                    status2 = processTest(processMap,
                        o2,
                        inst,
                        alpha,     // Only used for broadcast semantics
                        false
                    );
                    //System.out.println("XXXXEval And 2 "+o2.myString() +" Returning " + status2.myString());
                    status.setFailCount(status2.failCount);
                    //status.setPassCount(status2.passCount);  //pass count not passed up term
                    if (status2.failCount > 0) {   // true && false is false
                        //System.out.println("  @@@@@@ Failing 2 And 1->2" + o2.myString() + " " + asString(outerFreeVariabelMap) + " fail " + failures2);
                        status.failCount = status2.failCount;
                        List<String> failure = new ArrayList<>();
                        failure.add("&& failure");
                        failure.addAll(status2.getFailures());
                        status.setFailures(failure);
                        return status;//Fail must return

                    } else {
                        status.setPassCount(1);
                        status.setFailCount(0);
                    }
                }


                //System.out.println("And  passed " + operation.myString() + "  " + status.myString());

                /*  WORK   ************************   WORK   */
            } else {  //DO THE WORK and evaluate an OPERATION
                for (String key : outerFreeVariabelMap.keySet()) {
                    //System.out.println("adding "+key+"->"+outerFreeVariabelMap.get(key).getId());
                    processMap.put(key, outerFreeVariabelMap.get(key));
                }
                System.out.println("*** eval  " + operation.myString() + "  " + asString(outerFreeVariabelMap) + " "+ asString(localFreeVariableMap));
                try {
                    r = oE.evalOp(operation, processMap, petrinetInterpreter, z3Context, alpha, status.getTrace());
                } catch (CompilationException | ExecutionException e) {
                    System.out.println("\n-- EVAL " + operation.myString() + " with " + asString(outerFreeVariabelMap));
                    System.out.println("  Inst " + inst.myString());
                    System.out.println("ProcessMap " + processMap.keySet() +
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
                    status.setPassCount(1);
                    status.setFailCount(0);
                    System.out.println("passed");
                } else {
                    status.failCount++;
                    status.setPassCount(0);
                    String exceptionInformation = operation.myString();
                    exceptionInformation += "\n trace = " + status.getTrace().toString() + ",\n vars ";
                    exceptionInformation += asString(outerFreeVariabelMap);

                    failedEquations.add(exceptionInformation);
                    System.out.println("  XXXXX  failOutput " + exceptionInformation);
                    status.addFailure(exceptionInformation);
                    return status;
                }
                status.timeStamp = System.currentTimeMillis();
                //if all elements in the map are the same final element in models, then end the test.
                //System.out.println("Passed " + operation.myString() + " " + asString(outerFreeVariabelMap));
            }

            return status;
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
        throws CompilationException {
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

        public EquationReturn() {
        }

        public List<OperationResult> getResults() {
            return this.results;
        }

        public Map<String, ProcessModel> getToRender() {
            return this.toRender;
        }
    }


}

