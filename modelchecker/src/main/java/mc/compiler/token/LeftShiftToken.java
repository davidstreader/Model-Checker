package mc.compiler.token;

import mc.util.Location;

public class LeftShiftToken extends OperatorToken {

	public LeftShiftToken(Location location){
		super(location);
	}

	public String toString(){
		return "<<";
	}

}