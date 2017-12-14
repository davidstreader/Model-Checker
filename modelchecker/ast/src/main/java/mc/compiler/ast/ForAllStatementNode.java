package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class ForAllStatementNode extends ASTNode {

	private RangesNode ranges;
	private ASTNode process;

	public ForAllStatementNode(RangesNode ranges, ASTNode process, Location location){
		super(location);
		this.ranges = ranges;
		this.process = process;
	}
}
