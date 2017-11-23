package mc.compiler.token;

import mc.util.Location;

public class LessThanToken extends OperatorToken {

	public LessThanToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof LessThanToken;
	}

	public String toString(){
		return "<";
	}

}