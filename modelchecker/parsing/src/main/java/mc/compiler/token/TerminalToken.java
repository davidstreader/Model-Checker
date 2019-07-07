package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class TerminalToken extends Token {

	public TerminalToken(Location location){
		super(location);
	}

}
