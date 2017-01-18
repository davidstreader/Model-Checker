package mc.compiler.token;

import mc.util.Location;

public class HideToken extends SymbolToken {

	public HideToken(Location location){
		super(location);
	}

	public String toString(){
		return "\\";
	}

}