package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class OperationTypeToken extends Token {

	public OperationTypeToken(Location location) {
		super(location);
	}
}