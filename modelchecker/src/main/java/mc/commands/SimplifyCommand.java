package mc.commands;

import com.microsoft.z3.Expr;
import mc.util.expr.ExpressionPrinter;
import mc.util.expr.ExpressionSimplifier;
import org.fusesource.jansi.Ansi;
import org.slf4j.LoggerFactory;

public class SimplifyCommand implements Command{
    @Override
    public void run(String[] args) {
        String expr = String.join(" ",args);
        try {
            Expr expression = ExpressionSimplifier.constructExpression(expr);
            expression = expression.simplify();
            System.out.println(Ansi.ansi().render("Expression simplified to: @|yellow " + ExpressionPrinter.printExpression(expression)+"|@"));
        } catch (Exception ex) {
            System.out.println(Ansi.ansi().render("@|red There was an error parsing that expression. |@"));
            LoggerFactory.getLogger(getClass()).error(ex.getLocalizedMessage());

        }
    }
}
