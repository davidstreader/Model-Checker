package mc.compiler.token;

import mc.util.Location;

public class AdditionToken extends OperatorToken {

	public AdditionToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof AdditionToken;
	}

	public String toString(){
		return "+";
	}

}