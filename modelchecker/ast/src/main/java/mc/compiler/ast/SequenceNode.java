package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class SequenceNode extends ASTNode {

	private ActionLabelNode from;
	private ASTNode to;

	public SequenceNode(ActionLabelNode from, ASTNode to, Location location){
		super(location);
		this.from = from;
		this.to = to;
	}
}
