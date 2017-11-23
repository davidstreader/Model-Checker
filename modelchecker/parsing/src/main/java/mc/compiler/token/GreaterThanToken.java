package mc.compiler.token;

import mc.util.Location;

public class GreaterThanToken extends OperatorToken {

	public GreaterThanToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof GreaterThanToken;
	}

	public String toString(){
		return ">";
	}

}