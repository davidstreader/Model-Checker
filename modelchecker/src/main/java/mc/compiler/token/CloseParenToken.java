package mc.compiler.token;

import mc.util.Location;

public class CloseParenToken extends SymbolToken {

	public CloseParenToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof CloseParenToken;
	}

	public String toString(){
		return ")";
	}

}