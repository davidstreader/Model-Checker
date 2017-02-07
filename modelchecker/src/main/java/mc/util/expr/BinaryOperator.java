package mc.util.expr;

import lombok.Setter;

public abstract class BinaryOperator extends Operator {

	// fields
    @Setter
	private Expression lhs;
	@Setter
    private Expression rhs;

	public BinaryOperator(Expression lhs, Expression rhs){
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
        if(obj instanceof BinaryOperator){
            BinaryOperator op = (BinaryOperator)obj;
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
