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

/**
 * Proceesses the given in put as reverse polish notation, returning the
 * result of the process.
 *
 * @param {string} input - the expression to process
 * @param {object} variableMap - map of variables
 * @param {boolean} asBoolean - determines whether result is returned as a boolean or not
 * @returns {number|boolean} - result of process
 */
function processReversePolishNotation(input, variableMap, asBoolean){
	// check if 'asBoolean' has been defined
	asBoolean = (asBoolean == undefined) ? false : asBoolean;

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
			var operand2 = stack.pop();
			var operand1 = stack.pop();
			var result = processOperation(current, operand1, operand2);
			stack.push(result);
		}
		// check if current element is either a number or a variable
		else{
			// check if current is a number
			var value = parseInt(current);
			if(isNaN(value)){
				value = processReversePolishNotation(variableMap[current], variableMap);
				if(value == undefined){
					throw new ExpressionInterpreterException('Trying to process invalid variable name \'' + current + '\'.');
				}
			}

			stack.push(value);
		}
	}

	// if there are still elements to process on stack then throw error
	if(stack.length > 1){
		throw new ExpressionInterpreterException('Invalid operation processed.');	
	}

	// return result
	return (asBoolean) ? stack[0] != 0 : stack[0];

	/**
	 * Determines whether the specified input is an operator. Returns
	 * true if it is an operator, otherwise returns false.
	 * 
	 * @param {string} input - string to check
	 * @param {boolean} - true if input is operator, otherwise false
	 */
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

	/**
	 * Processes the two specified operands by the operation defined by the operator
	 * and returns the result. Throws an error if the operator given is invalid.
	 *
	 * @oaram {string} operator - the operation to perfrom
	 * @param {number} operand1 - the first operand
	 * @param {number} operand2 - the second operand
	 * @returns {number} - the result of the operation
	 */
	function processOperation(operator, operand1, operand2){
		if(operand1 == undefined || operand2 == undefined){
			throw new ExpressionInterpreterException('Not enough elements on the stack to process expression.');
		}

		switch(operator){
			case OR:
				return ((operand1 != 0) || (operand2 != 0)) ? 1 : 0;
			case AND:
				return ((operand1 != 0) && (operand2 != 0)) ? 1 : 0;
			case BIT_OR:
				return (operand1 | operand2);
			case BIT_EXCL_OR:
				return (operand1 ^ operand2);
			case BIT_AND:
				return (operand1 & operand2);
			case EQUIVALENT:
				return (operand1 == operand2) ? 1 : 0;
			case NOT_EQUIVALENT:
				return (operand1 != operand2) ? 1 : 0;
			case LESS_THAN:
				return (operand1 < operand2) ? 1 : 0;
			case LESS_THAN_OR_EQUAL:
				return (operand1 <= operand2) ? 1 : 0;
			case GREATER_THAN:
				return (operand1 > operand2) ? 1 : 0;
			case GREATER_THAN_OR_EQUAL:
				return (operand1 >= operand2) ? 1 : 0;
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

/**
 * Constructs and returns an 'ExpressionInterpreterException' message based
 * off of the specified message.
 *
 * @param {string} message - the message to be constructed 
 */
	function ExpressionInterpreterException(message){
		this.message = message;
		this.toString = function(){
			return ('ExpressionInterpreterException: ' + message);
		};
	}
}