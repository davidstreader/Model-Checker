package mc.compiler.token;

import mc.util.Location;

public class IdentifierToken extends Token {

	private String identifier;

	public IdentifierToken(String identifier, Location location){
		super(location);
		this.identifier = identifier;
	}

	public String getIdentifier(){
		return identifier;
	}

	public boolean equals(Object obj){
		if(obj == this){
			return true;
		}
		if(obj instanceof IdentifierToken){
			IdentifierToken token = (IdentifierToken)obj;
			return identifier.equals(token.getIdentifier());
		}

		return false;
	}

	public String toString(){
		return identifier;
	}

}