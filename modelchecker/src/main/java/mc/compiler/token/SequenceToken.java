package mc.compiler.token;

import mc.util.Location;

public class SequenceToken extends SymbolToken {

	public SequenceToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof SequenceToken;
	}

	public String toString(){
		return "->";
	}

}