package mc.compiler.token;

import mc.util.Location;

public class WhenToken extends KeywordToken {

	public WhenToken(Location location){
		super(location);
	}

	public String toString(){
		return "when";
	}

}