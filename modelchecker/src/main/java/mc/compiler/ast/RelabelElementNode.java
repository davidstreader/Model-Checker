package mc.compiler.ast;

import mc.util.Location;

public class RelabelElementNode extends ASTNode {

	// fields;
	private String newLabel;
	private String oldLabel;

	public RelabelElementNode(String newLabel, String oldLabel, Location location){
		super(location);
		this.newLabel = newLabel;
		this.oldLabel = oldLabel;
	}

	public String getNewLabel(){
		return newLabel;
	}

	public String getOldLabel(){
		return oldLabel;
	}
}
