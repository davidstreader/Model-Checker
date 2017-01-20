package mc.compiler.token;

import mc.util.Location;

public class SetToken extends KeywordToken {

	public SetToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof SetToken;
	}

	public String toString(){
		return "set";
	}

}