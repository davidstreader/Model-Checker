package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class SubtractionToken extends OperatorToken {

	public SubtractionToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "-";
	}
}