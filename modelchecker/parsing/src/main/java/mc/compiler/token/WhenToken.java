package mc.compiler.token;

import mc.util.Location;

public class WhenToken extends KeywordToken {

	public WhenToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof WhenToken;
	}

	public String toString(){
		return "when";
	}

}