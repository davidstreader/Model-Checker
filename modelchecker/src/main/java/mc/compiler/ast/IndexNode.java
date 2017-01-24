package mc.compiler.ast;

import mc.util.Location;

public class IndexNode extends ASTNode {

	// fields
	private String variable;
	private ASTNode range;
	private ASTNode process;

	public IndexNode(String variable, ASTNode range, ASTNode process, Location location){
		super(location);
		this.variable = variable;
		this.range = range;
	}

	public String getVariable(){
		return variable;
	}

	public void setVariable(String variable){
		this.variable = variable;
	}

	public ASTNode getRange(){
		return range;
	}

	public void setRange(ASTNode range){
		this.range = range;
	}

	public ASTNode getProcess(){
		return process;
	}

}
