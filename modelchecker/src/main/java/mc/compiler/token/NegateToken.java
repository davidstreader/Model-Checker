package mc.compiler.token;

import mc.util.Location;

public class NegateToken extends OperatorToken {

	public NegateToken(Location location){
		super(location);
	}

	public String toString(){
		return "!";
	}

}