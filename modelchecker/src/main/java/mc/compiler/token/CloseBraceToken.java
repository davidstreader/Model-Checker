package mc.compiler.token;

import mc.util.Location;

public class CloseBraceToken extends SymbolToken {

	public CloseBraceToken(Location location){
		super(location);
	}

	public String toString(){
		return "}";
	}

}