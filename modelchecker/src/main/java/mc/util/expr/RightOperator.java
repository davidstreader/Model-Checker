package mc.util.expr;

import lombok.Setter;

public abstract class RightOperator extends Operator {

  // fields
    @Setter
  private Expression rhs;

  public RightOperator(Expression rhs){
    this.rhs = rhs;
  }

  public Expression getRightHandSide(){
    return rhs;
  }
}
