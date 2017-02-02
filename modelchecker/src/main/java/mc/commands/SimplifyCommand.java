package mc.commands;

import mc.solver.ExpressionSolver;
import mc.util.expr.Expression;
import mc.util.expr.ExpressionPrinter;
import org.fusesource.jansi.Ansi;

import java.util.Collections;

public class SimplifyCommand implements Command{
  private ExpressionPrinter printer = new ExpressionPrinter();
  @Override
  public void run(String[] args) {
    String expr = String.join(" ",args);
    try {
      System.out.println("Expression simplified to: " + printer.printExpression(ExpressionSolver.simplify(Expression.constructExpression(expr,Collections.emptyMap())),Collections.emptyMap()));
    } catch (Exception ex) {
      System.out.println(Ansi.ansi().render("@|red There was an error parsing that expression. |@"));
      ex.printStackTrace();
    }
  }
}
