package mc.compiler.token;

import mc.util.Location;

public class BisimulationToken extends OperationToken {
	
	public BisimulationToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof BisimulationToken;
	}

	public String toString(){
		return "~";
	}
	
}
