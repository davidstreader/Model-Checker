package mc.util.expr;

import lombok.Setter;

public abstract class BothOperator extends Operator {

	// fields
    @Setter
	private Expression lhs;
	@Setter
    private Expression rhs;

	public BothOperator(Expression lhs, Expression rhs){
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public Expression getLeftHandSide(){
		return lhs;
	}

	public Expression getRightHandSide(){
		return rhs;
	}

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof BothOperator){
            BothOperator op = (BothOperator)obj;
            if(!lhs.equals(op.getLeftHandSide())){
                return false;
            }
            if(!rhs.equals(op.getRightHandSide())){
                return false;
            }

            return true;
        }

        return false;
    }
}
