package mc.util.expr;

public class BitNotOperator extends RightOperator {

  public BitNotOperator(Expression rhs){
    super(rhs);
  }

  public int evaluate(){
    int rhs = getRightHandSide().evaluate();
    return ~rhs;
  }

}
