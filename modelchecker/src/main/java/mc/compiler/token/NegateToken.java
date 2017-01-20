package mc.compiler.token;

import mc.util.Location;

public class NegateToken extends OperatorToken {

	public NegateToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof NegateToken;
	}

	public String toString(){
		return "!";
	}

}