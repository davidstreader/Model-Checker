package mc.compiler.ast;

import com.microsoft.z3.Expr;
import java.util.List;
import java.util.Map;
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
   * Processes contains the concrete syntax trees for the processes defined in the users code.
   */
  private final List<ProcessNode> processes;

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
   */
  private final Map<String, Expr> variableMap;

  /**
   * The Process Hierarchy contains the dependencies for each process, this is used in the
   * propagation of process types, and may be used in the future for parallel compilation.
   */
  private ProcessHierarchy processHierarchy = null;
}
