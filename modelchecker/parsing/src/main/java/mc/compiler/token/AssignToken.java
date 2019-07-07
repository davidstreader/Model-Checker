package mc.compiler.token;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.processmodels.ProcessType;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class AssignToken extends SymbolToken {
    ProcessType pType = ProcessType.PETRINET;

    public AssignToken(Location location){
		super(location);
	}

	@Override
	public String toString(){
		return "=";
	}
}