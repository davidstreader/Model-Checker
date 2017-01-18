package mc.compiler.token;

import mc.util.Location;

public class ErrorToken extends TerminalToken {

	public ErrorToken(Location location){
		super(location);
	}

	public String toString(){
		return "ERROR";
	}

}