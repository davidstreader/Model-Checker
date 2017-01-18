package mc.compiler.token;

import mc.util.Location;

public class OpenBracketToken extends SymbolToken {

	public OpenBracketToken(Location location){
		super(location);
	}

	public String toString(){
		return "[";
	}

}