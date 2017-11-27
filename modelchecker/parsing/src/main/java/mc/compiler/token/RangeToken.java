package mc.compiler.token;

import mc.util.Location;

public class RangeToken extends KeywordToken {

	public RangeToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof RangeToken;
	}

	public String toString(){
		return "range";
	}

}