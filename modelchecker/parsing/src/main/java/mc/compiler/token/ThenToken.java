package mc.compiler.token;

import mc.util.Location;

public class ThenToken extends KeywordToken {

	public ThenToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof ThenToken;
	}

	public String toString(){
		return "then";
	}

}