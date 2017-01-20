package mc.compiler.ast;

import java.util.List;

import mc.util.Location;

public class RangesNode extends ASTNode {

	// fields
	private List<IndexNode> ranges;

	public RangesNode(List<IndexNode> ranges, Location location){
		super(location);
		this.ranges = ranges;
	}

	public List<IndexNode> getRanges(){
		return ranges;
	}

	public void setRanges(List<IndexNode> ranges){
		this.ranges = ranges;
	}
}
