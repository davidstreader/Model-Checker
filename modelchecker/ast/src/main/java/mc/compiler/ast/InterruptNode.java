package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class InterruptNode extends ASTNode {

	private ActionLabelNode action;
	private ASTNode process;

	public InterruptNode(ActionLabelNode action, ASTNode process, Location location){
		super(location);
		this.action = action;
		this.process = process;
	}
}
