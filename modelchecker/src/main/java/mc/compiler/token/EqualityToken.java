package mc.compiler.token;

import mc.util.Location;

public class EqualityToken extends OperatorToken {

	public EqualityToken(Location location){
		super(location);
	}

	public String toString(){
		return "==";
	}

}