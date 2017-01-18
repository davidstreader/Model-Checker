package mc.compiler.ast;

import java.util.List;

import mc.util.Location;

public class RangesNode extends ASTNode {

	// fields
	private List<ASTNode> ranges;

	public RangesNode(List<ASTNode> ranges, Location location){
		super(location);
		this.ranges = ranges;
	}

	public List<ASTNode> getRanges(){
		return ranges;
	}

	public void setRanges(List<ASTNode> ranges){
		this.ranges = ranges;
	}
}
