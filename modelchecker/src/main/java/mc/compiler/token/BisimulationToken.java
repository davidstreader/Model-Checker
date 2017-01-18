package mc.compiler.token;

import mc.util.Location;

public class BisimulationToken extends OperationToken {
	
	public BisimulationToken(Location location){
		super(location);
	}
	
	public String toString(){
		return "~";
	}
	
}
