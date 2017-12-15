package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class OperatorToken extends Token {
	
	public OperatorToken(Location location){
		super(location);
	}
}
