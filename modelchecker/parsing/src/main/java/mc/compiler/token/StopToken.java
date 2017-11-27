package mc.compiler.token;

import mc.util.Location;

public class StopToken extends TerminalToken {

	public StopToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof StopToken;
	}

	public String toString(){
		return "STOP";
	}

}