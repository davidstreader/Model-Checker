package mc.compiler.ast;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Z3Object;
import com.rits.cloning.Cloner;
import mc.compiler.ProcessHierarchy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

  public AbstractSyntaxTree(List<ProcessNode> processes, List<ActionLabelNode> alphabet, List<OperationNode> operations, List<OperationNode> equations, Map<String, Expr> variableMap) {
    this.processes = processes;
    this.alphabet = alphabet;
    this.operations = operations;
    this.equations = equations;
    this.variableMap = variableMap;
  }

  public AbstractSyntaxTree(List<ProcessNode> processes, List<ActionLabelNode> alphabet, List<OperationNode> operations, List<OperationNode> equations, Map<String, Expr> variableMap, ProcessHierarchy processHierarchy) {
    this.processes = processes;
    this.alphabet = alphabet;
    this.operations = operations;
    this.equations = equations;
    this.variableMap = variableMap;
    this.processHierarchy = processHierarchy;
  }

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
    sb.append( processes.stream().map(x->"  "+x.myString()+"\n").collect(Collectors.joining()) );

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

  public List<ProcessNode> getProcesses() {
    return this.processes;
  }

  public List<ActionLabelNode> getAlphabet() {
    return this.alphabet;
  }

  public List<OperationNode> getOperations() {
    return this.operations;
  }

  public List<OperationNode> getEquations() {
    return this.equations;
  }

  public Map<String, Expr> getVariableMap() {
    return this.variableMap;
  }

  public ProcessHierarchy getProcessHierarchy() {
    return this.processHierarchy;
  }

  public void setProcessHierarchy(ProcessHierarchy processHierarchy) {
    this.processHierarchy = processHierarchy;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof AbstractSyntaxTree)) return false;
    final AbstractSyntaxTree other = (AbstractSyntaxTree) o;
    if (!other.canEqual((Object) this)) return false;
    final Object this$processes = this.getProcesses();
    final Object other$processes = other.getProcesses();
    if (this$processes == null ? other$processes != null : !this$processes.equals(other$processes)) return false;
    final Object this$alphabet = this.getAlphabet();
    final Object other$alphabet = other.getAlphabet();
    if (this$alphabet == null ? other$alphabet != null : !this$alphabet.equals(other$alphabet)) return false;
    final Object this$operations = this.getOperations();
    final Object other$operations = other.getOperations();
    if (this$operations == null ? other$operations != null : !this$operations.equals(other$operations)) return false;
    final Object this$equations = this.getEquations();
    final Object other$equations = other.getEquations();
    if (this$equations == null ? other$equations != null : !this$equations.equals(other$equations)) return false;
    final Object this$variableMap = this.getVariableMap();
    final Object other$variableMap = other.getVariableMap();
    if (this$variableMap == null ? other$variableMap != null : !this$variableMap.equals(other$variableMap))
      return false;
    final Object this$processHierarchy = this.getProcessHierarchy();
    final Object other$processHierarchy = other.getProcessHierarchy();
    if (this$processHierarchy == null ? other$processHierarchy != null : !this$processHierarchy.equals(other$processHierarchy))
      return false;
    return true;
  }

  protected boolean canEqual(final Object other) {
    return other instanceof AbstractSyntaxTree;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $processes = this.getProcesses();
    result = result * PRIME + ($processes == null ? 43 : $processes.hashCode());
    final Object $alphabet = this.getAlphabet();
    result = result * PRIME + ($alphabet == null ? 43 : $alphabet.hashCode());
    final Object $operations = this.getOperations();
    result = result * PRIME + ($operations == null ? 43 : $operations.hashCode());
    final Object $equations = this.getEquations();
    result = result * PRIME + ($equations == null ? 43 : $equations.hashCode());
    final Object $variableMap = this.getVariableMap();
    result = result * PRIME + ($variableMap == null ? 43 : $variableMap.hashCode());
    final Object $processHierarchy = this.getProcessHierarchy();
    result = result * PRIME + ($processHierarchy == null ? 43 : $processHierarchy.hashCode());
    return result;
  }

  public String toString() {
    return "AbstractSyntaxTree(processes=" + this.getProcesses() + ", alphabet=" + this.getAlphabet() + ", operations=" + this.getOperations() + ", equations=" + this.getEquations() + ", variableMap=" + this.getVariableMap() + ", processHierarchy=" + this.getProcessHierarchy() + ")";
  }
}
