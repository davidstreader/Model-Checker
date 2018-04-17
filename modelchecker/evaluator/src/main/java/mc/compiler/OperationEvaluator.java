package mc.compiler;

import static mc.util.Utils.instantiateClass;

import com.microsoft.z3.Context;

import java.util.*;

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
import mc.processmodels.ProcessModel;
import mc.processmodels.ProcessType;
import mc.processmodels.automata.Automaton;
import mc.processmodels.petrinet.Petrinet;
import mc.util.Location;

/**
 * Created by sheriddavi on 27/01/17.
 */
public class OperationEvaluator {

  private int operationId;

  static Map<String, Class<? extends IOperationInfixFunction>> operationsMap = new HashMap<>();

 /**
  *
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
      //System.out.println("starting Operation "+ operation.toString());
      boolean secondA2P2A = false; boolean firstA2P2A = false;
      if (operation.getSecondProcess() instanceof FunctionNode &&
        ((FunctionNode) operation.getSecondProcess()).getFunction().equals("a2p2a")) {
        //System.out.println("2 DO the a2p2a");
        secondA2P2A = true;
        operation.setSecondProcess(((FunctionNode) operation.getSecondProcess()).getProcesses().get(0));
      }
      if (operation.getFirstProcess() instanceof FunctionNode &&
        ((FunctionNode) operation.getFirstProcess()).getFunction().equals("a2p2a")) {
        //System.out.println("1 DO the a2p2a");
        firstA2P2A = true;
        operation.setFirstProcess(((FunctionNode) operation.getFirstProcess()).getProcesses().get(0));
      }
      String firstId = findIdent(operation.getFirstProcess(), code);
      String secondId = findIdent(operation.getSecondProcess(), code);
  //System.out.println("evaluateOperations  first "+firstId+ "  second "+ secondId);
      List<String> firstIds = collectIdentifiers(operation.getFirstProcess());
      List<String> secondIds = collectIdentifiers(operation.getSecondProcess());
      //System.out.println("second "+operation.getSecondProcess().toString());
      List<Automaton> automata = new ArrayList<>();
      List<Petrinet> pnets = new ArrayList<>();
      List<String> missing = new ArrayList<>(firstIds);

      missing.addAll(secondIds);  // all process ids
      missing.removeAll(processMap.keySet());

      if (!missing.isEmpty()) {
        throw new CompilationException(OperationEvaluator.class, "Identifier " + missing.get(0) + " not found!", operation.getLocation());
      }
// process ids all valid - in processMap
      //if (operation.getSecondProcess()().)
      pnets.add((Petrinet) interpreter.interpret("petrinet", operation.getFirstProcess(), getNextOperationId(), processMap, context));
      pnets.add((Petrinet) interpreter.interpret("petrinet", operation.getSecondProcess(), getNextOperationId(), processMap, context));
      //automata.add((Automaton) interpreter.interpret("automata", operation.getFirstProcess(), getNextOperationId(), processMap, context));
      //automata.add((Automaton) interpreter.interpret("automata", operation.getSecondProcess()(), getNextOperationId(), processMap, context));
// built all automata processs
  //System.out.println("oper "+ operation.getOperation().toLowerCase());
      IOperationInfixFunction funct = instantiateClass(operationsMap.get(operation.getOperation().toLowerCase()));
      if (funct == null) {
        throw new CompilationException(getClass(), "The given operation is invaid: "
            + operation.getOperation(), operation.getLocation());
      }
//now convert to Aut
      // on first operand apply a2p2a if needed
      Automaton newAut;
      if (processMap.get(pnets.get(0).getId()).getProcessType().
        equals(ProcessType.MULTI_PROCESS)) {
        newAut = (processMap.get(pnets.get(0).getId()).getProcessType().
          convertTo(ProcessType.AUTOMATA, processMap.get(pnets.get(0).getId()))); //What a way to extact  a net
        if (firstA2P2A==true) {
          //System.out.println("****1p1");
          A2P2A a2p = new A2P2A();
          newAut =  a2p.compose(pnets.get(0).getId()+"1a",
            Collections.emptySet(), context, newAut);

          //System.out.println("****1p2"+newAut.myString()+"\n");
        }
      } else {
        newAut = ((Automaton) processMap.get(pnets.get(0).getId()));
      }

      automata.add(newAut); //Add first automata

      //on second operand apply a2p2a if needed
      if (processMap.get(pnets.get(1).getId()).getProcessType().
        equals(ProcessType.MULTI_PROCESS)) {
        newAut = (processMap.get(pnets.get(1).getId()).getProcessType().
          convertTo(ProcessType.AUTOMATA, processMap.get(pnets.get(1).getId())));
        if (secondA2P2A==true) {
          //System.out.println("p1");
          A2P2A a2p = new A2P2A();
           newAut =  a2p.compose(pnets.get(1).getId()+"2a",
                       Collections.emptySet(), context, newAut);
          //System.out.println("p2 a2p2a "+newAut.myString());
        }

      } else {
        newAut = ((Automaton) processMap.get(pnets.get(1).getId()));
      }
      automata.add(newAut); // Add second automata

//now evaluate the operation
      boolean result = funct.evaluate(automata);
//now process the results
      if (operation.isNegated()) {
        result = !result;
      }
      results.add(new OperationResult(operation.getFirstProcess(),
           operation.getSecondProcess(), firstId, secondId,
           operation.getOperation(), null, operation.isNegated(), result, ""));

      //System.out.println("operation "+ firstId+" "+operation.getOperation()+" "+secondId);
    }
    return results;
  }

  static List<String> collectIdentifiers(ASTNode process) {
    List<String> ids = new ArrayList<>();
    collectIdentifiers(process, ids);
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
