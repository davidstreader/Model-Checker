package mc.compiler.token;

import mc.util.Location;

public class OpenBraceToken extends SymbolToken {

	public OpenBraceToken(Location location){
		super(location);
	}

	public String toString(){
		return "{";
	}

}