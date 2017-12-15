package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class DotToken extends SymbolToken {

	public DotToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return ".";
	}
}