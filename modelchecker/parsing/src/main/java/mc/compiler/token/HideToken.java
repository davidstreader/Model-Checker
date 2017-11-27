package mc.compiler.token;

import mc.util.Location;

public class HideToken extends SymbolToken {

	public HideToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof HideToken;
	}

	public String toString(){
		return "\\";
	}

}