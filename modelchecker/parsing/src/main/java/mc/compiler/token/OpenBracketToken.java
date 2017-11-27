package mc.compiler.token;

import mc.util.Location;

public class OpenBracketToken extends SymbolToken {

	public OpenBracketToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof OpenBracketToken;
	}

	public String toString(){
		return "[";
	}

}