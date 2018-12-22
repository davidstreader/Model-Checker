package mc.compiler;

import static mc.util.Utils.instantiateClass;

import com.microsoft.z3.Context;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import mc.Constant;
import mc.compiler.ast.*;
import mc.compiler.interpreters.AutomatonInterpreter;
import mc.compiler.interpreters.PetrinetInterpreter;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;

import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.petrinet.Petrinet;
import mc.Constant;
/**
 * Created by sheriddavi on 27/01/17.
 * Interpreter for Operations and Equations
 */
public class OperationEvaluator {

    private int operationId;
    private AutomatonInterpreter automatonInterpreter = new AutomatonInterpreter();
    //private PetrinetInterpreter petrinetInterpreter = new PetrinetInterpreter();
    static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new HashMap<>();
    //private List<ImpliesResult>  impRes = new ArrayList<>();
    //List<ImpliesResult> getImpRes() {return  impRes;}
    /**
     * This is the interpreter  for operations (equations) Called from Compiler
     * @param operations  one per equation in the operation section
     * @param processMap  name to processe map used to replace referances in operands
     * @param interpreter
     * @param code      Program code used to place cursor where an error occurrs
     * @param context   Z3 context
     * @param alpha
     * @return
     * @throws CompilationException
     * @throws InterruptedException
     */
    public List<OperationResult> evaluateOperations(List<OperationNode> operations,
                                                    Map<String, ProcessModel> processMap,
                                                    PetrinetInterpreter interpreter,
                                                    String code, Context context,
                                                    BlockingQueue<Object> messageQueue, Set<String> alpha)
            throws CompilationException, InterruptedException {
        reset();
        List<OperationResult> results = new ArrayList<>();
        //input  from AST
        //System.out.println("evaluateOperations processMap "+processMap.keySet());
        for (OperationNode operation : operations) {
            Result r = evaluateOperation(operation,processMap,
              interpreter,code, context,messageQueue,alpha);
            //System.out.println("evalOps "+((OperationResult)r).myString());
          //  if (r instanceof OperationResult) {
                results.add((OperationResult)r);
          //  } else if (r instanceof ImpliesResult) {
          //      impRes.add((ImpliesResult)r) ;  //  A<f B ==>> ping(A)<q ping(B)
          //  }
        }
        //System.out.println("***operation Evaluation processmap "+processMap.size());
        return results;
    }

    /**
     * Once per operation
     * @param operation
     * @param processMap
     * @param interpreter
     * @param code
     * @param context
     * @return
     * @throws CompilationException
     * @throws InterruptedException
     */
    private Result evaluateOperation(OperationNode operation,
                                    Map<String, ProcessModel> processMap,
                                     PetrinetInterpreter interpreter,
                                    String code,
                                    Context context,
                                    BlockingQueue<Object> messageQueue,
                                     Set<String> alpha) throws CompilationException, InterruptedException {

        //System.out.println("evaluateOperation op"+operation.myString() + " processMap "+processMap.keySet());
        Result or;
        //Galois Connection needs implication
        if (operation instanceof ImpliesNode) {

            OperationNode o1 =  (OperationNode) ((ImpliesNode) operation).getFirstOperation();
            OperationNode o2 =  (OperationNode) ((ImpliesNode) operation).getSecondOperation();
            OperationResult  or1 = evaluateOp(o1,processMap, interpreter,code, context,messageQueue,alpha);
            OperationResult  or2 = evaluateOp(o2,processMap, interpreter,code, context,messageQueue,alpha);

           boolean r = !or1.isRes() || or2.isRes();
            or = new OperationResult(new ArrayList<>(),r,"("+or1.isRes()+"->"+or2.isRes()+")",operation);
            //System.out.println("implies op eval or1 res "+or1.isRes()+"or2 res "+or2.isRes());
       } else {

           or = evaluateOp(operation,
             processMap,
             interpreter,
             code, context,messageQueue,alpha);
       }
       return or;
    }
/*
   wrapper to evaluation that sets up error location and storing of results
 */
    private OperationResult evaluateOp(OperationNode operation,
                                      Map<String, ProcessModel> processMap,
                                       PetrinetInterpreter interpreter,
                                      String code,
                                      Context context,
                                      BlockingQueue<Object> messageQueue,
                                       Set<String> alpha) throws CompilationException, InterruptedException {

            //input  from AST
        boolean r = false;
        //String firstId =  operation.getFirstProcess().myString(); // findIdent(operation.getFirstProcess(), code); //parsing for error feedback
        //String secondId = operation.getSecondProcess().myString(); //findIdent(operation.getSecondProcess(), code);


        List<String> firstIds = collectIdentifiers(operation.getFirstProcess());
        List<String> secondIds = collectIdentifiers(operation.getSecondProcess());
        System.out.println("evaluateOp " +operation.myString()+ " firstId " +firstIds+ " second "+secondIds);
//bound variable will have been removed.
        List<String> missing = new ArrayList<>(firstIds);
        missing.addAll(secondIds);  // all process ids
        //System.out.println("missing "+missing);
        //System.out.println("processMap.keySet() "+processMap.keySet());
        missing.removeAll(processMap.keySet());
        if (!missing.isEmpty()) {
            throw new CompilationException(OperationEvaluator.class, "Identifier " + missing.get(0) + " not found!", operation.getLocation());
        }
//******
        Stack<String> trace = new Stack<>();
        r = evalOp(operation,processMap,interpreter,context,alpha,trace);
  //System.out.println("evaluateOp "+ operation.myString()+ " trace "+trace);
        String ex;
        if (trace.isEmpty()) ex = ""; else {
           // List list = new ArrayList(trace);
           // ex = "trace "+list.size()+" = "+list.toString();
            Collections.reverse(trace);
            ex = " trace = "+trace.toString();
        }
        OperationResult result = new OperationResult(
          null,  r, "  "+ex,operation);
        //System.out.println("evaluateOp "+ result.myString());
        return result;
    }


/*
 Finally finally evaluating the dynamically loaded operation  func
 Called from EquationEvaluator as well as OperationEvaluator

 EquationEvaluator might require bound variables to be expanded!

 For computational reasons Aut->Aut functions at head of AST must be processed here
 */
    public boolean evalOp(OperationNode operation,
                          Map<String, ProcessModel> processMap,
                          PetrinetInterpreter interpreter,
                          Context context,
                          Set<String> alpha,
                          Stack<String> trace)
            throws CompilationException, InterruptedException {
        interpreter.setProcessMap(processMap);  //need Variable from EquationExpander
        Automaton.tagid =0; Petrinet.netId =0;
        List<ProcessModel> processModels = new ArrayList<>();
        Set<String> flags = operation.getFlags();
        //trace = new Stack<>();
        boolean r = false;
        System.out.print("***evalOp "+alpha+"  "+operation.myString());
        /*System.out.println("evalOp "+operation.myString());
        for(String key: processMap.keySet()){
            System.out.println(key+"->"+processMap.get(key).getId());
        } */

//  infix operations ~ <f <q ....  could be petrinet of automata
        IOperationInfixFunction funct = instantiateClass(operationsMap.get(operation.getOperation().toLowerCase()));
        //System.out.println("Funct " + funct.getFunctionName()+" "+ processMap.size());
        if (funct == null) {
            throw new CompilationException(getClass(), "The given operation is invaid: "
                    + operation.getOperation(), operation.getLocation());
        }
        /*System.out.println("*********starting Operation " + operation.getFirstProcessType() + " (" +
                operation.getOperation() + "  of type " +
                funct.getOperationType() + ")  " + operation.getSecondProcessType()); */


        //System.out.println("***evalOp "+alpha+"  "+operation.myString()+" "+operation.getOperationType());

        if (funct.getOperationType().equals(Constant.PETRINET)) {
            //String ps = processMap.values().stream().map(x->x.getId()).collect(Collectors.joining(" "));
            //System.out.println("Evaluate petrinet operation "+funct.getFunctionName());
         throw new CompilationException(this.getClass(),"Need to implement Petrinet semantics",operation.getLocation() );

        } else if (funct.getOperationType().equals(Constant.AUTOMATA)) {
            Automaton a = null;
            Automaton b = null;
            //Operations ~ <f <t <q ...
            //System.out.println("Evaluate automaton operation "+operation.getFirstProcessType()+ " "+operation.getSecondProcessType());
            //System.out.println("***evalOpm auto "+alpha+"  "+operation.myString());
            //System.out.println("evOp "+operation.getFirstProcess().myString()+ "  "+operation.getSecondProcess().myString());
            if (operation.getFirstProcess()instanceof  FunctionNode  ||
                operation.getFirstProcess()instanceof  IdentifierNode) {
                //System.out.println("\nOpeval Function1 "+operation.getFirstProcess().myString());
                //Automaton a = interpreter.getAut(processMap,interpreter,context,alpha, operation.getFirstProcess()).copy() ;
                 a = interpreter.getLocalAutomaton(context,alpha, operation.getFirstProcess());
                processModels.add(a);
                //System.out.println("\n****OpEval Fun1 "+a.myString());
            } else   if (operation.getFirstProcessType().equals(Constant.PETRINET)) {
                //System.out.println("\nOpEval TYPE NET 1 "+operation.toString());
              /*  Petrinet one = (Petrinet) interpreter.interpret(Constant.PETRINET,
                  operation.getFirstProcess(), getNextOperationId(), processMap, context, alpha); */
                Petrinet one = (Petrinet) interpreter.interpretEvalOp(operation.getFirstProcess(),
                         getNextOperationId(), processMap, context, alpha);

                 a = TokenRule.tokenRule(one);
                processModels.add(a);
                //System.out.println("OpEval TYPE NET 1 "+a.myString());
            } else if (operation.getFirstProcessType().equals(Constant.AUTOMATA)) {
                //System.out.println("\nOpEval TYPE Aut 1 "+operation.toString());
               /* Automaton one = (Automaton) interpreter.interpret(Constant.AUTOMATA,
                        operation.getFirstProcess(), getNextOperationId(), processMap, context, alpha); */
                a = (Automaton)  automatonInterpreter.interpretEvalOp(operation.getFirstProcess(), getNextOperationId(), processMap, context, alpha);


                processModels.add(a);
                //System.out.println("OpEval Aut one "+one.myString());
            }
   //System.out.println("\nOpEval ***processModels *1* "+ a.myString()); //+((Automaton) processModels.get(0)).myString());
            if (operation.getSecondProcess()instanceof  FunctionNode ||
                operation.getSecondProcess()instanceof  IdentifierNode) {
                //System.out.println("\nOpeval Function2");
               // Automaton a = interpreter.getAut(processMap,interpreter,context,alpha, operation.getSecondProcess()).copy() ;
                b = interpreter.getLocalAutomaton(context,alpha, operation.getSecondProcess());
                processModels.add(b);
               //System.out.println("****OpEval Fun2 "+a.myString());
            } else  if (operation.getSecondProcessType().equals(Constant.PETRINET)) {
                //System.out.println("\nOpEval TYPE NET 2 "+operation.toString());
               /* Petrinet two = (Petrinet) interpreter.interpret(Constant.PETRINET,
                  operation.getSecondProcess(), getNextOperationId(), processMap, context, alpha); */
                Petrinet two = (Petrinet) interpreter.interpretEvalOp(operation.getSecondProcess(),
                  getNextOperationId(), processMap, context, alpha);

                //processModels.add(TokenRule.tokenRule(two));
                b = TokenRule.tokenRule(two);
                processModels.add(b);
                //System.out.println("OpEval Net 2 "+a.myString());
            } else if (operation.getSecondProcessType().equals(Constant.AUTOMATA)) {
                //System.out.println("\nOpEval TYPE Aut 2 "+operation.toString());
                /* Automaton two = (Automaton) interpreter.interpret(Constant.AUTOMATA,
                        operation.getSecondProcess(), getNextOperationId(), processMap, context, alpha); */
                b = (Automaton)  automatonInterpreter.interpretEvalOp(operation.getSecondProcess(), getNextOperationId(), processMap, context, alpha);
                processModels.add(b);
                //System.out.println("OpEval two Aut "+two.myString());
            }
            System.out.println("\nOpEval "+operation.myString());
            System.out.println("***processModels *1* "+((Automaton) processModels.get(0)).myString());
            System.out.println("***processModels *2* "+((Automaton) processModels.get(1)).myString());
            //System.out.println("oper "+ operation.getOperation().toLowerCase());
            r = funct.evaluate(alpha, flags,context,trace,processModels);
            if (operation.isNegated()) { r = !r; }

        } else {
            //System.out.println("Bad operation type "+operation.getOperationType());
        }
        //if (r==false) {
            //System.out.println("END    evalOp " + operation.myString()+" " + EquationEvaluator.asString(processMap) + " => " + r);
       // }
        //System.out.println(" op Eval returns "+r+"  negated "+ operation.isNegated()+" trace "+trace);
        if (operation.isNegated() !=r) trace.clear();

        //System.out.println(" op Eval returns "+r+"  trace "+trace);

        //System.out.println("***evalOp with processMap  "+processMap.keySet().stream().map(x->x+"->"+processMap.get(x).getId()).reduce((x,y)->x+" "+y)+" returns "+r);

        return r;
    }

    //Automaton ain =  getAutomaton (processMap,interpreter,context,alpha, ((FunctionNode) ast).getProcesses().get(0)) ;
    //

    static List<String> collectIdentifiers(ASTNode process) {
        List<String> ids = new ArrayList<>();
        if (process==null){
            //System.out.println("process =- null");
            Throwable t = new Throwable();
            t.printStackTrace();
        }
        collectIdentifiers(process, ids);
        //System.out.println("\nOperationEvaluator Found "+ids+"\n");
        return ids;
    }


    /**
     * A recursive search for finding identifiers in an ast
     * Must return "Var:Dom"
     *
     * @param process the ast node that has identifiers in it that are to be collected
     * @param ids     the returned collection
     */
    private static void collectIdentifiers(ASTNode process, List<String> ids) {
       //System.out.println("collectIdentifiers in  "+process.myString()+"\n **");
        //System.out.println("collectId "+process.getClass().getSimpleName());
        if (process instanceof IdentifierNode) {
            String id = ((IdentifierNode) process).getVarDom();
            //((IdentifierNode) process).getDomain()
            if (!ids.contains(id)) ids.add(id);
            //System.out.println("IdentifierNode "+ id);
        } else if (process instanceof ForAllNode){
            // remove bound variables from the operations below the forall
            //System.out.println("  Bound ");
            List<String> temp = new ArrayList<>();
            collectIdentifiers(((ForAllNode) process).getOp(), temp);
            //System.out.println("  from op "+temp);
            List<String> bound = ((ForAllNode) process).getBound();
            //System.out.println("ForAllNode bound " +bound +  " temp "+temp);
            temp.removeAll(bound);
            //System.out.println("ForAllNode bound " +bound +  " temp "+temp);
            ids.addAll(temp);

        } else if (process instanceof AndNode){
            //System.out.println(" ImpliesNode");
            collectIdentifiers(((AndNode) process).getFirstOperation(), ids);
            collectIdentifiers(((AndNode) process).getSecondOperation(), ids);
        } else if (process instanceof ImpliesNode){
            //System.out.println(" ImpliesNode");
            collectIdentifiers(((ImpliesNode) process).getFirstOperation(), ids);
            collectIdentifiers(((ImpliesNode) process).getSecondOperation(), ids);
        } else if (process instanceof OperationNode){
             //no forall
                //System.out.println("  No Bound ");
                collectIdentifiers(((OperationNode) process).getFirstProcess(), ids);
                collectIdentifiers(((OperationNode) process).getSecondProcess(), ids);

            //System.out.println("operationNode "+ids);
        } else if (process instanceof ChoiceNode){
            //System.out.println(" ChoiceNode");
            collectIdentifiers(((ChoiceNode) process).getFirstProcess(), ids);
            collectIdentifiers(((ChoiceNode) process).getSecondProcess(), ids);
        }  else  if (process instanceof CompositeNode) {
            //System.out.println(" CompositeNode");
            collectIdentifiers(((CompositeNode) process).getFirstProcess(), ids);
            collectIdentifiers(((CompositeNode) process).getSecondProcess(), ids);
//        int numberNull = 0;
//        for (Expr c : subMap.values())
//            if(c == null)
//                numberNull++;
//
//        //System.out.println("NUmber null" + numberNull);
        } else  if (process instanceof FunctionNode) {
            //System.out.println(" FunctionNode");
            ((FunctionNode) process).getProcesses().forEach(p -> collectIdentifiers(p, ids));
        } else if(process instanceof ProcessRootNode) {
            //System.out.println(" ProcessRootNode");
            collectIdentifiers(((ProcessRootNode)process).getProcess(), ids);
        } else if (process instanceof IfStatementExpNode) {
            //System.out.println(" IfNode");
            collectIdentifiers(((IfStatementExpNode) process).getTrueBranch(), ids);
            if (((IfStatementExpNode) process).hasFalseBranch()) {
                collectIdentifiers(((IfStatementExpNode) process).getFalseBranch(), ids);
            }
        } else if (process instanceof SequenceNode) {
            //System.out.println(" SequenceNode");
            collectIdentifiers(((SequenceNode) process).getTo(), ids);
        }  else {
            if (process==null) {
                System.out.println("collectId operation = null");
                Throwable t = new Throwable();
                t.printStackTrace();
            } else {
                System.out.println(" DO NOT KNOW Node " + process.getName());
            }
        }
        //System.out.println("collectIdentifiers " + process.getClass().getSimpleName()+" "+ ids);
    }

    /**
     *
     *

    static String findIdent(ASTNode firstProcess, String code) {
        Location loc = firstProcess.getLocation();
        String[] lines = code.split("\\n");
        lines = Arrays.copyOfRange(lines, loc.getLineStart() - 1, loc.getLineEnd());
        if (loc.getLineEnd() != loc.getLineStart()) {
            lines[0] = lines[0].substring(loc.getColStart() - 1);
            lines[lines.length - 1] = lines[lines.length - 1].substring(0, loc.getColEnd() - 2);
        } else {
            lines[0] = lines[0].substring(loc.getColStart(), loc.getColEnd()+1);
        }

        String other = firstProcess.myString();
        //Beware   A||X <t{cong} C||X  It may be parsing needs to build the string! OR change use of ASTNode
        String out = String.join("", lines);
        System.out.println("\nFOR "+other +" findIdent returns "+out);
        return out;
    }
     */
    private String getNextOperationId() {
        return "op" + operationId++;
    }

    private void reset() {
        operationId = 0;
    }


}
