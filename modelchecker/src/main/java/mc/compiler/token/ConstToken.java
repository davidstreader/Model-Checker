package mc.compiler.token;

import mc.util.Location;

public class ConstToken extends KeywordToken {

	public ConstToken(Location location){
		super(location);
	}

	public boolean equals(Object obj){
		return obj instanceof ConstToken;
	}

	public String toString(){
		return "const";
	}

}