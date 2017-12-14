package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class RelabelNode extends ASTNode {

	private List<RelabelElementNode> relabels;

	public RelabelNode(List<RelabelElementNode> relabels, Location location){
		super(location);
		this.relabels = relabels;
	}
}
