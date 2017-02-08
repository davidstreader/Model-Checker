package mc.compiler.ast;

import mc.util.Location;
import mc.util.expr.Expression;

public class IfStatementNode extends ASTNode {

	// fields
	private Expression condition;
	private ASTNode trueBranch;
	private ASTNode falseBranch;

	public IfStatementNode(Expression condition, ASTNode trueBranch, Location location){
		super(location);
		this.condition = condition;
		this.trueBranch = trueBranch;
		falseBranch = null;
	}

	public IfStatementNode(Expression condition, ASTNode trueBranch, ASTNode falseBranch, Location location){
		super(location);
		this.condition = condition;
		this.trueBranch = trueBranch;
		this.falseBranch = falseBranch;
	}

	public Expression getCondition(){
		return condition;
	}

	public ASTNode getTrueBranch(){
		return trueBranch;
	}

	public ASTNode getFalseBranch(){
		return falseBranch;
	}

	public boolean hasFalseBranch(){ return falseBranch != null; }

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof IfStatementNode){
            IfStatementNode node = (IfStatementNode)obj;
            if(!condition.equals(node.getCondition())){
                return false;
            }
            if(!trueBranch.equals(node.getTrueBranch())){
                return false;
            }
            if(falseBranch == null && node.hasFalseBranch() || falseBranch != null && !node.hasFalseBranch()){
                return false;
            }
            if(falseBranch != null && !falseBranch.equals(node.getFalseBranch())){
                return false;
            }

            return true;
        }

        return false;
    }
}
