package mc.compiler.ast;

import mc.util.Location;

public class HidingNode extends ASTNode {

	// fields
	private String type;
	private SetNode set;

	public HidingNode(String type, SetNode set, Location location){
		super(location);
		this.type = type;
		this.set = set;
	}

	public String getType(){
		return type;
	}

	public SetNode getSet(){
		return set;
	}

    public void setSet(SetNode set){
        this.set = set;
    }

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
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
