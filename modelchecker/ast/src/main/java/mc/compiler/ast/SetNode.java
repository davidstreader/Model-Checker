package mc.compiler.ast;

import mc.util.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetNode extends ASTNode {

	// fields
	private List<String> set;
    private Map<Integer, RangesNode> rangeMap;

	public SetNode(List<String> set, Map<Integer, RangesNode> rangeMap, Location location){
		super(location);
		this.set = set;
        this.rangeMap = rangeMap;
	}

    public SetNode(List<String> set, Location location){
        super(location);
        this.set = set;
        this.rangeMap = new HashMap<>();
    }

	public List<String> getSet(){
		return set;
	}

	public void setSet(List<String> set){
		this.set = set;
	}

    public Map<Integer, RangesNode> getRangeMap(){
        return rangeMap;
    }

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj instanceof SetNode){
            SetNode node = (SetNode)obj;
            if(!rangeMap.equals(node.getRangeMap())){
                return false;
            }

            return set.equals(node.getSet());
        }

        return false;
    }
}
