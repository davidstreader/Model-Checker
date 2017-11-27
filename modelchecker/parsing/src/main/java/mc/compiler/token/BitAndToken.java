package mc.compiler.token;

import mc.util.Location;

public class BitAndToken extends OperatorToken {

	public BitAndToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof BitAndToken;
	}

	public String toString(){
		return "&";
	}

}