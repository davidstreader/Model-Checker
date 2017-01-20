package mc.commands;

import mc.Main;
import mc.solver.JavaSMTConverter;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;

import java.util.Collections;

public class SimplifyCommand implements Command{
  private Main main;
  private JavaSMTConverter eval = new JavaSMTConverter();
  public SimplifyCommand(Main main) {
    this.main = main;
  }
  @Override
  public void run(String[] args) {
    String expr = String.join(" ",args);
    //TODO: When we have an Expression -> String function, use it here.
    System.out.println("Expression simplified to: "+ eval.simplify(Expression.constructExpression(expr)));
  }
}
