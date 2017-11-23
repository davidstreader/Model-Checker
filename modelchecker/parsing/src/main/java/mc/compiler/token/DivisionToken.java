package mc.compiler.token;

import mc.util.Location;

public class DivisionToken extends OperatorToken {

	public DivisionToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof DivisionToken;
	}

	public String toString(){
		return "/";
	}

}