package mc.compiler.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.compiler.EquationSettings;
import mc.util.Location;

@Data
@EqualsAndHashCode(callSuper = true)
public class OperationNode extends ASTNode {

	private String operation;
	private boolean negated;
	private ASTNode firstProcess;
	private ASTNode secondProcess;
	private EquationSettings equationSettings;

	public OperationNode(String operation, boolean isNegated, ASTNode firstProcess, ASTNode secondProcess, Location location, EquationSettings equationSettings){
		super(location);
		this.operation = operation;
		this.negated = isNegated;
		this.firstProcess = firstProcess;
		this.secondProcess = secondProcess;
		this.equationSettings = equationSettings;
	}
}
