package mc.compiler.token;

import mc.util.Location;

public class InterruptToken extends SymbolToken {

	public InterruptToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof InterruptToken;
	}

	public String toString(){
		return "~>";
	}

}