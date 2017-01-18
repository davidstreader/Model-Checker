package mc.compiler.token;

import mc.util.Location;

public class OpenParenToken extends SymbolToken {

	public OpenParenToken(Location location){
		super(location);
	}

	public String toString(){
		return "(";
	}

}