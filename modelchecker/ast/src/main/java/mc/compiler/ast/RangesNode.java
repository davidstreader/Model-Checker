package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class RangesNode extends ASTNode {

	private List<IndexNode> ranges;

	public RangesNode(List<IndexNode> ranges, Location location){
		super(location);
		this.ranges = ranges;
	}
}
