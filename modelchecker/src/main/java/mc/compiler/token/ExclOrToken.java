package mc.compiler.token;

import mc.util.Location;

public class ExclOrToken extends OperatorToken {

	public ExclOrToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof ExclOrToken;
	}

	public String toString(){
		return "^";
	}

}