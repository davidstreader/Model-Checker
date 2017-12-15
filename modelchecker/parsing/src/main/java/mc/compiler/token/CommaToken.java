package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommaToken extends SymbolToken {

	public CommaToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return ",";
	}
}