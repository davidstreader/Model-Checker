package mc.compiler.token;

import mc.util.Location;

public class BitOrToken extends OperatorToken {

	public BitOrToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof BitOrToken;
	}

	public String toString(){
		return "|";
	}

}