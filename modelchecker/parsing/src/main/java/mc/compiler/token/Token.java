package mc.compiler.token;

import lombok.Getter;
import mc.util.Location;

public abstract class Token {

	@Getter
	private Location location;
	
	public Token(Location location){
		this.location = location;
	}

}
