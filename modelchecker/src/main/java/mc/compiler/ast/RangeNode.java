package mc.compiler.ast;

import mc.util.Location;

public class RangeNode extends ASTNode {

	// fields
	private String start;
	private String end;

	public RangeNode(String start, String end, Location location){
		super(location);
		this.start = start;
		this.end = end;
	}

	public String getStart(){
		return start;
	}

	public void setStart(String start){
		this.start = start;
	}

	public String getEnd(){
		return end;
	}

	public void setEnd(String end){
		this.end = end;
	}

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof RangeNode){
            RangeNode node = (RangeNode)obj;
            if(!start.equals(node.getStart())){
                return false;
            }
            if(!end.equals(node.getEnd())){
                return false;
            }

            return true;
        }

        return false;
    }
}
