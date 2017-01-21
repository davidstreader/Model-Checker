package mc.commands;

import mc.Main;
import mc.solver.JavaSMTConverter;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionEvaluator;
import org.fusesource.jansi.Ansi;

import java.util.Collections;

public class SimplifyCommand implements Command{
  private JavaSMTConverter eval = new JavaSMTConverter();
  @Override
  public void run(String[] args) {
    String expr = String.join(" ",args);
    //TODO: When we have an Expression -> String function, use it here.
    try {
      System.out.println("Expression simplified to: " + eval.simplify(Expression.constructExpression(expr)));
    } catch (Exception ex) {
      System.out.println(Ansi.ansi().render("@|red There was an error parsing that expression. |@"));
    }
  }
}
