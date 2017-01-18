package mc.compiler.token;

import mc.util.Location;

public abstract class TerminalToken extends Token {
	
	public TerminalToken(Location location){
		super(location);
	}
	
}
