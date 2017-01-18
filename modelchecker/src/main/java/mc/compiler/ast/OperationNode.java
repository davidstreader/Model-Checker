package mc.compiler.ast;

import mc.util.Location;

public class OperationNode extends ASTNode {

	// fields
	private String operation;
	private boolean isNegated;
	private ASTNode firstProcess;
	private ASTNode secondProcess;

	public OperationNode(String operation, boolean isNegated, ASTNode firstProcess, ASTNode secondProcess, Location location){
		super(location);
		this.operation = operation;
		this.isNegated = isNegated;
		this.firstProcess = firstProcess;
		this.secondProcess = secondProcess;
	}

	public String getOperation(){
		return operation;
	}

	public boolean isNegated(){
		return isNegated;
	}

	public ASTNode getFirstProcess(){
		return firstProcess;
	}

	public ASTNode getSecondProcess(){
		return secondProcess;
	}

}
