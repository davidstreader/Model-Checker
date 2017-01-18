package mc.compiler.token;

import mc.util.Location;

public class BitOrToken extends OperatorToken {

	public BitOrToken(Location location){
		super(location);
	}

	public String toString(){
		return "|";
	}

}