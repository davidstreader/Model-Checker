package mc.compiler.ast;

import java.util.Set;

import mc.util.Location;

public class HidingNode extends ASTNode {

	// fields
	private String type;
	private Set<String> set;

	public HidingNode(String type, Set<String> set, Location location){
		super(location);
		this.type = type;
		this.set = set;
	}

	public String getType(){
		return type;
	}

	public Set<String> getSet(){
		return set;
	}
}
