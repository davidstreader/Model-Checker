package mc.compiler.ast;

import com.microsoft.z3.Expr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import mc.compiler.ProcessHierarchy;

import java.util.List;
import java.util.Map;
/**
 * AbstractSyntaxTree holds a collection of processes, operations and equations to be later used by
 * the interpreter to create process models and execute operations.
 *
 * @see
 * @author David Sheridan
 * @author Sanjay Govind
 */
@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class AbstractSyntaxTree {
	private final List<ProcessNode>   processes;
	private final List<OperationNode> operations;
    private final List<OperationNode> equations;
	private final Map<String, Expr>   variableMap;
	private       ProcessHierarchy    processHierarchy = null;
}
