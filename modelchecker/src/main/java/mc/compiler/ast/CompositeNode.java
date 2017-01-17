package mc.compiler.ast;

import mc.util.Location;

public class CompositeNode extends ASTNode {

	// fields
	private ASTNode firstProcess;
	private ASTNode secondProcess;

	public CompositeNode(ASTNode firstProcess, ASTNode secondProcess, Location location){
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
