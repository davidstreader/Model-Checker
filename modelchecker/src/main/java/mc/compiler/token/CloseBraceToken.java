package mc.compiler.token;

import mc.util.Location;

public class CloseBraceToken extends SymbolToken {

	public CloseBraceToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof CloseBraceToken;
	}

	public String toString(){
		return "}";
	}

}