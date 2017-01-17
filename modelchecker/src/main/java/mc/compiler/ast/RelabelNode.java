package mc.compiler.ast;

import java.util.List;

import mc.util.Location;

public class RelabelNode extends ASTNode {

	// fields
	private List<RelabelElementNode> relabels;

	public RelabelNode(List<RelabelElementNode> relabels, Location location){
		super(location);
		this.relabels = relabels;
	}

	public List<RelabelElementNode> getRelabels(){
		return relabels;
	}

}
