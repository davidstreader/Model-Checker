package mc.compiler.token;

import mc.util.Location;

public class IntegerToken extends Token {
	
	private int integer;
	
	public IntegerToken(int integer, Location location){
		super(location);
		this.integer = integer;
	}
	
	public int getInteger(){
		return integer;
	}

	public boolean equals(Object obj){
		if(obj == this){
			return true;
		}
		if(obj instanceof IntegerToken){
			IntegerToken token = (IntegerToken)obj;
			return integer == token.getInteger();
		}

		return false;
	}

	public String toString(){
		return "" + integer;
	}
	
}
