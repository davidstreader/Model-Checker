package mc.compiler.token;

import mc.util.Location;

public class DotToken extends SymbolToken {

	public DotToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof DotToken;
	}

	public String toString(){
		return ".";
	}

}