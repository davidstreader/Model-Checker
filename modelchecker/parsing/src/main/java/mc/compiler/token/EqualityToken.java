package mc.compiler.token;

import mc.util.Location;

public class EqualityToken extends OperatorToken {

	public EqualityToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof EqualityToken;
	}

	public String toString(){
		return "==";
	}

}