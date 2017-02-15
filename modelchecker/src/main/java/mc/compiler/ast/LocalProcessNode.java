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

	public ASTNode getProcess(){
		return process;
	}

	public void setProcess(ASTNode process){
		this.process = process;
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
        if(obj instanceof LocalProcessNode){
            LocalProcessNode node = (LocalProcessNode)obj;
            if(!identifier.equals(node.getIdentifier())){
                return false;
            }
            if(ranges == null && node.getRanges() != null || ranges != null && node.getRanges() == null){
                return false;
            }
            if(ranges != null && !ranges.equals(node.getRanges())){
                return false;
            }
            if (process == null) return false;
            if(!process.equals(node.getProcess())){
                return false;
            }

            return true;
        }

        return false;
    }
}
