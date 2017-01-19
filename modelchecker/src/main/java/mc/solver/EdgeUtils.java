package mc.solver;

import mc.util.expr.Expression;

/**
 * This class allows you to solve guard information via an SMTSolver.
 */
public class EdgeUtils {
  public static void main(String[] args) {
    JavaSMTConverter converter = new JavaSMTConverter();
    Expression e = converter.simplify(Expression.constructExpression("!(1==1)||(1==1)"));
  }
}
