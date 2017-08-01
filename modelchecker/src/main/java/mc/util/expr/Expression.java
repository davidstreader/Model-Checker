package mc.util.expr;

import lombok.SneakyThrows;
import mc.exceptions.CompilationException;
import org.apache.xalan.xsltc.compiler.CompilerException;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Expression implements Serializable {

    public static Expression constructExpression(String expression, Map<String,String> variableMap){
        Pattern regex = Pattern.compile("(\\$v.+\\b)");
        Matcher matcher = regex.matcher(expression);
        while (matcher.find()) {
            expression = expression.replace(matcher.group(0),variableMap.get(matcher.group(0)));
            matcher = regex.matcher(expression);
        }
        ShuntingYardAlgorithm sya = new ShuntingYardAlgorithm();
        return sya.convert(expression);
    }

    public static Expression constructExpression(String s) {
        return constructExpression(s, Collections.emptyMap());
    }
    @Override
    public String toString() {
        return new ExpressionPrinter().printExpression(this);
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Expression)) return false;
        try {
            Expression simple =  ExpressionSimplifier.simplify(new EqualityOperator(this, (Expression) o),Collections.emptyMap());
            if (simple instanceof BooleanOperand && ((BooleanOperand) simple).getValue()) return true;
        } catch (CompilationException ignored) {

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
    @SneakyThrows
    public Expression copy() {
        return cloneExpr(this);
    }
    private Expression cloneExpr(Expression expr) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, CompilerException {
        if (expr instanceof BinaryOperator) {
            return cloneExpr((BinaryOperator)expr);
        }
        if (expr instanceof UnaryOperator) {
            return cloneExpr((UnaryOperator)expr);
        }
        if (expr instanceof VariableOperand) {
            return new VariableOperand(((VariableOperand) expr).getValue());
        }
        if (expr instanceof IntegerOperand) {
            return new IntegerOperand(((IntegerOperand) expr).getValue());
        }
        throw new CompilerException("Unable to clone: "+expr);
    }
    private BinaryOperator cloneExpr(BinaryOperator orig) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, CompilerException {
        return orig.getClass().getConstructor(Expression.class, Expression.class).newInstance(cloneExpr(orig.getLeftHandSide()),cloneExpr(orig.getRightHandSide()));
    }
    private UnaryOperator cloneExpr(UnaryOperator orig) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, CompilerException {
        return orig.getClass().getConstructor(Expression.class).newInstance(cloneExpr(orig.getRightHandSide()));
    }
    HashMap<Map<String,Integer>,Boolean> solveable = new HashMap<>();
    HashMap<Map<String,Integer>,Expression> simplified = new HashMap<>();
    public void clearTmp() {
        simplified.clear();
        solveable.clear();
    }
}
