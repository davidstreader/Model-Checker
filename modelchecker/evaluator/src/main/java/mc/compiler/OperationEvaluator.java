package mc.compiler;

import static mc.util.Utils.instantiateClass;

import com.microsoft.z3.Context;

import java.util.*;
import java.util.stream.Collectors;

import mc.compiler.ast.ASTNode;
import mc.compiler.ast.ChoiceNode;
import mc.compiler.ast.CompositeNode;
import mc.compiler.ast.FunctionNode;
import mc.compiler.ast.IdentifierNode;
import mc.compiler.ast.IfStatementExpNode;
import mc.compiler.ast.OperationNode;
import mc.compiler.ast.ProcessRootNode;
import mc.compiler.ast.SequenceNode;
import mc.exceptions.CompilationException;
import mc.plugins.IOperationInfixFunction;
import mc.processmodels.MultiProcessModel;
import mc.processmodels.ProcessModel;

import mc.processmodels.automata.Automaton;
import mc.processmodels.automata.AutomatonNode;
import mc.processmodels.conversion.TokenRule;
import mc.processmodels.conversion.OwnersRule;
import mc.processmodels.petrinet.Petrinet;
import mc.util.Location;

/**
 * Created by sheriddavi on 27/01/17.
 */
public class OperationEvaluator {

    private int operationId;

    static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new HashMap<>();
    private final String automata = "automata";

    /**
     * This is the interpreter  for operations (equations) Called from Compiler
     * @param operations  one per equation in the operation section
     * @param processMap  name to processe map used to replace referances in operands
     * @param interpreter
     * @param code      I think this is only used to place cursor where error occurrs
     * @param context   Z3 context
     * @return
     * @throws CompilationException
     * @throws InterruptedException
     */
    public List<OperationResult> evaluateOperations(List<OperationNode> operations,
                                                    Map<String, ProcessModel> processMap,
                                                    Interpreter interpreter,
                                                    String code, Context context)
            throws CompilationException, InterruptedException {
        reset();
        List<OperationResult> results = new ArrayList<>();
        //input  from AST
        for (OperationNode operation : operations) {

            results.add(evaluateOperation(operation,processMap,
                    interpreter,
                    code, context));
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
    public OperationResult evaluateOperation(OperationNode operation,
                                             Map<String, ProcessModel> processMap,
                                             Interpreter interpreter,
                                             String code, Context context) throws CompilationException, InterruptedException {

        //input  from AST
        boolean r = false;
        String firstId = findIdent(operation.getFirstProcess(), code);
        String secondId = findIdent(operation.getSecondProcess(), code);



        List<String> firstIds = collectIdentifiers(operation.getFirstProcess());
        List<String> secondIds = collectIdentifiers(operation.getSecondProcess());
        //System.out.println("***second "+operation.getSecondProcess().toString());


        List<String> missing = new ArrayList<>(firstIds);
        missing.addAll(secondIds);  // all process ids
        missing.removeAll(processMap.keySet());
        if (!missing.isEmpty()) {
            throw new CompilationException(OperationEvaluator.class, "Identifier " + missing.get(0) + " not found!", operation.getLocation());
        }
//******
        r = evalOp(operation,processMap,interpreter,context);
        //System.out.println("operation "+ firstId+" "+operation.getOperation()+" "+secondId+" "+r);
        //Add type to funct so we can pass the correct parameters
//*********
        //now evaluate the operation




        //System.out.println("operation "+ firstId+" "+operation.getOperation()+" "+secondId+" "+r);
        OperationResult result = new OperationResult(operation.getFirstProcess(),
                operation.getSecondProcess(), firstId, secondId,
                operation.getOperation(), null, operation.isNegated(), r, "");

        return result;
    }


    public boolean evalOp(OperationNode operation,
                          Map<String, ProcessModel> processMap,
                          Interpreter interpreter,
                          Context context)
            throws CompilationException, InterruptedException {
        List<ProcessModel> processModels = new ArrayList<>();
        boolean r = false;
        //System.out.println("evalOp "+operation.getOperation());
        IOperationInfixFunction funct = instantiateClass(operationsMap.get(operation.getOperation().toLowerCase()));
        //System.out.println("Funct " + funct.getFunctionName()+" "+ processMap.size());
        if (funct == null) {
            throw new CompilationException(getClass(), "The given operation is invaid: "
                    + operation.getOperation(), operation.getLocation());
        }
        /*System.out.println("*********starting Operation " + operation.getFirstProcessType() + " (" +
                operation.getOperation() + "  of type " +
                funct.getOperationType() + ")  " + operation.getSecondProcessType());*/
        //System.out.println(((operation.hell());


        if (funct.getOperationType().equals("petrinet")) {
            String ps = processMap.values().stream().map(x->x.getId()).collect(Collectors.joining(" "));
            //System.out.println("Evaluate petrinet operation pMap "+ps);
            //System.out.println();
// Convert to PetriuNets were needed
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
                //System.out.println("\n**Two "+two.getId());
                processModels.add(two);
            } else if (operation.getSecondProcessType().equals(automata)) {
                Automaton two = (Automaton) interpreter.interpret(automata,
                        operation.getFirstProcess(), getNextOperationId(), processMap, context);
                processModels.add(OwnersRule.ownersRule( two));
            }

            //System.out.println("oper "+ operation.getOperation().toLowerCase());
            //Add type to funct so we can pass the correct parameters

//now convert to Aut
            // on first operand apply a2p2a if needed
            //System.out.println("processMap.keySet() "+processMap.keySet());
            //System.out.println(pnets.get(0).getId());
            //System.out.println(processMap.get(pnets.get(0).getId()).getProcessType());

            r = funct.evaluate(processModels);
            if (operation.isNegated()) { r = !r; }

        } else if (funct.getOperationType().equals(automata)) {
            //System.out.println("Evaluate automaton operation "+operation.getFirstProcessType()+ " "+operation.getSecondProcessType());

// Convert to PetriuNets were needed
            if (operation.getFirstProcessType().equals("petrinet")) {
                Petrinet one = (Petrinet) interpreter.interpret("petrinet",
                        operation.getFirstProcess(), getNextOperationId(), processMap, context);
                processModels.add(TokenRule.tokenRule(one));
            } else if (operation.getFirstProcessType().equals(automata)) {
                Automaton one = (Automaton) interpreter.interpret(automata,
                        operation.getFirstProcess(), getNextOperationId(), processMap, context);
                processModels.add(one);
            }
            //System.out.println("*1* "+((Automaton) processModels.get(0)).myString());
            if (operation.getSecondProcessType().equals("petrinet")) {
                Petrinet two = (Petrinet) interpreter.interpret("petrinet",
                        operation.getSecondProcess(), getNextOperationId(), processMap, context);
                //System.out.println("\n**Two "+two.getId());
                processModels.add(TokenRule.tokenRule(two));
            } else if (operation.getSecondProcessType().equals(automata)) {
                Automaton two = (Automaton) interpreter.interpret(automata,
                        operation.getSecondProcess(), getNextOperationId(), processMap, context);
                processModels.add(two);
            }
            //System.out.println("*2*"+((Automaton) processModels.get(1)).myString());
            //System.out.println("oper "+ operation.getOperation().toLowerCase());
            r = funct.evaluate(processModels);
            if (operation.isNegated()) { r = !r; }

        } else {
            System.out.println("Bad operation type "+operation.getOperationType());
        }
        return r;
    }

    static List<String> collectIdentifiers(ASTNode process) {
        List<String> ids = new ArrayList<>();
        collectIdentifiers(process, ids);
        //System.out.println("OperationEvaluator Found "+ids);
        return ids;
    }

    /**
     * A recursive search for finding identifiers in an ast
     *
     * @param process the ast node that has identifiers in it that are to be collected
     * @param ids     the returned collection
     */
    private static void collectIdentifiers(ASTNode process, List<String> ids) {
        if (process instanceof IdentifierNode) {

            ids.add(((IdentifierNode) process).getIdentifier());
        }

        if (process instanceof ChoiceNode) {
            collectIdentifiers(((ChoiceNode) process).getFirstProcess(), ids);
            collectIdentifiers(((ChoiceNode) process).getSecondProcess(), ids);
        }
        if (process instanceof CompositeNode) {
            collectIdentifiers(((CompositeNode) process).getFirstProcess(), ids);
            collectIdentifiers(((CompositeNode) process).getSecondProcess(), ids);
//        int numberNull = 0;
//        for (Expr c : subMap.values())
//            if(c == null)
//                numberNull++;
//
//        //System.out.println("NUmber null" + numberNull);
        }
        if (process instanceof FunctionNode) {
            ((FunctionNode) process).getProcesses().forEach(p -> collectIdentifiers(p, ids));
        }
        if(process instanceof ProcessRootNode) {
            collectIdentifiers(((ProcessRootNode)process).getProcess(), ids);
        }

        if (process instanceof IfStatementExpNode) {
            collectIdentifiers(((IfStatementExpNode) process).getTrueBranch(), ids);
            if (((IfStatementExpNode) process).hasFalseBranch()) {
                collectIdentifiers(((IfStatementExpNode) process).getFalseBranch(), ids);
            }
        }

        if (process instanceof SequenceNode) {
            collectIdentifiers(((SequenceNode) process).getTo(), ids);
        }
    }

    /**
     *
     * @param firstProcess
     * @param code
     * @return
     */
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
        return String.join("", lines);
    }

    private String getNextOperationId() {
        return "op" + operationId++;
    }

    private void reset() {
        operationId = 0;
    }
}
