package mc.compiler.token;

import mc.util.Location;

public class ForAllToken extends KeywordToken {

	public ForAllToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof ForAllToken;
	}

	public String toString(){
		return "forall";
	}

}