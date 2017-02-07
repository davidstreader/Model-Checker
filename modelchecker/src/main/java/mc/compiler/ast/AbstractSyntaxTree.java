package mc.compiler.ast;

import mc.util.expr.Expression;

import java.util.List;
import java.util.Map;

public class AbstractSyntaxTree {

	// fields
	private List<ProcessNode> processes;
	private List<OperationNode> operations;
	private Map<String, Expression> variableMap;

	public AbstractSyntaxTree(List<ProcessNode> processes, List<OperationNode> operations, Map<String, Expression> variableMap){
		this.processes = processes;
		this.operations = operations;
		this.variableMap = variableMap;
	}

	public List<ProcessNode> getProcesses(){
		return processes;
	}

	public List<OperationNode> getOperations(){
		return operations;
	}

	public Map<String, Expression> getVariableMap(){
		return variableMap;
	}

}
