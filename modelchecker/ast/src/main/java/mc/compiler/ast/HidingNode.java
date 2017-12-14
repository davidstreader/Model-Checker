package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class HidingNode extends ASTNode {

	private String type;
	private SetNode set;

	public HidingNode(String type, SetNode set, Location location){
		super(location);
		this.type = type;
		this.set = set;
	}
}
