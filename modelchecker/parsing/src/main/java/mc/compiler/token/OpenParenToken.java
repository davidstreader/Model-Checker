package mc.compiler.token;

import mc.util.Location;

public class OpenParenToken extends SymbolToken {

	public OpenParenToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof OpenParenToken;
	}

	public String toString(){
		return "(";
	}

}