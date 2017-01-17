package mc.compiler.ast;

import java.util.Set;

import mc.util.Location;

public class SetNode extends ASTNode {

	// fields
	private Set<String> set;

	public SetNode(Set<String> set, Location location){
		super(location);
		this.set = set;
	}

	public Set<String> getSet(){
		return set;
	}
}
