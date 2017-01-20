package mc.compiler.token;

import mc.util.Location;

public class ModuloToken extends OperatorToken {

	public ModuloToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof ModuloToken;
	}

	public String toString(){
		return "%";
	}

}