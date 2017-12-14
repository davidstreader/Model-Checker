package mc.compiler.ast;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Expr;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mc.util.Location;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"processes","flags","prune","replacements"})
public class FunctionNode extends ASTNode {

	private String function;
	private List<ASTNode> processes;
    private ImmutableSet<String> flags;
    private boolean prune;
    private Map<String,Expr> replacements;

	public FunctionNode(String function, List<ASTNode> processes, Location location){
		super(location);
		this.function = function;
		this.processes = processes;
	}
}
