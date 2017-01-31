package mc.compiler.ast;

import mc.compiler.token.RangeSeparatorToken;
import mc.util.Location;

public class RelabelElementNode extends ASTNode {

	// fields;
	private String newLabel;
	private String oldLabel;
    private RangesNode ranges;

	public RelabelElementNode(String newLabel, String oldLabel, Location location){
		super(location);
		this.newLabel = newLabel;
		this.oldLabel = oldLabel;
        ranges = null;
	}

	public String getNewLabel(){
		return newLabel;
	}

	public String getOldLabel(){
		return oldLabel;
	}

    public RangesNode getRanges(){
        return ranges;
    }

    public void setRanges(RangesNode ranges){
        this.ranges = ranges;
    }

    public boolean hasRanges(){
        return ranges != null;
    }

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof RelabelElementNode){
            RelabelElementNode node = (RelabelElementNode)obj;
            if(!newLabel.equals(node.getNewLabel())){
                return false;
            }
            if(!oldLabel.equals(node.getOldLabel())){
                return false;
            }
            if(hasRanges() && node.hasRanges() && !ranges.equals(node.getRanges())){
                return false;
            }

            return true;
        }

        return false;
    }
}
