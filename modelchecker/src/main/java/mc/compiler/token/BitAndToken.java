package mc.compiler.token;

import mc.util.Location;

public class BitAndToken extends OperatorToken {

	public BitAndToken(Location location){
		super(location);
	}

	public String toString(){
		return "&";
	}

}