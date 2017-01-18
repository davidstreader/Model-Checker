package mc.compiler.token;

import mc.util.Location;

public class IfToken extends KeywordToken {

	public IfToken(Location location){
		super(location);
	}

	public String toString(){
		return "if";
	}

}