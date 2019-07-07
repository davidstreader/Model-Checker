package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConstToken extends KeywordToken {

	public ConstToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "const";
	}
}