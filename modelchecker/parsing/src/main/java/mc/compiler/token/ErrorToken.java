package mc.compiler.token;

import mc.util.Location;

public class ErrorToken extends TerminalToken {

	public ErrorToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof ErrorToken;
	}

	public String toString(){
		return "ERROR";
	}

}