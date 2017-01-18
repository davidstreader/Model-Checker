package mc.compiler.token;

import mc.util.Location;

public class ExclOrToken extends OperatorToken {

	public ExclOrToken(Location location){
		super(location);
	}

	public String toString(){
		return "^";
	}

}