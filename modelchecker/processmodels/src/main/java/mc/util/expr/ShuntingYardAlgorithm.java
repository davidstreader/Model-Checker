package mc.util.expr;

import com.microsoft.z3.*;

import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import mc.exceptions.CompilationException;
import mc.util.Location;

public class ShuntingYardAlgorithm {

  private Map<String, Integer> precedenceMap;

  private Stack<String> operatorStack;
  private Stack<Expr> output;

  private int index;
  private String current;
  private Context context;

  public ShuntingYardAlgorithm(Context context) throws InterruptedException {
    this.context = context;
    setupPrecedenceMap();
    reset();
  }

  private void setupPrecedenceMap() {
    precedenceMap = new HashMap<>();
    precedenceMap.put("or", -12);
    precedenceMap.put("and", -11);
    precedenceMap.put("bitor", -10);
    precedenceMap.put("exclor", -9);
    precedenceMap.put("bitand", -8);
    precedenceMap.put("eq", -7);
    precedenceMap.put("noteq", -7);
    precedenceMap.put("lt", -6);
    precedenceMap.put("lteq", -6);
    precedenceMap.put("gt", -6);
    precedenceMap.put("gteq", -6);
    precedenceMap.put("rshift", -5);
    precedenceMap.put("lshift", -5);
    precedenceMap.put("add", -4);
    precedenceMap.put("sub", -4);
    precedenceMap.put("bitnot", -2);
    precedenceMap.put("not", -2);
    precedenceMap.put("neg", -2);
    precedenceMap.put("pos", -2);
    precedenceMap.put("mul", -3);
    precedenceMap.put("div", -3);
    precedenceMap.put("mod", -3);
    precedenceMap.put("(", -1);
    precedenceMap.put(")", -1);
  }

  private List<String> rightOperators = Arrays.asList("bitnot", "not", "neg", "pos");

  private void reset() {
    operatorStack = new Stack<>();
    output = new Stack<>();
    index = 0;
  }


  /*
     The output,  Expr ia a Z3 AST
   */
  public Expr convert(String expression, Location location) throws InterruptedException, CompilationException {
    try {
      reset();
      char[] characters = expression.toCharArray();
    //  Throwable t = new Throwable();t.printStackTrace();
  //System.out.println("convert shunting Yard "+expression);
      while (index < expression.length()) {
        String result = parse(characters);
    //System.out.println("   res = "+result+ "  current "+current);
        if (Objects.equals(result, "boolean")) {
          BoolExpr op = context.mkBool(Boolean.parseBoolean(current));
          output.push(op);
        } else if (Objects.equals(result, "integer")) {
          BitVecExpr op = Expression.mkBV(Integer.parseInt(current), context);
          output.push(op);
        } else if (Objects.equals(result, "real")) {
          FPNum op = Expression.mkNum(Double.parseDouble(current), context);
          output.push(op);
          //System.out.println("sY real Pushed "+op.getSExpr());
        } else if (Objects.equals(result, "variable")) {
          BitVecExpr op = context.mkBVConst(current, 32);
          output.push(op);
        } else if (Objects.equals(result, "operator")) {
          int precedence = precedenceMap.get(current);
          while (!operatorStack.isEmpty() && !Objects.equals(operatorStack.peek(), "(")) {
            int nextPrecedence = precedenceMap.get(operatorStack.peek());
            if (precedence <= nextPrecedence) {
              processStack(operatorStack.pop());
            } else {
              break;
            }
          }

          operatorStack.push(current);
        } else if (Objects.equals(result, "rightoperator")) {
          operatorStack.push(current);
        } else if (Objects.equals(result, "(")) {
          operatorStack.push(result);
        } else if (Objects.equals(result, ")")) {
          while (!operatorStack.isEmpty()) {
            String operator = operatorStack.pop();
            if (operator.equals("(")) {
              break;
            }
            processStack(operator);
          }
        }
        //System.out.println("sY  convert half way "+ output.peek().toString());
      }

      while (!operatorStack.isEmpty()) {
        processStack(operatorStack.pop());
      }
      //System.out.println("sY  convert output size "+output.toString());

     // if (!output.empty()) System.out.println("sY  convert output peek "+output.peek().toString());


      return output.pop();
    } catch (EmptyStackException | ClassCastException ex) {
      if (location == null) {
        throw new CompilationException(ShuntingYardAlgorithm.class, "There was an issue trying to parse the expression: " + expression);
      }
      throw new CompilationException(ShuntingYardAlgorithm.class, "There was an issue trying to parse the expression: " + expression, location);
    }
  }

  private void processStack(String operator) {
    Expr rhs = output.pop();
    Expr op;
    if (rightOperators.contains(operator)) {
      op = constructRightOperator(operator, rhs);
    } else {
      Expr lhs = output.pop();
      op = constructBothOperator(operator, lhs, rhs);
    }
    output.push(op);
  }

  private Expr constructRightOperator(String operator, Expr rhs) {
    switch (operator) {
      case "bitnot":
        return context.mkBVNot((BitVecExpr) rhs);
      case "not":
        return context.mkNot((BoolExpr) rhs);
      case "neg":
        return context.mkBVNeg((BitVecExpr) rhs);
      case "pos":
        return rhs;
    }
    return null;
  }

  private Expr constructBothOperator(String operator, Expr lhs, Expr rhs) {
    switch (operator) {
      case "or":
        return context.mkOr((BoolExpr) lhs, (BoolExpr) rhs);
      case "bitor":
        return context.mkBVOR((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "exclor":
        return context.mkXor((BoolExpr) lhs, (BoolExpr) rhs);
      case "and":
        return context.mkAnd((BoolExpr) lhs, (BoolExpr) rhs);
      case "bitand":
        return context.mkBVAND((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "eq":
        return context.mkEq(lhs, rhs);
      case "noteq":
        return context.mkNot(context.mkEq(lhs, rhs));
      case "lt":
        return context.mkBVSLT((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "lteq":
        return context.mkBVSLE((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "gt":
        return context.mkBVSGT((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "gteq":
        return context.mkBVSGE((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "lshift":
        return context.mkBVASHR((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "rshift":
        return context.mkBVSHL((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "add":
        return context.mkBVAdd((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "sub":
        return context.mkBVSub((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "mul":
        return context.mkBVMul((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "div":
        return context.mkBVSDiv((BitVecExpr) lhs, (BitVecExpr) rhs);
      case "mod":
        return context.mkBVSMod((BitVecExpr) lhs, (BitVecExpr) rhs);
    }

    return null;
  }
/*
  Sets current
 */
  private String parse(char[] expression) {
    gobbleWhitespace(expression);
    String string = new String(expression).substring(index);
 //System.out.println("sY parse "+string);
    if (string.toLowerCase().startsWith("true")) {
      current = "true";
      index += 4;
      return "boolean";
    }
    if (string.toLowerCase().startsWith("false")) {
      current = "false";
      index += 5;
      return "boolean";
    }
    if (string.toLowerCase().startsWith("$true")) {
      current = "true";
      index += 5;
      return "boolean";
    }
    if (string.toLowerCase().startsWith("$false")) {
      current = "false";
      index += 6;
      return "boolean";
    }
    if (Character.isDigit(expression[index])) {
      //System.out.println("sY exp = "+ string+ " "+ string.contains("."));
      if (string.contains(".")) {
        parseReal(expression);
        return "real";
      } else {
        parseInteger(expression);
        return "integer";
      }

    } else if (expression[index] == '$') {
      parseVariable(expression);
      return "variable";
    } else if (expression[index] == '(' || expression[index] == ')') {
      current = "" + expression[index++];
      return current;
    } else {
      parseOperator(expression);
      if (rightOperators.contains(current)) {
        return "rightoperator";
      }
      return "operator";
    }
  }

  private void parseInteger(char[] expression) {
    StringBuilder builder = new StringBuilder();
    while (index < expression.length && Character.isDigit(expression[index])) {
      builder.append(expression[index++]);
    }
    current = builder.toString();
  }
  private void parseReal(char[] expression) {
    StringBuilder builder = new StringBuilder();

    while (index < expression.length && (Character.isDigit(expression[index]) || (expression[index] == '.'))) {
      builder.append(expression[index++]);
    }

    current = builder.toString();
  }

  private void parseVariable(char[] expression) {
    StringBuilder builder = new StringBuilder();
    builder.append("$");
    index++;

    char next = expression[index];
    while (index < expression.length && (Character.isAlphabetic(next) || Character.isDigit(next) || next == '_')) {
      builder.append(expression[index++]);
      if (index == expression.length) {
        break;
      }
      next = expression[index];
    }
    current = builder.toString();
  }

  private void parseOperator(char[] expression) {
    if (expression[index] == '|') {
      if (index + 1 < expression.length && expression[index + 1] == '|') {
        current = "or";
        index += 2;
      } else {
        current = "bitor";
        index++;
      }
    } else if (expression[index] == '&') {
      if (index + 1 < expression.length && expression[index + 1] == '&') {
        current = "and";
        index += 2;
      } else {
        current = "bitand";
        index++;
      }
    } else if (expression[index] == '^') {
      current = "exclor";
      index++;
    } else if (index + 1 < expression.length && expression[index] == '=' && expression[index + 1] == '=') {
      current = "eq";
      index += 2;
    } else if (expression[index] == '!' && index + 1 < expression.length && expression[index + 1] == '=') {
      current = "noteq";
      index += 2;
    } else if (expression[index] == '<') {
      if (index + 1 < expression.length) {
        if (expression[index + 1] == '=') {
          current = "lteq";
          index += 2;
          return;
        } else if (expression[index + 1] == '<') {
          current = "lshift";
          index += 2;
          return;
        }
      }
      current = "lt";
      index++;

    } else if (expression[index] == '>') {
      if (index + 1 < expression.length) {
        if (expression[index + 1] == '=') {
          current = "gteq";
          index += 2;
          return;
        } else if (expression[index + 1] == '>') {
          current = "rshift";
          index += 2;
          return;
        }
      }
      current = "gt";
      index++;
    } else if (expression[index] == '+') {
      if (precedenceMap.containsKey(current) || index == 0) {
        current = "pos";
      } else {
        current = "add";
      }
      index++;
    } else if (expression[index] == '-') {
      if (precedenceMap.containsKey(current) || index == 0) {
        current = "neg";
      } else {
        current = "sub";
      }
      index++;
    } else if (expression[index] == '*') {
      current = "mul";
      index++;
    } else if (expression[index] == '/') {
      current = "div";
      index++;
    } else if (expression[index] == '%') {
      current = "mod";
      index++;
    } else if (expression[index] == '!') {
      current = "not";
      index++;
    } else if (expression[index] == '~') {
      current = "bitnot";
      index++;
    }
  }

  private void gobbleWhitespace(char[] expression) {
    char next = expression[index];
    while (next == ' ' || next == '\t' || next == '\n' || next == '\r') {
      next = expression[++index];
    }
  }
}
