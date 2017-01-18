package mc.compiler.ast;

import mc.util.Location;

public class IdentifierNode extends ASTNode {

	// fields
	String identifier;

	public IdentifierNode(String identifier, Location location){
		super(location);
		this.identifier = identifier;
	}

	public String getIdentifier(){
		return identifier;
	}

	public void setIdentifer(String identifier){
		this.identifier = identifier;
	}
}
