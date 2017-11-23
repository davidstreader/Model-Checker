package mc.compiler.token;

import mc.util.Location;

public class ElseToken extends KeywordToken {

	public ElseToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof ElseToken;
	}

	public String toString(){
		return "else";
	}

}