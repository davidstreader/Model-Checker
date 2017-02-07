package mc.util.expr;

import lombok.Setter;

public abstract class UnaryOperator extends Operator {

    // fields
    @Setter
    private Expression rhs;

    public UnaryOperator(Expression rhs) {
        this.rhs = rhs;
    }

    public Expression getRightHandSide() {
        return rhs;
    }

    public boolean equals(Object obj){
        if(obj == this){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(obj instanceof UnaryOperator){
            UnaryOperator op = (UnaryOperator)obj;
            return rhs.equals(op.getRightHandSide());
        }

        return false;
    }
}
