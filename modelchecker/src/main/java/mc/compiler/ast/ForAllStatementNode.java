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

	public void setRanges(RangesNode ranges){
		this.ranges = ranges;
	}

	public ASTNode getProcess(){
		return process;
	}

	public void setProcess(ASTNode process){
		this.process = process;
	}

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof ForAllStatementNode){
            ForAllStatementNode node = (ForAllStatementNode)obj;
            if(!ranges.equals(node.getRanges())){
                return false;
            }
            if(!process.equals(node.getProcess())){
                return false;
            }

            return true;
        }

        return false;
    }
}
