package mc.compiler.token;

import mc.util.Location;

public abstract class Token {
	
	private Location location;
	
	public Token(Location location){
		this.location = location;
	}
	
	public Location getLocation(){
		return location;
	}
	
}
