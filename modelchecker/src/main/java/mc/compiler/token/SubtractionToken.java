package mc.compiler.token;

import mc.util.Location;

public class SubtractionToken extends OperatorToken {

	public SubtractionToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof SubtractionToken;
	}

	public String toString(){
		return "-";
	}

}