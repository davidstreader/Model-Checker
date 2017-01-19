package mc.util.expr;

public abstract class RightOperator extends Operator {

  // fields
  private Expression rhs;

  public RightOperator(Expression rhs){
    this.rhs = rhs;
  }

  public Expression getRightHandSide(){
    return rhs;
  }
}
