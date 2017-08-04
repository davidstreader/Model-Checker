package mc.compiler.ast;

import com.microsoft.z3.BoolExpr;
import lombok.ToString;
import mc.util.Location;

import static mc.util.expr.Expression.getContext;

@ToString
public class IfStatementNode extends ASTNode {

    // fields
    private BoolExpr condition;
    private ASTNode trueBranch;
    private ASTNode falseBranch;

    public IfStatementNode(BoolExpr condition, ASTNode trueBranch, Location location){
        super(location);
        this.condition = condition;
        this.trueBranch = trueBranch;
        falseBranch = null;
    }

    public IfStatementNode(BoolExpr condition, ASTNode trueBranch, ASTNode falseBranch, Location location){
        super(location);
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public BoolExpr getCondition(){
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
        boolean result = super.equals(obj);
        if(!result){
            return false;
        }
        if(obj == this){
            return true;
        }
        if(obj instanceof IfStatementNode){
            IfStatementNode node = (IfStatementNode)obj;
            try {
                if(getContext().mkEq(condition,node.getCondition()).simplify().isFalse()){
                    return false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(!trueBranch.equals(node.getTrueBranch())){
                return false;
            }
            if(falseBranch == null && node.hasFalseBranch() || falseBranch != null && !node.hasFalseBranch()){
                return false;
            }
            return falseBranch == null || falseBranch.equals(node.getFalseBranch());
        }

        return false;
    }
}
