package mc.compiler.token;

import mc.util.Location;

public class SequenceToken extends SymbolToken {

	public SequenceToken(Location location){
		super(location);
	}

	public String toString(){
		return "->";
	}

}