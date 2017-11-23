package mc.compiler.ast;

import lombok.ToString;
import mc.util.Location;
@ToString
public class SequenceNode extends ASTNode {

	// fields
	private ActionLabelNode from;
	private ASTNode to;

	public SequenceNode(ActionLabelNode from, ASTNode to, Location location){
		super(location);
		this.from = from;
		this.to = to;
	}

	public ActionLabelNode getFrom(){
		return from;
	}

	public void setFrom(ActionLabelNode from){
		this.from = from;
	}

	public ASTNode getTo(){
		return to;
	}

	public void setTo(ASTNode to){
		this.to = to;
	}

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj instanceof SequenceNode){
            SequenceNode node = (SequenceNode)obj;
            if(!from.equals(node.getFrom())){
                return false;
            }
            return to.equals(node.getTo());
        }

        return false;
    }
}
