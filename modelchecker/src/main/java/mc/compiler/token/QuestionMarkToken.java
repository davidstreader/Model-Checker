package mc.compiler.token;

import mc.util.Location;

public class QuestionMarkToken extends SymbolToken {

	public QuestionMarkToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof QuestionMarkToken;
	}

	public String toString(){
		return "?";
	}

}