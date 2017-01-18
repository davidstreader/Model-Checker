package mc.compiler.ast;

import mc.util.Location;

public class LocalProcessNode extends ASTNode {

	// fields
	private String identifier;
	private RangesNode ranges;
	private ASTNode process;

	public LocalProcessNode(String identifier, RangesNode ranges, ASTNode process, Location location){
		super(location);
		this.identifier = identifier;
		this.ranges = ranges;
		this.process = process;
	}

	public String getIdentifier(){
		return identifier;
	}

	public void setIdentifier(String identifier){
		this.identifier = identifier;
	}

	public RangesNode getRanges(){
		return ranges;
	}

	public void setRanges(RangesNode ranges){
		this.ranges = ranges;
	}

	public ASTNode getProcess(){
		return process;
	}

	public void setProcess(ASTNode process){
		this.process = process;
	}
}
