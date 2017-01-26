package mc.commands;

import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;
import org.fusesource.jansi.Ansi;

import java.util.Collections;
public class EvalCommand implements Command{
  private ExpressionEvaluator eval = new ExpressionEvaluator();
  @Override
  public void run(String[] args) {
    String expr = String.join(" ",args);
    try {
    System.out.println("Expression evaluated to: "+ eval.evaluateExpression(Expression.constructExpression(expr,Collections.emptyMap()), Collections.emptyMap()));
  } catch (Exception ex) {
    System.out.println(Ansi.ansi().render("@|red There was an error parsing that expression. |@"));
  }
  }
}
