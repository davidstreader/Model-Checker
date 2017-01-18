package mc.compiler.token;

import mc.util.Location;

public class AdditionToken extends OperatorToken {

	public AdditionToken(Location location){
		super(location);
	}

	public String toString(){
		return "+";
	}

}