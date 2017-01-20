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

	public boolean equals(Object obj){
		if(obj == this){
			return true;
		}
		if(obj instanceof ActionToken){
			ActionToken token = (ActionToken)obj;
			return action.equals(token.getAction());
		}

		return false;
	}

	public String toString(){
		return action;
	}

}