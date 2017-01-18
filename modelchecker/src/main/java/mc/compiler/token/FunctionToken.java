package mc.compiler.token;

import mc.util.Location;

public class FunctionToken extends Token {

	private String function;

	public FunctionToken(String function, Location location){
		super(location);
		this.function = function;
	}

	public String getFunction(){
		return function;
	}

	public String toString(){
		return function;
	}

}