package net.modelsolver;
import java.util.*;

public class ShuntingYard {
  public static void main(String[] args) {
    System.out.println(postfix("sin(max(2)/3*3.1415)"));
  }
  private enum Operator
  {
    ADD(1), SUBTRACT(2), MULTIPLY(3), DIVIDE(4), TEST(5);
    final int precedence;
    Operator(int p) { precedence = p; }
  }

  private static Map<String, Operator> ops = new HashMap<String, Operator>() {{
    put("+", Operator.ADD);
    put("-", Operator.SUBTRACT);
    put("*", Operator.MULTIPLY);
    put("/", Operator.DIVIDE);
    put("\\", Operator.DIVIDE);
    put(">", Operator.TEST);
    put("<", Operator.TEST);
    put(">=", Operator.TEST);
    put("<=", Operator.TEST);
    put("==", Operator.TEST);
    put("&&", Operator.TEST);
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
    String combined = "(?"+String.join("|",ops.keySet());
    combined = combined.replace("\\","\\\\");
    combined = combined.replace("/","\\/");
    combined += "|\\(|\\)"+")";
    combined = combined.replace("*","\\*");
    combined = combined.replace("+","\\+");
    combined = combined+"|"+combined.replace("?","?<");
    for (String token : infix.split(combined)) {
      // operator
      if (ops.containsKey(token)) {
        while (!stack.isEmpty() && isHigerPrec(token, stack.peek()))
          output.append(stack.pop()).append(' ');
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
