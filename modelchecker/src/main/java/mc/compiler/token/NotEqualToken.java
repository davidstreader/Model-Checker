package mc.compiler.token;

import mc.util.Location;

public class NotEqualToken extends OperatorToken {

	public NotEqualToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof NotEqualToken;
	}

	public String toString(){
		return "!=";
	}

}