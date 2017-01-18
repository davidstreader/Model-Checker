package mc.compiler.ast;

import mc.util.Location;

public class RangeNode extends ASTNode {

	// fields
	private int start;
	private int end;

	public RangeNode(int start, int end, Location location){
		super(location);
		this.start = start;
		this.end = end;
	}

	public int getStart(){
		return start;
	}

	public void setStart(int start){
		this.start = start;
	}

	public int getEnd(){
		return end;
	}

	public void setEnd(int end){
		this.end = end;
	}
}
