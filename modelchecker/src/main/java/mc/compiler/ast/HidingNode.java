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

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof HidingNode){
            HidingNode node = (HidingNode)obj;
            if(!type.equals(node.getType())){
                return false;
            }
            if(!set.equals(node.getSet())){
                return false;
            }

            return true;
        }

        return false;
    }
}
