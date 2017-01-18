package mc.compiler.token;

import mc.util.Location;

public class CommaToken extends SymbolToken {

	public CommaToken(Location location){
		super(location);
	}

	public String toString(){
		return ",";
	}

}