package mc.compiler.token;

import mc.util.Location;

public class DollarToken extends SymbolToken {

	public DollarToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof DollarToken;
	}

	public String toString(){
		return "$";
	}

}