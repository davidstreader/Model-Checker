package mc.solver;

import lombok.AllArgsConstructor;
import org.sosy_lab.common.Appender;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Utility methods for Infix2Postfix and Postfix2Infix
 */
class OperatorUtils {
  //A list of operators with precedence mappings.
  @AllArgsConstructor
  private enum Operator
  {
    //AND has lowest precedence as it should always be done last.
    //COMPARISON is next, as it relies on everything else to be done
    AND(0),COMPARISON(1),ADD(2), SUBTRACT(3), MULTIPLY(4), DIVIDE(5);
    final int precedence;
  }

  /**
   * A list of operations
   */
  private static Map<String, Operator> ops = new HashMap<String, Operator>() {{
    put("+", Operator.ADD);
    put("-", Operator.SUBTRACT);
    put("*", Operator.MULTIPLY);
    put("/", Operator.DIVIDE);
    put("\\", Operator.DIVIDE);
    put(">", Operator.COMPARISON);
    put("<", Operator.COMPARISON);
    put(">=", Operator.COMPARISON);
    put("<=", Operator.COMPARISON);
    put("==", Operator.COMPARISON);
    put("&&", Operator.AND);
  }};

  /**
   * Get a single regexp that can match all operators
   */
  private static String getCombined() {
    String combined = "(?="+String.join("|",ops.keySet());
    combined = combined.replace("\\","\\\\");
    combined = combined.replace("/","\\/");
    combined += "|\\(|\\)"+")";
    combined = combined.replace("*","\\*");
    combined = combined.replace("+","\\+");
    combined = combined+"|"+combined.replace("?=","?<=");
    return combined;
  }
  /**
   * Check if one operator has a higher precedence than another
   * @param op1 the first operator
   * @param op2 the second operator
   * @return sub >= op
   */
  private static boolean isHigerPrec(String op1, String op2)
  {
    return (ops.containsKey(op2) && ops.get(op2).precedence >= ops.get(op1).precedence);
  }
  /**
   * A ShuntingYard algorithm that can process entire guards.
   */
  static String infixToPostfix(String infix)
  {
    infix = infix.replaceAll("\\s","");
    StringBuilder output = new StringBuilder();
    Stack<String> stack  = new Stack<>();
    String combined = getCombined();
    for (String token : infix.split(combined)) {
      //check if we have an operator
      if (OperatorUtils.ops.containsKey(token)) {
        while (!stack.isEmpty() && isHigerPrec(token, stack.peek())) {
          output.append(stack.pop()).append(' ');
        }
        stack.push(token);

        // left parenthesis
      } else if (token.equals("(")) {
        stack.push(token);

        // right parenthesis
      } else if (token.equals(")")) {
        while (!stack.peek().equals("(")) {
          output.append(stack.pop()).append(' ');
        }
        stack.pop();
        // digit
      } else {
        output.append(token).append(' ');
      }
    }

    while (!stack.isEmpty())
      output.append(stack.pop()).append(' ');
    return output.toString();
  }
  /**
   * Convert an appender from MathSMT into a infix expression for display
   * @param appender The appender
   * @return an expression
   */
  static String postfixToInfix(Appender appender) {
    //The appender has a bunch of information about variables that we dont actually care about.
    //So we skip all but the last line.
    String[] res = appender.toString().split("\\r?\\n|\\r");
    //Get the last line
    String tmp = res[res.length-1];
    //Remove the assert from the result
    tmp = tmp.replace("(assert ","");
    boolean isAnd = tmp.contains("and");
    //Remove the and function from the result, we can and everything later.
    tmp = tmp.replace("(and ","");
    //The assert and and functions leave extra brackets that need to be stripped.
    tmp = tmp.substring(0,tmp.length()-(isAnd?2:1));
    //MathSMT returns = instead of == for comparisons.
    tmp =tmp.replace("=","==");
    //Add a tmp symbol around brackets so we can split around them later.
    tmp = tmp.replaceAll("[(]","$0~").replaceAll("[)]","~$0");
    Stack<String> stack = new Stack<>();
    //Loop over all tokens (between space and tmp symbol)
    for (String token: tmp.split("[\\s]+|~")) {
      //When we find a closing bracket, we know that all required args are in the stack.
      if (token.equals(")")) {
        //Read the first expression
        String ex0 = stack.pop();
        //Second
        String ex1 = stack.pop();
        //Operator
        String op = stack.pop();
        //The related open bracket
        stack.pop();
        //Push the constructed expression to the stack
        stack.push("("+ex1+op+ex0+")");
      } else {
        //Just push if we haven't encountered a )
        stack.push(token);
      }
    }
    //At the end of this, we end up with a collection of products to and together
    String joined = "";
    for (String s : stack) {
      //the nice thing about this is that we know the open and close brackets are extra, so we can remove them.
      joined += "&&"+s.substring(1,s.length()-1);
    }
    //Remove the extra && from the beginning.
    joined = joined.substring(2);
    return joined;
  }
}
