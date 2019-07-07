package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class FunctionToken extends Token {

	private final String function;

	public FunctionToken(String function, Location location){
		super(location);
		this.function = function;
	}

	@Override
	public String toString(){
		return function;
	}
}