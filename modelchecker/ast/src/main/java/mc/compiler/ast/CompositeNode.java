package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper=true)
public class CompositeNode extends ASTNode {

	private ASTNode firstProcess;
	private ASTNode secondProcess;
	private String  operation;

	public CompositeNode(String operation, ASTNode firstProcess, ASTNode secondProcess, Location location){
		super(location);
		this.operation     = operation;
		this.firstProcess  = firstProcess;
		this.secondProcess = secondProcess;
	}
}