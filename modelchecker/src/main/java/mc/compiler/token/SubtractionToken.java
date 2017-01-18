package mc.compiler.token;

import mc.util.Location;

public class SubtractionToken extends OperatorToken {

	public SubtractionToken(Location location){
		super(location);
	}

	public String toString(){
		return "-";
	}

}