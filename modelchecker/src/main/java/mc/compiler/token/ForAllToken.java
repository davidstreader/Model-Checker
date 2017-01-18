package mc.compiler.token;

import mc.util.Location;

public class ForAllToken extends KeywordToken {

	public ForAllToken(Location location){
		super(location);
	}

	public String toString(){
		return "forall";
	}

}