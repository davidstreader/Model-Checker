package mc.compiler.ast;

import mc.util.Location;

public class ChoiceNode extends ASTNode {

	// fields
	private ASTNode firstProcess;
	private ASTNode secondProcess;

	public ChoiceNode(ASTNode firstProcess, ASTNode secondProcess, Location location){
		super(location);
		this.firstProcess = firstProcess;
		this.secondProcess = secondProcess;
	}

	public ASTNode getFirstProcess(){
		return firstProcess;
	}

	public ASTNode getSecondProcess(){
		return secondProcess;
	}

}
