package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class RangeSeparatorToken extends SymbolToken {

	public RangeSeparatorToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "..";
	}
}