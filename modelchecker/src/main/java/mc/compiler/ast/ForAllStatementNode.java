package mc.compiler.ast;

import mc.util.Location;

public class ForAllStatementNode extends ASTNode {

	// fields
	private RangesNode ranges;
	private ASTNode process;

	public ForAllStatementNode(RangesNode ranges, ASTNode process, Location location){
		super(location);
		this.ranges = ranges;
		this.process = process;
	}

	public RangesNode getRanges(){
		return ranges;
	}

	public ASTNode getProcess(){
		return process;
	}
}
