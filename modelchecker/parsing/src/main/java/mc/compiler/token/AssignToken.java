package mc.compiler.token;

import mc.util.Location;

public class AssignToken extends SymbolToken {
	
	public AssignToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof AssignToken;
	}

	public String toString(){
		return "=";
	}

}