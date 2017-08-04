package mc.commands;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.microsoft.z3.Expr;
import mc.exceptions.CompilationException;
import mc.util.expr.ExpressionEvaluator;
import mc.util.expr.Expression;
import org.fusesource.jansi.Ansi;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class EvalCommand implements Command{
    private ExpressionEvaluator eval = new ExpressionEvaluator();
    @Override
    public void run(String[] args) {
        try {
            Expr expression;
            Map<String,Integer> vars = Collections.emptyMap();
            try {
                expression = Expression.constructExpression(String.join(" ",args));
            } catch (Exception ex) {
                if (args.length > 1) {
                    expression = Expression.constructExpression(String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1)));
                    vars = new Gson().fromJson(
                        "{" + args[args.length - 1] + "}".replace("=", ":"), new TypeToken<Map<String, Integer>>() {
                        }.getType()
                    );
                } else {
                    throw ex;

                }
            }
            System.out.println(Ansi.ansi().render("Expression evaluated to: @|yellow "+ eval.evaluateExpression(expression,vars)+"|@"));
        } catch (Exception ex) {
            if (ex instanceof JsonSyntaxException) {
                System.out.println(Ansi.ansi().render("@|red The variable map that was provided is invalid. |@"));
            } else {
                System.out.println(Ansi.ansi().render("@|red There was an error parsing that expression. |@"));
                if (ex instanceof CompilationException) {
                    LoggerFactory.getLogger(((CompilationException) ex).getClazz()).error(ex.getMessage());
                } else {
                    LoggerFactory.getLogger(EvalCommand.class).error(ex.getMessage());
                }
            }
        }
    }
}
