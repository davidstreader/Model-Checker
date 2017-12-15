package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class SymbolToken extends Token {
	
	public SymbolToken(Location location){
		super(location);
	}
}
