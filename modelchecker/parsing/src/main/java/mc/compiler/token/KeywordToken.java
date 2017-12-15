package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class KeywordToken extends Token {
	
	public KeywordToken(Location location){
		super(location);
	}
}
