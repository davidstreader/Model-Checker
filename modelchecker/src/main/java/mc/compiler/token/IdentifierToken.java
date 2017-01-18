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

	public String toString(){
		return identifier;
	}

}