package net.modelsolver;
import lombok.AllArgsConstructor;

import java.util.*;

/**
 * A ShuntingYard algorithm that can process entire guards.
 */
class ShuntingYard {
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

  private static boolean isHigerPrec(String op, String sub)
  {
    return (ops.containsKey(sub) && ops.get(sub).precedence >= ops.get(op).precedence);
  }

  public static String postfix(String infix)
  {
    infix = infix.replaceAll("\\s","");
    StringBuilder output = new StringBuilder();
    Stack<String> stack  = new Stack<>();
    String combined = "(?="+String.join("|",ops.keySet());
    combined = combined.replace("\\","\\\\");
    combined = combined.replace("/","\\/");
    combined += "|\\(|\\)"+")";
    combined = combined.replace("*","\\*");
    combined = combined.replace("+","\\+");
    combined = combined+"|"+combined.replace("?=","?<=");
    for (String token : infix.split(combined)) {
      // operator
      if (ops.containsKey(token)) {
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

    while ( ! stack.isEmpty())
      output.append(stack.pop()).append(' ');
    return output.toString();
  }

}
