package mc.compiler.token;

import mc.util.Location;

public class RangeToken extends KeywordToken {

	public RangeToken(Location location){
		super(location);
	}

	public String toString(){
		return "range";
	}

}