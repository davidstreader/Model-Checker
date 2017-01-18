package mc.compiler.token;

import mc.util.Location;

public class ColonToken extends SymbolToken {
	
	public ColonToken(Location location){
		super(location);
	}
	
	public String toString(){
		return ":";
	}
	
}
