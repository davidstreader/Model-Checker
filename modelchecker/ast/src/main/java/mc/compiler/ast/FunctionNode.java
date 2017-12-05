package mc.compiler.ast;

import com.microsoft.z3.Expr;
import lombok.Getter;
import lombok.Setter;
import mc.util.Location;

import java.util.Map;

public class FunctionNode extends ASTNode {

	// fields
	private String function;
	private ASTNode process;

    @Getter
    @Setter
    private boolean fair = true;

    private boolean prune;

    public boolean needsPruning() {
        return prune;
    }

    public void setPruning(boolean prune) {
        this.prune = prune;
    }

    @Getter
    @Setter
    private Map<String,Expr> replacements;

	public FunctionNode(String function, ASTNode process, Location location){
		super(location);
		this.function = function;
		this.process = process;
	}

	public String getFunction(){
		return function;
	}

	public ASTNode getProcess(){
		return process;
	}

	public void setProcess(ASTNode process){
		this.process = process;
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
            return process.equals(node.getProcess());
        }

        return false;
    }

}
