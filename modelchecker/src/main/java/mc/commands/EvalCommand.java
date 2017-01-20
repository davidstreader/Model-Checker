package mc.commands;

import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;

import java.util.Collections;
public class EvalCommand implements Command{
  private ExpressionEvaluator eval = new ExpressionEvaluator();
  @Override
  public void run(String[] args) {
    String expr = String.join(" ",args);
    System.out.println("Expression evaluated to: "+ eval.evaluateExpression(Expression.constructExpression(expr), Collections.emptyMap()));
  }
}
