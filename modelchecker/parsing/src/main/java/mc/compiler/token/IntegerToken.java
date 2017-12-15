package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntegerToken extends Token {
	
	private int integer;
	
	public IntegerToken(int integer, Location location){
		super(location);
		this.integer = integer;
	}

	@Override
	public String toString(){
		return "" + integer;
	}
}
