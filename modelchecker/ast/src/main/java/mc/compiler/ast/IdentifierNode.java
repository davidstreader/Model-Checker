package mc.compiler.ast;

import lombok.ToString;
import mc.util.Location;
@ToString
public class IdentifierNode extends ASTNode {

	// fields
	private String identifier;

	public IdentifierNode(String identifier, Location location){
		super(location);
		this.identifier = identifier;
	}

	public String getIdentifier(){
		return identifier;
	}

	public void setIdentifer(String identifier){
		this.identifier = identifier;
	}

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj instanceof IdentifierNode){
            IdentifierNode node = (IdentifierNode)obj;
            return identifier.equals(node.getIdentifier());
        }

        return false;
    }
}
