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
	
	public String toString(){
		return "" + integer;
	}
	
}
