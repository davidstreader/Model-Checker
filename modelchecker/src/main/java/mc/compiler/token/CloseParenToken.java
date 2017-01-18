package mc.compiler.token;

import mc.util.Location;

public class CloseParenToken extends SymbolToken {

	public CloseParenToken(Location location){
		super(location);
	}

	public String toString(){
		return ")";
	}

}