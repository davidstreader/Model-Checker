package mc.compiler.ast;

import java.util.List;

import mc.util.Location;

public class HidingNode extends ASTNode {

	// fields
	private String type;
	private List<String> set;

	public HidingNode(String type, List<String> set, Location location){
		super(location);
		this.type = type;
		this.set = set;
	}

	public String getType(){
		return type;
	}

	public List<String> getSet(){
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
