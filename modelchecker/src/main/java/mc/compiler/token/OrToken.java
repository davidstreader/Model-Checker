package mc.compiler.token;

import mc.util.Location;

public class OrToken extends OperatorToken {

	public OrToken(Location location){
		super(location);
	}

	public String toString(){
		return "||";
	}

}