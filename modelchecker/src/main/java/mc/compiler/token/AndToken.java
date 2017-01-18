package mc.compiler.token;

import mc.util.Location;

public class AndToken extends OperatorToken {

	public AndToken(Location location){
		super(location);
	}

	public String toString(){
		return "&&";
	}

}