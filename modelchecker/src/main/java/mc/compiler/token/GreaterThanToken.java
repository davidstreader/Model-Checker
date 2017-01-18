package mc.compiler.token;

import mc.util.Location;

public class GreaterThanToken extends OperatorToken {

	public GreaterThanToken(Location location){
		super(location);
	}

	public String toString(){
		return ">";
	}

}