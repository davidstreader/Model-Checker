package mc.compiler.ast;

import mc.util.Location;

public class IndexNode extends ASTNode {

	// fields
	private String variable;
	private ASTNode range;

	public IndexNode(String variable, ASTNode range, Location location){
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
}
