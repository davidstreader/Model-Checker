package mc.compiler;

import static mc.util.Utils.instantiateClass;

import com.microsoft.z3.Context;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import mc.compiler.ast.*;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.ProcessModel;

import mc.processmodels.automata.Automaton;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.petrinet.Petrinet;
import mc.util.Location;

/**
 * Created by sheriddavi on 27/01/17.
 * Interpreter for Operations and Equations
 */
public class OperationEvaluator {

    private int operationId;

    static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new HashMap<>();
    private final String automata = "automata";
    private List<ImpliesResult>  impRes = new ArrayList<>();
    List<ImpliesResult> getImpRes() {return  impRes;}
    Set<String> alpha;
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
                                                    Interpreter interpreter,
                                                    String code, Context context,
                                                    BlockingQueue<Object> messageQueue, Set<String> alpha)
            throws CompilationException, InterruptedException {
        reset();
        List<OperationResult> results = new ArrayList<>();
        //input  from AST
        for (OperationNode operation : operations) {
            Result r = evaluateOperation(operation,processMap,
              interpreter,code, context,messageQueue,alpha);
            if (r instanceof OperationResult) {
                results.add((OperationResult)r);
            } else if (r instanceof ImpliesResult) {
                impRes.add((ImpliesResult)r) ;  //  A<f B ==>> ping(A)<q ping(B)
            }
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
                                    Interpreter interpreter,
                                    String code,
                                    Context context,
                                    BlockingQueue<Object> messageQueue,
                                     Set<String> alpha) throws CompilationException, InterruptedException {

        Result or;
        //Galois Connection needs implication
        if (operation instanceof ImpliesNode) {

            OperationNode o1 =  (OperationNode) ((ImpliesNode) operation).getFirstOperation();
            OperationNode o2 =  (OperationNode) ((ImpliesNode) operation).getSecondOperation();
            OperationResult  or1 = evaluateOp(o1,processMap, interpreter,code, context,messageQueue,alpha);
            OperationResult  or2 = evaluateOp(o2,processMap, interpreter,code, context,messageQueue,alpha);
            or = new ImpliesResult(or1,or2);
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
                                      Interpreter interpreter,
                                      String code,
                                      Context context,
                                      BlockingQueue<Object> messageQueue,
                                       Set<String> alpha) throws CompilationException, InterruptedException {

            //input  from AST
        boolean r = false;
        String firstId =  operation.getFirstProcess().myString(); // findIdent(operation.getFirstProcess(), code); //parsing for error feedback
        String secondId = operation.getSecondProcess().myString(); //findIdent(operation.getSecondProcess(), code);


        List<String> firstIds = collectIdentifiers(operation.getFirstProcess());
        List<String> secondIds = collectIdentifiers(operation.getSecondProcess());
        //System.out.println("evaluateOp " +operation.getOperation()+ " firstId " +firstIds+ "second "+secondIds);
//bound variable will have been removed.
        List<String> missing = new ArrayList<>(firstIds);
        missing.addAll(secondIds);  // all process ids
        missing.removeAll(processMap.keySet());
        if (!missing.isEmpty()) {
            throw new CompilationException(OperationEvaluator.class, "Identifier " + missing.get(0) + " not found!", operation.getLocation());
        }
//******
        r = evalOp(operation,processMap,interpreter,context,alpha);
  //System.out.println("evaluateOp "+ firstId+" "+operation.getOperation()+" "+secondId+" "+r);
        OperationResult result = new OperationResult(operation.getFirstProcess(),
                operation.getSecondProcess(), firstId, secondId,
                operation.getOperation(), null, operation.isNegated(), r, "");

        return result;
    }

/*
 Finally finally evaluating the dynamically loaded operation  func
 Called from EquationEvaluator as well as OperationEvaluator

 EquationEvaluator might require bound variables to be expanded!
 */
    public boolean evalOp(OperationNode operation,
                          Map<String, ProcessModel> processMap,
                          Interpreter interpreter,
                          Context context,
                          Set<String> alpha)
            throws CompilationException, InterruptedException {



        List<ProcessModel> processModels = new ArrayList<>();
        Set<String> flags = operation.getFlags();
        boolean r = false;
        /*System.out.println("evalOp "+operation.myString());
        for(String key: processMap.keySet()){
            System.out.println(key+"->"+processMap.get(key).getId());
        }*/


        IOperationInfixFunction funct = instantiateClass(operationsMap.get(operation.getOperation().toLowerCase()));
        //System.out.println("Funct " + funct.getFunctionName()+" "+ processMap.size());
        if (funct == null) {
            throw new CompilationException(getClass(), "The given operation is invaid: "
                    + operation.getOperation(), operation.getLocation());
        }
        /*System.out.println("*********starting Operation " + operation.getFirstProcessType() + " (" +
                operation.getOperation() + "  of type " +
                funct.getOperationType() + ")  " + operation.getSecondProcessType()); */



        if (funct.getOperationType().equals("petrinet")) {
            String ps = processMap.values().stream().map(x->x.getId()).collect(Collectors.joining(" "));
            //System.out.println("Evaluate petrinet operation pMap "+ps);
            //System.out.println();
// Convert operands to PetriNets were needed and store in processModels
            if (operation.getFirstProcessType().equals("petrinet")) {
                Petrinet one = (Petrinet) interpreter.interpret("petrinet",
                        operation.getFirstProcess(), getNextOperationId(), processMap, context);
                processModels.add(one);
            } else if (operation.getFirstProcessType().equals(automata)) {
                Automaton one = (Automaton) interpreter.interpret(automata,
                        operation.getFirstProcess(), getNextOperationId(), processMap, context);
                processModels.add(OwnersRule.ownersRule( one));
            }
            if (operation.getSecondProcessType().equals("petrinet")) {
                Petrinet two = (Petrinet) interpreter.interpret("petrinet",
                        operation.getSecondProcess(), getNextOperationId(), processMap, context);
                processModels.add(two);
            } else if (operation.getSecondProcessType().equals(automata)) {
                Automaton two = (Automaton) interpreter.interpret(automata,
                        operation.getFirstProcess(), getNextOperationId(), processMap, context);
                processModels.add(OwnersRule.ownersRule( two));
            }
            r = funct.evaluate(new TreeSet<>(), flags,context,processModels);  //actually evaluating the operation
            if (operation.isNegated()) { r = !r; }

        } else if (funct.getOperationType().equals(automata)) {
            //System.out.println("Evaluate automaton operation "+operation.getFirstProcessType()+ " "+operation.getSecondProcessType());

// Convert to PetriNets were needed
            if (operation.getFirstProcessType().equals("petrinet")) {
                Petrinet one = (Petrinet) interpreter.interpret("petrinet",
                        operation.getFirstProcess(), getNextOperationId(), processMap, context);
                processModels.add(TokenRule.tokenRule(one));
            } else if (operation.getFirstProcessType().equals(automata)) {
                Automaton one = (Automaton) interpreter.interpret(automata,
                        operation.getFirstProcess(), getNextOperationId(), processMap, context);
                processModels.add(one);
            }
            //System.out.println("processModels *1* "+((Automaton) processModels.get(0)).myString());
            if (operation.getSecondProcessType().equals("petrinet")) {
                Petrinet two = (Petrinet) interpreter.interpret("petrinet",
                        operation.getSecondProcess(), getNextOperationId(), processMap, context);

                processModels.add(TokenRule.tokenRule(two));
            } else if (operation.getSecondProcessType().equals(automata)) {
                Automaton two = (Automaton) interpreter.interpret(automata,
                        operation.getSecondProcess(), getNextOperationId(), processMap, context);
                processModels.add(two);
            }
            //System.out.println("processModels *2*"+((Automaton) processModels.get(1)).myString());
            //System.out.println("oper "+ operation.getOperation().toLowerCase());
            r = funct.evaluate(alpha, flags,context,processModels);
            if (operation.isNegated()) { r = !r; }

        } else {
            System.out.println("Bad operation type "+operation.getOperationType());
        }
        //if (r==false) {
            System.out.println("                evalOp " + operation.myString()+" " + EquationEvaluator.asString(processMap) + " => " + r);
       // }
        return r;
    }

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
     *
     * @param process the ast node that has identifiers in it that are to be collected
     * @param ids     the returned collection
     */
    private static void collectIdentifiers(ASTNode process, List<String> ids) {
       //if (process!= null) System.out.println("collectIds in  "+process.wholeString()+"\n **");
        if (process instanceof IdentifierNode) {
            String id = ((IdentifierNode) process).getIdentifier();
            if (!ids.contains(id)) ids.add(id);
            //System.out.println("IdentifierNode "+ id);
        } else if (process instanceof ForAllNode){
            // remove bound variables from the operations below the forall
            //System.out.println("  Bound ");
            List<String> temp = new ArrayList<>();
            collectIdentifiers(((ForAllNode) process).getOp(), temp);
            System.out.println("  from op "+temp);
            List<String> bound = ((ForAllNode) process).getBound();
            System.out.println("ForAllNode bound " +bound +  " temp "+temp);
            temp.removeAll(bound);
            System.out.println("ForAllNode bound " +bound +  " temp "+temp);
            ids.addAll(temp);

        } else if (process instanceof ImpliesNode){
            //System.out.println(" ImpliesNode");
            collectIdentifiers(((ImpliesNode) process).getFirstProcess(), ids);
            collectIdentifiers(((ImpliesNode) process).getSecondProcess(), ids);
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
