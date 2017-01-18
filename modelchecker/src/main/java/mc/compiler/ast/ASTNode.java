package mc.compiler.ast;

import mc.util.Location;

public abstract class ASTNode {

	// fields
	private String label;
	private RelabelNode relabel;
	private Location location;

	public ASTNode(Location location){
		label = null;
		relabel = null;
		this.location = location;
	}

	public String getLabel(){
		return label;
	}

	public void setLabel(String label){
		this.label = label;
	}

	public boolean hasLabel(){
		return label != null;
	}

	public RelabelNode getRelabel(){
		return relabel;
	}

	public void setRelabelNode(RelabelNode relabel){
		this.relabel = relabel;
	}

	public boolean hasRelabel(){
		return relabel != null;
	}

	public Location getLocation(){
		return location;
	}
}
