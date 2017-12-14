package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class LocalProcessNode extends ASTNode {

	private String identifier;
	private RangesNode ranges;
	private ASTNode process;

	public LocalProcessNode(String identifier, RangesNode ranges, ASTNode process, Location location){
		super(location);
		this.identifier = identifier;
		this.ranges = ranges;
		this.process = process;
	}
}
