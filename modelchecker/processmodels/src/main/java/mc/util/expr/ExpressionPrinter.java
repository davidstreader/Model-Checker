package mc.util.expr;

import com.microsoft.z3.BitVecNum;
import com.microsoft.z3.Expr;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public class ExpressionPrinter {
    public static String printExpression(Expr expression, Map<String, Integer> variableMap){
        return print(expression, variableMap);
    }

    private static String print(Expr expression, Map<String, Integer> variableMap){
        if (expression.isTrue()) {
            return "true";
        }
        if (expression.isFalse()) {
            return "false";
        }
        if(expression instanceof BitVecNum){
            return expression.toString();
        }
        if(expression.isConst()){
            String c = expression.toString();
            return variableMap.containsKey(c)?variableMap.get(c)+"":c;
        }
        String lhs = print(expression.getArgs()[0],variableMap);
        if(expression.isNot()){
            if (expression.getArgs()[0].isEq()){
                return "("+lhs+"!="+print(expression.getArgs()[1],variableMap)+")";
            }
            return "("+"!"+lhs+")";
        }
        if(expression.isBVNOT()){
            return "("+"~"+lhs+")";
        }

        if(expression.isOr()){
            return "("+ Arrays.stream(expression.getArgs()).map(ExpressionPrinter::printExpression).collect(Collectors.joining("||")) +")";
        }
        if(expression.isAnd()){
            return "("+ Arrays.stream(expression.getArgs()).map(ExpressionPrinter::printExpression).collect(Collectors.joining("&&")) +")";
        }
        String rhs = print(expression.getArgs()[1],variableMap);
        if(expression.isBVAdd()){
            return "("+lhs +"+"+ rhs+")";
        }
        if(expression.isBVSub()){
            return "("+lhs +"-"+ rhs+")";
        }
        if(expression.isBVMul()){
            return "("+lhs +"*"+ rhs+")";
        }
        if(expression.isBVMul()){
            return "("+lhs +"/"+ rhs+")";
        }
        if(expression.isBVSMod()){
            return "("+lhs +"%"+ rhs+")";
        }
        if(expression.isBVShiftLeft()){
            return "("+lhs +"<<"+ rhs+")";
        }
        if(expression.isBVShiftRightArithmetic()){
            return "("+lhs +">>"+ rhs+")";
        }
        if(expression.isBVOR()){
            return "("+lhs +"|"+ rhs+")";
        }
        if(expression.isXor()){
            return "("+lhs +"^"+ rhs+")";
        }
        if(expression.isBVAND()){
            return "("+lhs+"&"+rhs+")";
        }
        if(expression.isEq()){
            return "("+lhs+"=="+rhs+")";
        }
        if(expression.isBVSLT()){
            return "("+lhs+"<"+rhs+")";
        }
        if(expression.isBVSLE()){
            return "("+lhs+"<="+rhs+")";
        }
        if(expression.isBVSGT()){
            return "("+lhs+">"+rhs+")";
        }
        if(expression.isBVSGE()){
            return "("+lhs+">="+rhs+")";
        }
        System.out.println(expression);
        throw new IllegalArgumentException("");
    }



    public static String printExpression(Expr expression) {
        return printExpression(expression, Collections.emptyMap());
    }
}
