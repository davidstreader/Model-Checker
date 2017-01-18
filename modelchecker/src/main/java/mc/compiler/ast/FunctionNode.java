package mc.compiler.ast;

import mc.util.Location;

public class FunctionNode extends ASTNode {

	// fields
	private String function;
	private ASTNode process;

	public FunctionNode(String function, ASTNode process, Location location){
		super(location);
		this.function = function;
		this.process = process;
	}

	public String getFunction(){
		return function;
	}

	public void setFunction(String function){
		this.function = function;
	}

	public ASTNode getProcess(){
		return process;
	}

	public void setProcess(ASTNode process){
		this.process = process;
	}

}
