package mc.compiler.token;

import mc.util.Location;

public class SetToken extends KeywordToken {

	public SetToken(Location location){
		super(location);
	}

	public String toString(){
		return "set";
	}

}