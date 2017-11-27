package mc.compiler.token;

import mc.util.Location;

public class CloseBracketToken extends SymbolToken {

	public CloseBracketToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof CloseBracketToken;
	}

	public String toString(){
		return "]";
	}

}