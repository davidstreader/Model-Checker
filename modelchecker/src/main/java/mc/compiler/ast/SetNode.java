package mc.compiler.ast;

import java.util.List;

import mc.util.Location;

public class SetNode extends ASTNode {

	// fields
	private List<String> set;

	public SetNode(List<String> set, Location location){
		super(location);
		this.set = set;
	}

	public List<String> getSet(){
		return set;
	}

	public void setSet(List<String> set){
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
