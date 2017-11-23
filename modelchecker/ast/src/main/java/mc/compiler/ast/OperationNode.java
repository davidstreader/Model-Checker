package mc.compiler.ast;

import lombok.Getter;
import mc.util.Location;

import static mc.compiler.EquationEvaluator.EquationSettings;

public class OperationNode extends ASTNode {

	// fields
	private String operation;
	private boolean isNegated;
	private ASTNode firstProcess;
	private ASTNode secondProcess;
	@Getter
	private EquationSettings equationSettings;

	public OperationNode(String operation, boolean isNegated, ASTNode firstProcess, ASTNode secondProcess, Location location, EquationSettings equationSettings){
		super(location);
		this.operation = operation;
		this.isNegated = isNegated;
		this.firstProcess = firstProcess;
		this.secondProcess = secondProcess;
		this.equationSettings = equationSettings;
	}

	public String getOperation(){
		return operation;
	}

	public boolean isNegated(){
		return isNegated;
	}

	public ASTNode getFirstProcess(){
		return firstProcess;
	}

    public void setFirstProcess(ASTNode firstProcess){
        this.firstProcess = firstProcess;
    }

	public ASTNode getSecondProcess(){
		return secondProcess;
	}

    public void setSecondProcess(ASTNode secondProcess){
        this.secondProcess = secondProcess;
    }

    public boolean equals(Object obj){
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj instanceof OperationNode){
            OperationNode node = (OperationNode)obj;
            if(!operation.equals(node.getOperation())){
                return false;
            }
            if(isNegated != node.isNegated()){
                return false;
            }
            if(!firstProcess.equals(node.getFirstProcess())){
                return false;
            }
            return secondProcess.equals(node.getSecondProcess());
        }

        return false;
    }
}
