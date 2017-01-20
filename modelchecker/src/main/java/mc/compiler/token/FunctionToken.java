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

	public boolean equals(Object obj){
		if(obj == this){
			return true;
		}
		if(obj instanceof FunctionToken){
			FunctionToken token = (FunctionToken)obj;
			return function.equals(token.getFunction());
		}

		return false;
	}

	public String toString(){
		return function;
	}

}