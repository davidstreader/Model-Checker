package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class SetNode extends ASTNode {

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
}
