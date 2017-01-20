package mc.compiler.token;

import mc.util.Location;

public class RightShiftToken extends OperatorToken {

	public RightShiftToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof RightShiftToken;
	}

	public String toString(){
		return ">>";
	}

}