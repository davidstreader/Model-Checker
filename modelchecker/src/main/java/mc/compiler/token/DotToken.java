package mc.compiler.token;

import mc.util.Location;

public class DotToken extends SymbolToken {

	public DotToken(Location location){
		super(location);
	}

	public String toString(){
		return ".";
	}

}