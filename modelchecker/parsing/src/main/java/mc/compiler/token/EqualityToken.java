package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class EqualityToken extends OperatorToken {

	public EqualityToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "==";
	}
}