package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;
@Data
@EqualsAndHashCode(callSuper = false)
public class ActionLabelNode extends ASTNode {

	private String action;

	public ActionLabelNode(String action, Location location){
		super(location);
		this.action = action;
	}
}
