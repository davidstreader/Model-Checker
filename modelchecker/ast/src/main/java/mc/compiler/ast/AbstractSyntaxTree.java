package mc.compiler.ast;

import com.microsoft.z3.Expr;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import mc.compiler.ProcessHierarchy;

import java.util.List;
import java.util.Map;
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
