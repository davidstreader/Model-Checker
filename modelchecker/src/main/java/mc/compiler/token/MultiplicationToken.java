package mc.compiler.token;

import mc.util.Location;

public class MultiplicationToken extends OperatorToken {

	public MultiplicationToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof MultiplicationToken;
	}

	public String toString(){
		return "*";
	}

}