package mc.compiler.token;

import mc.util.Location;

public class GreaterThanEqToken extends OperatorToken {

	public GreaterThanEqToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof GreaterThanEqToken;
	}

	public String toString(){
		return ">=";
	}

}