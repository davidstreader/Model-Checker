package mc.compiler.token;

import mc.util.Location;

public class GreaterThanEqToken extends OperatorToken {

	public GreaterThanEqToken(Location location){
		super(location);
	}

	public String toString(){
		return ">=";
	}

}