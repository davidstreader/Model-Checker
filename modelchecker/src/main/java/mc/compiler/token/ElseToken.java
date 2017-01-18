package mc.compiler.token;

import mc.util.Location;

public class ElseToken extends KeywordToken {

	public ElseToken(Location location){
		super(location);
	}

	public String toString(){
		return "else";
	}

}