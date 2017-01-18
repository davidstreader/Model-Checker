package mc.compiler.token;

import mc.util.Location;

public class DivisionToken extends OperatorToken {

	public DivisionToken(Location location){
		super(location);
	}

	public String toString(){
		return "/";
	}

}