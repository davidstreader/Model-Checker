'use strict';

// operator types
var OR = '||';
var AND = '&&';
var BIT_OR = '|';
var BIT_EXCL_OR = '^';
var BIT_AND = '&';
var EQUIVALENT = '==';
var NOT_EQUIVALENT = '!=';
var LESS_THAN = '<';
var LESS_THAN_OR_EQUAL = '<=';
var GREATER_THAN = '>';
var GREATER_THAN_OR_EQUAL = '>=';
var RIGHT_SHIFT = '>>';
var LEFT_SHIFT = '<<';
var ADD = '+';
var SUBTRACT = '-';
var MULTIPLY = '*';
var DIVIDE = '/';
var MODULO = '%';

function processReversePolishNotation(input, variableMap){
	// check if input is a number
	if(typeof(input) == 'number'){
		return input;
	}

	input = input.split(' ');
	var stack = [];
	
	for(var i = 0; i < input.length; i++){
		var current = input[i];
		// check if the current element is an operator
		if(isOperator(current)){
			var result = processOperation(current, stack.pop(), stack.pop());
			stack.push(result);
		}
		// check if current element is a number
		else if(typeof(current) == 'number'){
			stack.push(current);
		}
		// check if current element is a variable
		else{
			var variable = variableMap[current];
			if(variable == undefined){
				throw new ExpressionInterpreterException('Trying to process invalid variable name \'' + current + '\'.');
			}

			stack.push(variable);
		}
	}

	if(stack.length > 1){
		throw new ExpressionInterpreterException('Invalid operation processed.');	
	}

	return stack[0];

	function isOperator(input){
		switch(input){
			case OR:
			case AND:
			case BIT_OR:
			case BIT_EXCL_OR:
			case BIT_AND:
			case EQUIVALENT:
			case NOT_EQUIVALENT:
			case LESS_THAN:
			case LESS_THAN_OR_EQUAL:
			case GREATER_THAN:
			case GREATER_THAN_OR_EQUAL:
			case RIGHT_SHIFT:
			case LEFT_SHIFT:
			case ADD:
			case SUBTRACT:
			case MULTIPLY:
			case DIVIDE:
			case MODULO:
				return true;
			default:
				return false;
		}
	}

	function processOperation(operator, operand1, operand2){
		if(operand1 == undefined || operand2 == undefined){
			throw new ExpressionInterpreterException('Not enough elements on the stack to process expression.');
		}

		switch(operator){
			case OR:
				return ((operand1 != 0) || (operand2 != 0));
			case AND:
				return ((operand1 != 0) && (operand2 != 0));
			case BIT_OR:
				return (operand1 | operand2);
			case BIT_EXCL_OR:
				return (operand1 ^ operand2);
			case BIT_AND:
				return (operand1 & operand2);
			case EQUIVALENT:
				return (operand1 == operand2);
			case NOT_EQUIVALENT:
				return (operand1 != operand2);
			case LESS_THAN:
				return (operand1 < operand2);
			case LESS_THAN_OR_EQUAL:
				return (operand1 <= operand2);
			case GREATER_THAN:
				return (operand1 > operand2);
			case GREATER_THAN_OR_EQUAL:
				return (operand1 >= operand2);
			case RIGHT_SHIFT:
				return (operand1 >> operand2);
			case LEFT_SHIFT:
				return (operand1 << operand2);
			case ADD:
				return (operand1 + operand2);
			case SUBTRACT:
				return (operand1 - operand2);
			case MULTIPLY:
				return (operand1 * operand2);
			case DIVIDE:
				return (operand1 / operand2);
			case MODULO:
				return (operand1 % operand2);
				return true;
			default:
				throw new ExpressionInterpreterException('Invalid operator \'' + operator + '\' used.');
		}
	}

	function ExpressionInterpreterException(message){
		this.message = message;
		this.toString = function(){
			return ('ExpressionInterpreterException: ' + message);
		};
	}
}