package mc.compiler.ast;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.util.Location;
@ToString
@Getter
@Setter
@EqualsAndHashCode(callSuper=true)
public class CompositeNode extends ASTNode {

	// fields
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
