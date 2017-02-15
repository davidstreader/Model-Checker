package mc.compiler.ast;

import mc.util.Location;

public class FunctionNode extends ASTNode {

	// fields
	private String function;
	private ASTNode process;

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
        if(obj == null){
            return false;
        }
        if(obj instanceof FunctionNode){
            FunctionNode node = (FunctionNode)obj;
            if(!function.equals(node.getFunction())){
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
