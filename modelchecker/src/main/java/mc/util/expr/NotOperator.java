package mc.util.expr;

/**
 * Created by sanjay on 20/01/2017.
 */
public class NotOperator extends RightOperator {
  public NotOperator(Expression rhs) {
    super(rhs);
  }

  @Override
  public int evaluate() {
    int rhs = getRightHandSide().evaluate();
    boolean ret = rhs != 0;
    if (ret) return 1;
    return 0;
  }
}
