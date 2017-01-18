package mc.compiler.token;

import mc.util.Location;

public class LessThanEqToken extends OperatorToken {

	public LessThanEqToken(Location location){
		super(location);
	}

	public String toString(){
		return "<=";
	}

}