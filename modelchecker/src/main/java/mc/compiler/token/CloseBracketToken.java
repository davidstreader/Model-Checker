package mc.compiler.token;

import mc.util.Location;

public class CloseBracketToken extends SymbolToken {

	public CloseBracketToken(Location location){
		super(location);
	}

	public String toString(){
		return "]";
	}

}