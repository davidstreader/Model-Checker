package mc.compiler.token;

import mc.util.Location;

public class CommaToken extends SymbolToken {

	public CommaToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof CommaToken;
	}

	public String toString(){
		return ",";
	}

}