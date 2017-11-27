package mc.compiler.token;

import mc.util.Location;

public class AndToken extends OperatorToken {

	public AndToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof AndToken;
	}

	public String toString(){
		return "&&";
	}

}