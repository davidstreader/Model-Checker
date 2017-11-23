package mc.compiler.token;

import mc.util.Location;

public class BisimulationTypeToken extends OperationTypeToken {
	
	public BisimulationTypeToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof BisimulationTypeToken;
	}

	public String toString(){
		return "~";
	}
	
}
