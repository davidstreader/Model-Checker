package mc.compiler.token;

import mc.util.Location;

public class StopToken extends TerminalToken {

	public StopToken(Location location){
		super(location);
	}

	public String toString(){
		return "STOP";
	}

}