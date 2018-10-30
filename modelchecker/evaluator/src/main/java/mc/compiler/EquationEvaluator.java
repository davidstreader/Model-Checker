package mc.compiler;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

import com.microsoft.z3.Context;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.var;
import mc.compiler.ast.ForAllNode;
import mc.compiler.ast.IdentifierNode;
import mc.compiler.ast.ImpliesNode;
import mc.compiler.ast.OperationNode;
import mc.compiler.interpreters.PetrinetInterpreter;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;
import mc.processmodels.petrinet.Petrinet;
import mc.util.LogMessage;
import org.apache.commons.lang3.StringUtils;

public class EquationEvaluator {

  private int equationId;

  static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new TreeMap<>();

  private Map<String, Integer> indexMap = new TreeMap<>(); //  automaton name -> index in models
  private List<OperationResult> results = new ArrayList<>();
  //private List<ImpliesResult> impResults = new ArrayList<>();
  int totalPermutations = 0;
  List<ProcessModel> processes;
  private Map<String, List<ProcessModel>> domains= new TreeMap<>(); //map Domain to list of processes


  private void buildDomains(Map<String, ProcessModel> processMap) {
    //build domains name 2 process list ,using processMap
    for (String k : processMap.keySet()) {
     // if (allVariables.contains(k)) continue;  //do not add variable to domain
      String[] parts = StringUtils.split(k, ':');
      String dom = parts[1];
      if (domains.containsKey(dom)) {
        domains.get(dom).add(processMap.get(k));
      } else {
        //System.out.println("WARNING DOMAIN NOT FOUND "+dom);
        domains.put(dom, new ArrayList<>(Arrays.asList(processMap.get(k))));
      }
    }
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
                                          List<OperationNode> operations,
                                          String code, Context z3Context,
                                          BlockingQueue<Object> messageQueue, Set<String> alpha)
    throws CompilationException, InterruptedException {
    reset();
    //System.out.println("EVAL EQUS \n" + processMap.keySet());
/*
   Uses processes in all domains?
 */
    processes = processMap.values().stream().collect(Collectors.toList());
//processMap will have Var:Dom -> currentProcesses set
    buildDomains(processMap);

    StringBuilder sb = new StringBuilder();
    for (String key: domains.keySet()){
      sb.append(key+"->");
      for(ProcessModel pm : domains.get(key)){
        sb.append(pm.getId()+" ");
      }
      sb.append("\n");
    }
    System.out.println(sb.toString());
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

      evaluateEquation(pMap, operation, code, z3Context, messageQueue, alpha);
    }

    return new EquationReturn(results, toRender);
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
                                OperationNode operation,
                                String code, com.microsoft.z3.Context z3Context,
                                BlockingQueue<Object> messageQueue,
                                Set<String> alpha)
    throws CompilationException, InterruptedException {
    // ONCE per equation! NOTE the state space needs to be clean
    Petrinet.netId = 0;  // hard to debug with long numbers and nothing stored
    ModelStatus status = new ModelStatus();
    //Set up the Domain  so the processMap can include variable 2 process map
  /*  if (operation instanceof ForAllNode) {  // redundent But messes up results message
      System.out.println("Top level forall is redundent!");
      return evaluateEquation(processMap,
        ((ForAllNode) operation).getOp(), code, z3Context, messageQueue, alpha);
    } */
    //collect the free variables
    //System.out.println("START - evaluateEquation " + operation.myString()+ " "+processMap.keySet());
    List<String> globlFreeVariables = collectFreeVariables(operation, processMap.keySet());
    System.out.println("globalFreeVariables " + globlFreeVariables);  //Var:Dom


    if (globlFreeVariables.size() > 3) {
      messageQueue.add(new LogMessage("\nWith this many variables you'll be waiting the rest of your life for this to complete\n.... good luck"));
    }
    Map<String, ProcessModel> globalFreeVar2Model = new TreeMap<>();

    //sets up the domains or use in looping
    Instantiate inst = new Instantiate(processMap, globlFreeVariables, globlFreeVariables);
    globalFreeVar2Model = inst.peek();
    System.out.println("new inst "+inst.myString());

    int totalPermutations = inst.permutationCount();

    //WORK Done here once per equation many ground equations evaluated
    List<String> failures = testUserdefinedModel(
      processMap,   // id + var  2 process map
      status,
      operation,
      inst,
      z3Context,
      messageQueue,
      alpha,     // Only used for broadcast semantics
      globalFreeVar2Model,
      true
    );

    // Process the results  DISPLAYED in UserInterfaceController.updateLogText
    String shortImplies;

    if (status.impliesConclusionTrue > 0 || status.impliesAssumptionFalse > 0)
      shortImplies = " (implies short circuit ass/conc " +
        status.impliesAssumptionFalse + "/" + status.impliesConclusionTrue + ") ";
    else shortImplies = " ()";
    //System.out.println("END - evaluateEquation "+ operation.myString()+" "+failures+ " "+status.myString());
    results.add(new OperationResult(failures, status.passCount == totalPermutations,
      status.passCount + "/" + totalPermutations + shortImplies, operation));


    return;
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
   * @param context
   * @param messageQueue
   * @param alpha                -- broadcast
   * @param outerFreeVariabelMap map of instianted variables
   * @return
   * @throws CompilationException
   */
  private List<String> testUserdefinedModel(Map<String, ProcessModel> processMap,
                                            ModelStatus status,  //used to RETURN results
                                            OperationNode operation,
                                            Instantiate inst,
                                            com.microsoft.z3.Context context,
                                            BlockingQueue<Object> messageQueue,
                                            Set<String> alpha,
                                            Map<String, ProcessModel> outerFreeVariabelMap,  //used in forAll{x}
                                            boolean updateFreeVariables  //allows ==> to controll when to move on
  )
    throws CompilationException, InterruptedException {
    List<String> freeVariables = outerFreeVariabelMap.keySet().stream().collect(Collectors.toList());    // free variables

    /*System.out.println("Satrting testUserDefinedModel id " + status.getId() + " " + operation.myString() +
      "\n   processMap " + processMap.keySet() +
      "\n   outer " + asString(outerFreeVariabelMap) + " pass " + status.passCount+
      "\n   inst"+inst.myString()); */

    boolean r = false;
    ArrayList<String> failedEquations = new ArrayList<>();
    // moved to inside while loop    Interpreter interpreter = new Interpreter();


    OperationEvaluator oE = new OperationEvaluator();
    int i = 0; status.passCount=0;
    while (true) { //Once per ground equation (operation)  Assumes && hence short circuit on false
      // outer free variable have been instantiated
      //     recurse  and more free vars generated by forall +
      //     after recurseion the  outerFree variable must be changed
     //System.out.println("    while loop " + i++ +" "+ asString(outerFreeVariabelMap)+"  status.passCount "+status.passCount);

      if (operation instanceof ForAllNode) {    //  FORALL
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
              if (localBound.stream().filter(x->x.startsWith(var)).collect(Collectors.toList()).size() !=0){
                //System.out.println("found "+key);
                if (processMap.containsKey(key)) {
                  processMap.remove(key);
                  //System.out.println("after remove "+asString(processMap));
                  }
                continue; //skip variables to be instantiated
              };
            }
            processMap.put(key, outerFreeVariabelMap.get(key));
            allVariables.add(key);
          }
        }
        System.out.println("forall var2 "+allVariables);
        System.out.println("forall localBound "+localBound);
        System.out.println("AFTER processMap "+asString(processMap));
     //System.out.println("    localBound " + localBound + " allVariables "+allVariables);
//Bind the local variables in the term
        for (String local: ((ForAllNode) operation).getBound()){
          System.out.println("local = "+local);
          System.out.println(operation.myString());
          if (local.contains(":")) {
             String[] parts = local.split(":");
             String var = parts[0];
            //((ForAllNode)operation).setOp(((ForAllNode) operation).getOp().instantiate(var+":*",local)) ;
            ((ForAllNode)operation).setOp(((ForAllNode) operation).getOp().instantiate(local,local)) ;
            //System.out.println("WHY?");
            //System.out.println(operation.myString());
          }
        }

        //System.out.println("domain Bound operation "+operation.myString());
        // Sets up the domains
        Instantiate forinst = new Instantiate(processMap, localBound,allVariables);
        System.out.println("new forinst "+forinst.myString());
        OperationNode localOp = ((ForAllNode) operation).getOp();
        ModelStatus localStatus = new ModelStatus();
/* build freeVar2Model for FIRST evaluation
        for (String b : localBound) {
          boundVariabelMap.put(b, processes.get(0));
        }*/
        boundVariabelMap = forinst.peek();
        System.out.println("Evaluate forall  with free var " + asString(boundVariabelMap));

        List<String> failures = testUserdefinedModel(processMap,  //Global variable will be in here
          //models,
          localStatus,
          localOp,
          forinst,
          context,
          messageQueue,
          alpha,     // Only used for broadcast semantics never writen to
          boundVariabelMap, // bound variable instantiation will be added to processMap in call
          true //call test again with expanded variable map
        );
// must pass
       //System.out.println("  foall END of testUserDefine"+"  localStatus.passCount "+localStatus.passCount);
        if (localStatus.failCount > 0) {
          status.setFailCount(localStatus.failCount);  //Fail must return
          status.setPassCount(localStatus.passCount);
          // status.setPassCount(localStatus.passCount);  // pass count not passed up term
          failures.add(asString(outerFreeVariabelMap));
          //System.out.println("Returning from forall "+ failures+" " +status.myString());
          return failures;
        } else {
          status.passCount++;

        }
      //System.out.println("  status.passCount "+status.passCount);
      } else if (operation instanceof ImpliesNode) {    //IMPLIES
        //System.out.println("Implies " + operation.myString()+" with "+ asString(outerFreeVariabelMap));
        ModelStatus status1 = new ModelStatus();
        ModelStatus status2 = new ModelStatus();
        boolean or1 = false;
        boolean or2 = false;
        if (((ImpliesNode) operation).getFirstOperation() instanceof ForAllNode) {
          //to asses short circuit evaluate 2 First
          OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
          //System.out.println("implies evaluate 2 first " + o2.myString());
          List<String> failures2 = testUserdefinedModel(processMap,
            //models,
            status2,
            o2,
            inst,
            context,
            messageQueue,
            alpha,     // Only used for broadcast semantics
            outerFreeVariabelMap, false  // will update free var map  BUT needed in second call
          );
          //System.out.println("evaluated 2 status = " + status2.myString());
          or2 = status2.failCount == 0;
          if (or2 == true) {
            //System.out.println("  @@@@@@ Short circuit Implies 2 == true");
            status.failCount = 0; //force success
            failedEquations = new ArrayList<>();
            status.impliesConclusionTrue++;
            //status.setPassCount(status2.passCount);
            r = true;
            status.passCount++;
          } else {
            OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
            //System.out.println("implies now evaluate 1 " + o1.myString());
            List<String> failures1 = testUserdefinedModel(processMap,
              //models,
              status1,
              o1,
              inst,
              context,
              messageQueue,
              alpha,     // Only used for broadcast semantics
              outerFreeVariabelMap, false
            );
            //System.out.println("Return of status1 = " + status1.myString());
            or1 = status1.failCount == 0;
            r = (!or1);  // A -> B  EQUIV  not A OR B and B==false

            //System.out.println(" "+ r + " "+ status.myString());
            // status.setPassCount(status1.passCount); //pass count not passed up tree
            if (!r) {//or1==true and or2==false
              status.setFailCount(status2.failCount);
              //System.out.println("  @@@@@@ Failing 1 Implies 2->1 " + operation.myString() + " " + asString(outerFreeVariabelMap)+" fail "+failures2);
              return failures2; //Fail must return the failures from 2 NOT 1
            } else status.passCount++;
          }
        } else {  //evaluate 1 First
          OperationNode o1 = (OperationNode) ((ImpliesNode) operation).getFirstOperation();
          //System.out.println("Implies now evaluate 1 first " + o1.myString());
          List<String> failures1 = testUserdefinedModel(processMap,
            //models,
            status1,
            o1,
            inst,
            context,
            messageQueue,
            alpha,     // Only used for broadcast semantics
            outerFreeVariabelMap, false
          );

          //System.out.println("Eval Implies 1 Returning " + status1.myString());
          or1 = status1.failCount == 0;
          if (or1 == false) {  //Short Circuit
            //System.out.println("  @@@@@@ Short circuit Implies 1 == false hence  true");
            r = true;
            status.setFailCount(0);
            failedEquations = new ArrayList<>();
            status.passCount++;
            status.impliesAssumptionFalse++;
          } else { //Not short Circuit so evaluate other part of Implies
            OperationNode o2 = (OperationNode) ((ImpliesNode) operation).getSecondOperation();
            //System.out.println("implies now evaluate 2  " + o2.myString());
            List<String> failures2 = testUserdefinedModel(processMap,
              //models,
              status2,
              o2,
              inst,
              context,
              messageQueue,
              alpha,     // Only used for broadcast semantics
              outerFreeVariabelMap, false
            );
            //System.out.println("status2 = " + status2.myString());
            or2 = status2.failCount == 0;
            r = or2;  // A -> B  EQUIV  not A OR B and A==true
            status.setFailCount(status2.failCount);
            //status.setPassCount(status2.passCount);  //pass count not passed up term
            if (status2.failCount > 0) {
              //System.out.println("  @@@@@@ Failing 2 Implies 1->2" + operation.myString() + " " + asString(outerFreeVariabelMap) + " fail " + failures2);
              return failures2; //Fail must return
            } else status.passCount++;
          }
        }

        //System.out.println("implies result " + r + " " + status.myString());

      } else {  //DO THE WORK and evaluate an OPERATION
        //System.out.println("\nStaring operation " + operation.myString() );

        // build the automata from the AST  or look up known automata
        Interpreter interpreter = new Interpreter();
        //outerFreeVariabelMap.keySet().stream().forEach(x -> processMap.put(x, outerFreeVariabelMap.get(x)));
        for (String key : outerFreeVariabelMap.keySet()) {
          //System.out.println("adding "+key+"->"+outerFreeVariabelMap.get(key).getId());
          processMap.put(key, outerFreeVariabelMap.get(key));
        }

        //System.out.println("*** evalop  "+processMap.keySet().stream().map(x->x+"->"+processMap.get(x).getId()).reduce((x,y)->x+" "+y));
        r = oE.evalOp(operation, processMap, interpreter, context, alpha);
        // r = oE.evalOp(operation, idMap, interpreter, context);
        System.out.println(asString(processMap));
        System.out.println("Processed operation " + operation.myString() +  " " + r);

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
          if (exceptionInformation.length() > 0) {
            failOutput += exceptionInformation + "\n";
          }
          failOutput += asString(outerFreeVariabelMap);
         /* for (String key : outerFreeVariabelMap.keySet()) {
            failOutput += key + "=" + outerFreeVariabelMap.get(key).getId() + ", ";
          }*/
          failedEquations.add(failOutput);
          //System.out.println("failOutput " + failOutput);
        }

//If we've failed too many operation tests;
        if (status.failCount > 0) {
          //System.out.println("  @@@@@@ Failing " + operation.myString() + " " + failedEquations);
          return failedEquations;
        }  // end by failure
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
        System.out.println("looping "+i++ +inst.myString());
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

  private List<String> collectFreeVariables(OperationNode operation, Set<String> processes) {
    List<String> firstIds;
    List<String> secondIds;
    //System.out.println("collectFreeVariables " + operation.myString()+ "  "+processes);
    //System.out.println(operation.myString());
    if (operation instanceof ForAllNode) {
      firstIds = OperationEvaluator.collectIdentifiers(((ForAllNode) operation).getOp());
      secondIds = ((ForAllNode) operation).getBound();
      secondIds = secondIds.stream().map(x->{
          if (x.contains(":")) return x;
          else return x+":*";}).collect(Collectors.toList());
      //System.out.println("collect from forall bound " + secondIds + " of vars " + firstIds);

      //firstIds.removeAll(secondIds);
      firstIds = filterVar(firstIds,secondIds);
      //System.out.println("collect from forall bound " + secondIds + " of free " + firstIds);
      secondIds = new ArrayList<>();
    } else if (operation instanceof ImpliesNode) {
      //System.out.println(operation.myString());
      firstIds = collectFreeVariables((OperationNode)(((ImpliesNode) operation).getFirstOperation()),processes);
      secondIds = collectFreeVariables((OperationNode)(((ImpliesNode) operation).getSecondOperation()),processes);
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
    for(String one:first) {
      String[] p1 = one.split(":");
      String v1 = p1[0];
      boolean drop = false;
      for(String two:second){
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


  @Getter
  @AllArgsConstructor
  static class EquationReturn {
    List<OperationResult> results;
    //List<ImpliesResult> impResults;
    Map<String, ProcessModel> toRender;  // Compiler reads to this!
  }

  /**
   *  Instantiate used to iterate over domains
   */
  private class Instantiate {
    private Map<String, ProcessModel> processMap= new TreeMap<>();
    private Map<String, Integer> indexes = new TreeMap<>();  //Map variable 2 Domain 2 index

    /**
     * For  Variables with different Domains!
     * provides the next() variable instantiation and
     * end() to indicate that all instantiations given
     *
     * @param processMap      Conceptually processes and variables are disjoint but
     * @param freeVariables   pragmatically they overlap
     * @param allVariables    needed to prevent domains holding variables
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

     // System.out.println("***Instantiate "+ PetrinetInterpreter.asString(processMap));
     // System.out.println("***inst " +asString(peek()));
      this.processMap = processMap;
      this.myString();
    }



    public Map<String, ProcessModel> peek() {
      Map<String, ProcessModel> currentInstantiation = new TreeMap<>();
      //System.out.print("PEEK ");
      for (String key : indexes.keySet()) {
        String[] parts = StringUtils.split(key, ':');
        String dom = parts[1];
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

    public String myString(){
    StringBuilder sb = new StringBuilder();

    sb.append("  domains ");
    domains.keySet().stream().forEach(x->{
      sb.append("\n    "+x+"->");
      domains.get(x).stream().forEach(key->sb.append(key.getId()+", "));
    });
      sb.append("\n  indexes ");
      indexes.keySet().stream().forEach(x->{
        sb.append("  "+x+"->"+indexes.get(x));

      });
      Map<String, ProcessModel> currentInstantiation = peek();
      sb.append("\n  currentInstantiation ");
      currentInstantiation.keySet().stream().forEach(x->{
        sb.append("  "+x+"->"+currentInstantiation.get(x).getId());

      });
    return sb.toString();
    }
  }

}

