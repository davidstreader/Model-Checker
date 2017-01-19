package mc.solver;

import mc.util.expr.Expression;

/**
 * This class allows you to solve guard information via an SMTSolver.
 */
public class EdgeUtils {
  public static void main(String[] args) {
    JavaSMTConverter converter = new JavaSMTConverter();
    System.out.println((1 + 1 == 2) && (2 == 2));
    System.out.println(converter.convert(Expression.constructExpression("($i+1==2)&&($i==1)")));
  }
}
