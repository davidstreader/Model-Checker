package mc.compiler.token;

import mc.util.Location;

public class AtToken extends SymbolToken {

	public AtToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof AtToken;
	}

	public String toString(){
		return "@";
	}

}