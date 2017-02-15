package mc.compiler.ast;

import mc.util.Location;

public class InterruptNode extends ASTNode {

	// fields
	private ActionLabelNode action;
	private ASTNode process;

	public InterruptNode(ActionLabelNode action, ASTNode process, Location location){
		super(location);
		this.action = action;
		this.process = process;
	}

	public ActionLabelNode getAction(){
		return action;
	}

	public void setAction(ActionLabelNode action){
		this.action = action;
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
        if(obj == null){
            return false;
        }
        if(obj instanceof InterruptNode){
            InterruptNode node = (InterruptNode)obj;
            if(!action.equals(node.getAction())){
                return false;
            }
            if(!process.equals(node.getProcess())){
                return false;
            }

            return true;
        }

        return false;
    }
}
