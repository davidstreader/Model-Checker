package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotEqualToken extends OperatorToken {

	public NotEqualToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "!=";
	}
}