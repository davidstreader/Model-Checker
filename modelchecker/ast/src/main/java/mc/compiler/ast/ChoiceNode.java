package mc.compiler.ast;

import lombok.ToString;
import mc.util.Location;
@ToString
public class ChoiceNode extends ASTNode {

	// fields
	private ASTNode firstProcess;
	private ASTNode secondProcess;

	public ChoiceNode(ASTNode firstProcess, ASTNode secondProcess, Location location){
		super(location);
		this.firstProcess = firstProcess;
		this.secondProcess = secondProcess;
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
        if(obj instanceof ChoiceNode){
            ChoiceNode node = (ChoiceNode)obj;
            if(!firstProcess.equals(node.getFirstProcess())){
                return false;
            }
            return secondProcess.equals(node.getSecondProcess());
        }

        return false;
    }
}
