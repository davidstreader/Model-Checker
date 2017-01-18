package mc.compiler.token;

import mc.util.Location;

public class ThenToken extends KeywordToken {

	public ThenToken(Location location){
		super(location);
	}

	public String toString(){
		return "then";
	}

}