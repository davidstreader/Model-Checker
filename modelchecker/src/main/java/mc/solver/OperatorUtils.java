package mc.solver;

import lombok.AllArgsConstructor;
import org.sosy_lab.common.Appender;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
  private static Map<Pattern, Operator> ops = new HashMap<Pattern, Operator>() {{
    put(Pattern.compile("\\+"), Operator.ADD);
    put(Pattern.compile("-"), Operator.SUBTRACT);
    put(Pattern.compile("\\*"), Operator.MULTIPLY);
    put(Pattern.compile("\\/"), Operator.DIVIDE);
    put(Pattern.compile("\\\\"), Operator.DIVIDE);
    //Make sure not to match the > on its own if follwed by a =
    put(Pattern.compile(">(?!=)"), Operator.COMPARISON);
    put(Pattern.compile("<(?!=)"), Operator.COMPARISON);
    put(Pattern.compile(">="), Operator.COMPARISON);
    put(Pattern.compile("<="), Operator.COMPARISON);
    put(Pattern.compile("=="), Operator.COMPARISON);
    put(Pattern.compile("!="), Operator.COMPARISON);
    put(Pattern.compile("&&"), Operator.AND);
  }};

  /**
   * Get a single regexp that can match all operators
   */
  private static String getCombined() {
    //Map all the operators to their string equivalents, then join them into a giant regexp
    //Add brackets to the regex.
    return "("+ops.keySet().stream().map(Pattern::pattern).collect(Collectors.joining("|"))+"|\\(|\\))";
  }
  /**
   * Check if one operator has a higher precedence than another
   * @param op1 the first operator
   * @param op2 the second operator
   * @return sub >= op
   */
  private static boolean isHigerPrec(Pattern op1, Pattern op2)
  {
    return (op2 != null && ops.get(op2).precedence >= ops.get(op1).precedence);
  }
  private static Pattern getOp(String token) {
    return ops.keySet().stream().filter(pattern -> pattern.matcher(token).matches()).findAny().orElse(null);
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
    for (String token : infix.replaceAll(combined,"~$1~").split("~")) {
      if (Objects.equals(token.replace("\\s+",""), "")) continue;
      Pattern op = getOp(token);
      //check if we have an operator
      if (getOp(token) != null) {
        Pattern op2 = getOp(stack.peek());
        while (!stack.isEmpty() && isHigerPrec(op, op2)) {
          output.append(stack.pop()).append(' ');
          op2 = getOp(stack.peek());
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
    //when we hit a not, get rid of its surrounding brackets
    int idx = tmp.indexOf("(not");
    while (idx > -1) {
      tmp = tmp.substring(0,idx)+"!"+tmp.substring(idx+5);
      int brCount = 0;
      for (int i = idx; true; i++) {
        if (tmp.charAt(i) == '(') {
          brCount++;
        }
        if (tmp.charAt(i) == ')') {
          if (brCount == 0) {
            tmp = tmp.substring(0,i)+tmp.substring(i+1);
            break;
          }
          brCount--;
        }
      }
      idx = tmp.indexOf("(not");
    }
    //The assert and and functions leave extra brackets that need to be stripped.
    tmp = tmp.substring(0,tmp.length()-(isAnd?2:1));
    //MathSMT returns = instead of == for comparisons.
    tmp =tmp.replaceAll("\\b=\\b","==");
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
        //The last stack.pop() here is the related open bracket. However, ! symbols will stick to it.
        //Push the constructed expression to the stack
        stack.push(stack.pop()+ex1+op+ex0+")");
      } else {
        //Just push if we haven't encountered a )
        stack.push(token);
      }
    }

    //At the end of this, we end up with a collection of products to and together
    String joined = "";
    for (String s : stack) {
      if (s.startsWith("("))
        //the nice thing about this is that we know the open and close brackets are extra, so we can remove them.
        joined += "&&"+s.substring(1,s.length()-1);
      else
        joined += "&&"+s;
    }
    //Remove the extra && from the beginning.
    joined = joined.substring(2);
    joined = joined.replaceAll("\\b=\\b","==");
    //Look for !(a==b) and swap it for (a!=b)
    Pattern replaceNegate = Pattern.compile("!\\(([^()]*?)==([^()]*?)\\)");
    Matcher matcher = replaceNegate.matcher(joined);
    while (matcher.find()) {
      joined = joined.replace(matcher.group(0),matcher.group(1)+"!="+matcher.group(2));
    }
    return joined;
  }
}
