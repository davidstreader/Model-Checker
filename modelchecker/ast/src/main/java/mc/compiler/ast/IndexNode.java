package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class IndexNode extends ASTNode {

	private String variable;
	private ASTNode range;
	private ASTNode process;

	public IndexNode(String variable, ASTNode range, ASTNode process, Location location){
		super(location);
		this.variable = variable;
		this.range = range;
        this.process = process;
	}
}
