package mc.compiler.ast;

import com.microsoft.z3.Expr;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
@AllArgsConstructor
@Getter
public class AbstractSyntaxTree {
	// fields
	private List<ProcessNode> processes;
	private List<OperationNode> operations;
    private List<OperationNode> equations;
	private Map<String, Expr> variableMap;

}
