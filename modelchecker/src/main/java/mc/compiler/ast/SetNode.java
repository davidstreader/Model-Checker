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

	public void setSet(Set<String> set){
		this.set = set;
	}

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof SetNode){
            SetNode node = (SetNode)obj;
            return set.equals(node.getSet());
        }

        return false;
    }
}
