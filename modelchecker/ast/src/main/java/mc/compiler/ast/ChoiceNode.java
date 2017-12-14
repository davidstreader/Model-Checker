package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChoiceNode extends ASTNode {

	private ASTNode firstProcess;
	private ASTNode secondProcess;

	public ChoiceNode(ASTNode firstProcess, ASTNode secondProcess, Location location){
		super(location);
		this.firstProcess = firstProcess;
		this.secondProcess = secondProcess;
	}
}
