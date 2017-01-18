package mc.util.expr;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class ShuntingYardAlgorithm {

	private Map<String, Integer> precedenceMap;

	private Stack<String> operatorStack;
	private Stack<Expression> output;

	private int index;
	private String current;

	public ShuntingYardAlgorithm(){
		setupPrecedenceMap();
		reset();
	}

	private void setupPrecedenceMap(){
		precedenceMap = new HashMap<String, Integer>();
		precedenceMap.put("or", 10);
		precedenceMap.put("and", 9);
		precedenceMap.put("bitor", 8);
		precedenceMap.put("exclor", 7);
		precedenceMap.put("bitand", 6);
		precedenceMap.put("eq", 5);
		precedenceMap.put("noteq", 5);
		precedenceMap.put("lt", 4);
		precedenceMap.put("lteq", 4);
		precedenceMap.put("gt", 4);
		precedenceMap.put("gteq", 4);
		precedenceMap.put("rshift", 3);
		precedenceMap.put("lshift", 3);
		precedenceMap.put("add", 2);
		precedenceMap.put("sub", 2);
		precedenceMap.put("mul", 1);
		precedenceMap.put("div", 1);
		precedenceMap.put("mod", 1);
		precedenceMap.put("(", 0);
		precedenceMap.put(")", 0);
	}

	private void reset(){
		operatorStack = new Stack<String>();
		output = new Stack<Expression>();
		index = 0;
	}

	public Expression convert(String expression){
		reset();
		char[] characters = expression.toCharArray();

		while(index < expression.length()){
			String result = parse(characters);
			if(result == "integer"){
				IntegerOperand op = new IntegerOperand(Integer.parseInt(current));
				output.push(op);
			}
			else if(result == "variable"){
				VariableOperand op = new VariableOperand(current);
				output.push(op);
			}
			else if(result == "operator"){
				int precedence = precedenceMap.get(current);
				while(!operatorStack.isEmpty()){
					int nextPrecedence = precedenceMap.get(operatorStack.peek());
					if(precedence <= nextPrecedence){
						String operator = operatorStack.pop();
						Expression rhs = output.pop();
						Expression lhs = output.pop();
						Operator op = constructOperator(operator, lhs, rhs);
						output.push(op);
					}
				}

				operatorStack.push(current);
			}
			else if(result == "("){
				operatorStack.push(result);
			}
			else if(result == ")"){
				while(!operatorStack.isEmpty()){
					String operator = operatorStack.pop();
					if(operator.equals("(")){
						break;
					}

					Expression rhs = output.pop();
					Expression lhs = output.pop();
					Operator op = constructOperator(operator, lhs, rhs);
					output.push(op);
				}
			}
		}

		while(!operatorStack.isEmpty()){
			String operator = operatorStack.pop();
			Expression rhs = output.pop();
			Expression lhs = output.pop();
			Operator op = constructOperator(operator, lhs, rhs);
			output.push(op);
		}

		return output.pop();
	}

	private Operator constructOperator(String operator, Expression lhs, Expression rhs){
		switch(operator){
		case "or":
			return new OrOperator(lhs, rhs);
		case "bitor":
			return new BitOrOperator(lhs, rhs);
		case "exclor":
			return new ExclOrOperator(lhs, rhs);
		case "and":
			return new AndOperator(lhs, rhs);
		case "bitand":
			return new BitAndOperator(lhs, rhs);
		case "eq":
			return new EqualityOperator(lhs, rhs);
		case "noteq":
			return new NotEqualOperator(lhs, rhs);
		case "lt":
			return new LessThanOperator(lhs, rhs);
		case "lteq":
			return new LessThanEqOperator(lhs, rhs);
		case "gt":
			return new GreaterThanOperator(lhs, rhs);
		case "gteq":
			return new GreaterThanEqOperator(lhs, rhs);
		case "lshift":
			return new LeftShiftOperator(lhs, rhs);
		case "rshift":
			return new RightShiftOperator(lhs, rhs);
		case "add":
			return new AdditionOperator(lhs, rhs);
		case "sub":
			return new SubtractionOperator(lhs, rhs);
		case "mul":
			return new MultiplicationOperator(lhs, rhs);
		case "div":
			return new DivisionOperator(lhs, rhs);
		case "mod":
			return new ModuloOperator(lhs, rhs);
		}

		return null;
	}

	private String parse(char[] expression){
		gobbleWhitespace(expression);

		if(Character.isDigit(expression[index])){
			parseInteger(expression);
			return "integer";
		}
		else if(expression[index] == '$'){
			parseVariable(expression);
			return "variable";
		}
		else if(expression[index] == '(' || expression[index] == ')'){
			current = "" + expression[index++];
			return current;
		}
		else{
			parseOperator(expression);
			return "operator";
		}
	}

	private void parseInteger(char[] expression){
		StringBuilder builder = new StringBuilder();
		while(index < expression.length && Character.isDigit(expression[index])){
			builder.append(expression[index++]);
		}

		current = builder.toString();
	}

	private void parseVariable(char[] expression){
		StringBuilder builder = new StringBuilder();
		builder.append("$");
		index++;

		char next = expression[index];
		while(index < expression.length && (Character.isAlphabetic(next) || Character.isDigit(next) || next == '_')){
			builder.append(expression[index++]);
			if(index == expression.length){
				break;
			}
			next = expression[index];
		}
		current = builder.toString();
	}

	private void parseOperator(char[] expression){
		if(expression[index] == '|'){
			if(expression[index + 1] == '|'){
				current = "or";
				index += 2;
			}
			else{
				current = "bitor";
				index++;
			}
		}
		else if(expression[index] == '&'){
			if(expression[index + 1] == '&'){
				current = "and";
				index += 2;
			}
			else{
				current = "bitand";
				index++;
			}
		}
		else if(expression[index] == '^'){
			current = "exclor";
			index++;
		}
		else if(expression[index] == '=' && expression[index + 1] == '='){
			current = "eq";
			index += 2;
		}
		else if(expression[index] == '!' && expression[index + 1] == '='){
			current = "noteq";
			index += 2;
		}
		else if(expression[index] == '<'){
			if(expression[index + 1] == '='){
				current = "lteq";
				index += 2;
			}
			else if(expression[index + 1] == '<'){
				current = "lshift";
				index += 2;
			}
			else{
				current = "lt";
				index++;
			}
		}
		else if(expression[index] == '>'){
			if(expression[index + 1] == '='){
				current = "gteq";
				index += 2;
			}
			else if(expression[index + 1] == '>'){
				current = "rshift";
				index += 2;
			}
			else{
				current = "gt";
				index++;
			}
		}
		else if(expression[index] == '+'){
			current = "add";
			index++;
		}
		else if(expression[index] == '-'){
			current = "sub";
			index++;
		}
		else if(expression[index] == '*'){
			current = "mul";
			index++;
		}
		else if(expression[index] == '/'){
			current = "div";
			index++;
		}
		else if(expression[index] == '%'){
			current = "mod";
			index++;
		}
	}

	private void gobbleWhitespace(char[] expression){
		char next = expression[index];
		while(next == ' ' || next == '\t' || next == '\n' || next == '\r'){
			next = expression[index++];
		}
	}
}