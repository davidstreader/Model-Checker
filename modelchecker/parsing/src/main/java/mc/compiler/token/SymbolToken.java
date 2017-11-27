package mc.compiler.token;

import mc.util.Location;

public abstract class SymbolToken extends Token {
	
	public SymbolToken(Location location){
		super(location);
	}
}
