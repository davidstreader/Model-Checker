package mc.commands;

import lombok.AllArgsConstructor;
import mc.Main;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;

import java.util.Collections;
public class EvalCommand implements Command{
  private Main main;
  private ExpressionEvaluator eval = new ExpressionEvaluator();
  public EvalCommand(Main main) {
    this.main = main;
  }
  @Override
  public void run(String[] args) {
    String expr = String.join(" ",args);
    System.out.println("Expression evaluated to: "+ eval.evaluateExpression(Expression.constructExpression(expr), Collections.emptyMap()));
  }
}
