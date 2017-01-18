package mc.compiler.token;

import mc.util.Location;

public class InterruptToken extends SymbolToken {

	public InterruptToken(Location location){
		super(location);
	}

	public String toString(){
		return "~>";
	}

}