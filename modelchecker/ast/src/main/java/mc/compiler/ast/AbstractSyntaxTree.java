package mc.compiler.ast;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.microsoft.z3.Z3Object;
import com.rits.cloning.Cloner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import mc.compiler.ProcessHierarchy;

/**
 * AbstractSyntaxTree holds a collection of processes, operations and equations to be later used by
 * the interpreter to create process models and execute operations.
 *
 * @author David Sheridan
 * @author Sanjay Govind
 * @author Jacob Beal
 * @see ProcessNode
 * @see OperationNode
 * @see ProcessHierarchy
 */
@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class AbstractSyntaxTree {
  /**
   * an AST for whole file not just one process
   * Processes contains the concrete syntax trees for the processes defined in the users code.
   * A ProcessNode is an AST for a single process
   */
  private final List<ProcessNode> processes;
  /**
   * Needed in the construction of Galos connections. Defines a global alphabet of listening events
   * Broadcast processes have "implicit" listening loops and this is taken into account in
   * 1. parallel composition
   * 2. quiescent trace equality
   * But it was easier to build a global Domain for the BC2AP Galois mapping  than perform a two phase
   * Interpretation, the first to build the sey of listening events on both relevent processes.
   */
  private final List<ActionLabelNode> alphabet;

  /**
   * Operations contains concrete syntax trees for the evaluations defined by the users code,
   * for the processes defined by the users code.
   */
  private final List<OperationNode> operations;

  /**
   * Equations contain concrete syntax trees for relationships defined by the user, that are tested
   * against numerous generated processes, to test whether a given relationship is general.
   */
  private final List<OperationNode> equations;

  /**
   * TODO: find out how this is used.
   * should be a symbolic evaluation  variable to Z3 expression.
   * Now if this is built by the expansion then a similar construction
   * should be built on a symbolic automata or symbolic petri net!
   */
  private final Map<String, Expr> variableMap;

  /**
   * The Process Hierarchy contains the dependencies for each process, this is used in the
   * propagation of process types, and may be used in the future for parallel compilation.
   */
  private ProcessHierarchy processHierarchy = null;

  public String processesToString() {
    String out = "";
     for(ProcessNode pn: this.processes) {
       out = out + pn.toString()+ " ";
     }
     return out;
  }

  public String processes2String() {
    StringBuilder sb = new StringBuilder();
    sb.append("AST \n");
    for(ProcessNode pn: processes) {
      sb.append(pn.myString()+"\n");
    }
    if (variableMap!=null) {
      sb .append("variableMap ");
      for(String key: variableMap.keySet()){
        sb.append(key+"->"+variableMap.get(key).toString()+", ");
      }
    }
    return sb.toString();
    }
  public String myString() {
    StringBuilder sb = new StringBuilder();
    sb .append("AST \n");
    sb.append( processes.stream().map(x->x.myString()+"\n").collect(Collectors.joining()) );

    if(variableMap!=null) sb.append(variableMap.keySet().stream().
         map(x->x+"->"+variableMap.get(x).toString()+" ").collect(Collectors.joining()));
    return sb.toString();
  }


  /**
   * Clone the current Node.
   *
   * @return a deep copy of the current node.
   * @see Cloner
   */
  public AbstractSyntaxTree copy() {
    Cloner cloner = new Cloner();
    cloner.dontClone(Context.class);
    cloner.dontClone(Z3Object.class);
    cloner.dontClone(Expr.class);
    cloner.dontClone(BoolExpr.class);
    return cloner.deepClone(this);
  }
}
