package mc.compiler.token;

import mc.util.Location;

public class RangeSeparatorToken extends SymbolToken {

	public RangeSeparatorToken(Location location){
		super(location);
	}

	public String toString(){
		return "..";
	}

}