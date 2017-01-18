package mc.compiler.token;

import mc.util.Location;

public class AssignToken extends SymbolToken {
	
	public AssignToken(Location location){
		super(location);
	}
	
	public String toString(){
		return "=";
	}

}