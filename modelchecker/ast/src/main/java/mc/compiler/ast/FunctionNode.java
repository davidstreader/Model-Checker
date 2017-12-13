package mc.compiler.ast;

import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.Expr;
import lombok.Getter;
import lombok.Setter;
import mc.util.Location;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class FunctionNode extends ASTNode {

	// fields
    @Getter
	private String function;
    @Getter
    @Setter
	private List<ASTNode> processes;

    @Getter
    @Setter
    private ImmutableSet<String> flags;

    @Getter
    @Setter
    private boolean prune;

    public boolean needsPruning() {
        return prune;
    }

    @Getter
    @Setter
    private Map<String,Expr> replacements;

	public FunctionNode(String function, List<ASTNode> processes, Location location){
		super(location);
		this.function = function;
		this.processes = processes;
	}

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj instanceof FunctionNode){
            FunctionNode node = (FunctionNode)obj;
            if(!function.equals(node.getFunction())){
                return false;
            }
            return processes.equals(node.getProcesses());
        }

        return false;
    }

}
