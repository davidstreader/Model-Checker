package mc.compiler.token;

import mc.util.Location;

public class MultiplicationToken extends OperatorToken {

	public MultiplicationToken(Location location){
		super(location);
	}

	public String toString(){
		return "*";
	}

}