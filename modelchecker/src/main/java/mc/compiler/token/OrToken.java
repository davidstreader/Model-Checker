package mc.compiler.token;

import mc.util.Location;

public class OrToken extends OperatorToken {

	public OrToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof OrToken;
	}

	public String toString(){
		return "||";
	}

}