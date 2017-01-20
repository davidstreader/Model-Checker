package mc.compiler.token;

import mc.util.Location;

public class LeftShiftToken extends OperatorToken {

	public LeftShiftToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof LeftShiftToken;
	}

	public String toString(){
		return "<<";
	}

}