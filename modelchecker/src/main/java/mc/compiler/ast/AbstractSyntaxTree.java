package mc.compiler.ast;

import java.util.List;

public class AbstractSyntaxTree {

	// fields
	private List<ProcessNode> processes;
	private List<OperationNode> operations;

	public AbstractSyntaxTree(List<ProcessNode> processes, List<OperationNode> operations){
		this.processes = processes;
		this.operations = operations;
	}

	public List<ProcessNode> getProcesses(){
		return processes;
	}

	public List<OperationNode> getOperations(){
		return operations;
	}

}
