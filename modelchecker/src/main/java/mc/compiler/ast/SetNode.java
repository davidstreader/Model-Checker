package mc.compiler.ast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mc.util.Location;

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
        this.rangeMap = new HashMap<Integer, RangesNode>();
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
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
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
