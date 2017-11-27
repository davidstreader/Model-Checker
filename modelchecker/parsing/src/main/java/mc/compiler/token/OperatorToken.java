package mc.compiler.token;

import mc.util.Location;

public abstract class OperatorToken extends Token {
	
	public OperatorToken(Location location){
		super(location);
	}
}
