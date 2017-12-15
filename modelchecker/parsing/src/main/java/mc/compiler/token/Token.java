package mc.compiler.token;

import lombok.Data;
import mc.util.Location;

@Data
public abstract class Token {

	private Location location;
	
	public Token(Location location){
		this.location = location;
	}
}
