package mc.commands;

import mc.solver.ExpressionSimplifier;
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
            Expression expression = Expression.constructExpression(expr);
            expression = ExpressionSimplifier.simplify(expression, Collections.emptyMap());
            System.out.println(Ansi.ansi().render("Expression simplified to: @|yellow " + printer.printExpression(expression)+"|@"));
        } catch (Exception ex) {
            System.out.println(Ansi.ansi().render("@|red There was an error parsing that expression. |@"));
            System.out.println("Error Message: "+ex.getLocalizedMessage());
        }
    }
}
