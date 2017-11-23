package mc.compiler.ast;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import lombok.NonNull;
import lombok.ToString;
import mc.util.Location;


@ToString
public class IfStatementNode extends ASTNode {

    // fields
    private BoolExpr condition;
    private ASTNode trueBranch;
    private ASTNode falseBranch;
    private Context z3Context;

    /**
     * The set up structure for handling if statements
     * @param condition     The boolean expression that determines if the true branch occurs
     * @param trueBranch    The branch directly after the "then", what happens when "condition" is true
     * @param location      Where in the code this appears
     * @param z3Context     The z3 context for evaluating the boolean expression passed
     */
    public IfStatementNode(BoolExpr condition, ASTNode trueBranch, Location location, Context z3Context){
        super(location);
        this.condition = condition;
        this.trueBranch = trueBranch;
        falseBranch = null;
        this.z3Context = z3Context;
    }

    /**
     *  This function is an extension of the basic if .. then, with the addition of else.
     * @param condition     The boolean expression that determines if the true branch or false branch occurs
     * @param trueBranch    The branch directly after the "then", what happens when "condition" is true
     * @param falseBranch   The else branch, what happens if condition is false
     * @param location      Where in the code this appears
     * @param z3Context     The z3 context for evaluating the boolean expression passed
     */
    public IfStatementNode(BoolExpr condition, ASTNode trueBranch, ASTNode falseBranch, Location location, Context z3Context){
        super(location);
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
        this.z3Context = z3Context;
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
            if(z3Context.mkEq(condition,node.getCondition()).simplify().isFalse()){
                return false;
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
