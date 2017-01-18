package mc.compiler.token;

import mc.util.Location;

public class ActionToken extends Token {

	private String action;

	public ActionToken(String action, Location location){
		super(location);
		this.action = action;
	}

	public String getAction(){
		return action;
	}

	public String toString(){
		return action;
	}

}