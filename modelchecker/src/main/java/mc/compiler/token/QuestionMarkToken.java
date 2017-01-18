package mc.compiler.token;

import mc.util.Location;

public class QuestionMarkToken extends SymbolToken {

	public QuestionMarkToken(Location location){
		super(location);
	}

	public String toString(){
		return "?";
	}

}