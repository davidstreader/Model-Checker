package mc.compiler.token;

import mc.util.Location;

public class LessThanToken extends OperatorToken {

	public LessThanToken(Location location){
		super(location);
	}

	public String toString(){
		return "<";
	}

}