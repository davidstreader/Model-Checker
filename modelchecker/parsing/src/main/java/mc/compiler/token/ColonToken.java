package mc.compiler.token;

import mc.util.Location;

public class ColonToken extends SymbolToken {
	
	public ColonToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof ColonToken;
	}

	public String toString(){
		return ":";
	}
	
}
