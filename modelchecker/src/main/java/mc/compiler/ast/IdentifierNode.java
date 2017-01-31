package mc.compiler.ast;

import mc.util.Location;

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
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof IdentifierNode){
            IdentifierNode node = (IdentifierNode)obj;
            return identifier.equals(node.getIdentifier());
        }

        return false;
    }
}
