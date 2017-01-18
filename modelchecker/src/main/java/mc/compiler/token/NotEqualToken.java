package mc.compiler.token;

import mc.util.Location;

public class NotEqualToken extends OperatorToken {

	public NotEqualToken(Location location){
		super(location);
	}

	public String toString(){
		return "!=";
	}

}