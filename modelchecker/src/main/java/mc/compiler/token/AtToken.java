package mc.compiler.token;

import mc.util.Location;

public class AtToken extends SymbolToken {

	public AtToken(Location location){
		super(location);
	}

	public String toString(){
		return "@";
	}

}