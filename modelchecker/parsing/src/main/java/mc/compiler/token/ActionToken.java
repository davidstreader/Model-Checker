package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActionToken extends Token {

	private String action;

	public ActionToken(String action, Location location){
		super(location);
		this.action = action;
	}

	@Override
	public String toString(){
		return action;
	}
}