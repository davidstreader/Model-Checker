package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

/**
 * Identifier token is any variable starting with an uppercase letter.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IdentifierToken extends Token {

	private final String identifier;

	public IdentifierToken(String identifier, Location location){
		super(location);
		this.identifier = identifier;
	}

	@Override
	public String toString(){
		return identifier;
	}
}