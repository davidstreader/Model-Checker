package mc.compiler.token;

import mc.util.Location;

public class ConstToken extends KeywordToken {

	public ConstToken(Location location){
		super(location);
	}

	public String toString(){
		return "const";
	}

}