package mc.compiler.ast;

import mc.util.Location;

public abstract class ASTNode {

	// fields
	private Location location;

	public ASTNode(Location location){
		this.location = location;
	}

	public Location getLocation(){
		return location;
	}
}
