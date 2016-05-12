'use strict';

/**
 * Proceesses the given in put as reverse polish notation, returning the
 * result of the process.
 *
 * @param {string} input - the expression to process
 * @returns {number} - result of process
 */
function evaluateReversePolishNotation(input){
	var input = input.split(' ');
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
			stack.push(value);
		}
	}

	// if there are still elements to process on stack then throw error
	if(stack.length > 1){
		throw new ExpressionInterpreterException('Invalid operation processed.');	
	}

	// return result
	return stack[0];

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
			default:
				throw new ExpressionInterpreterException('Invalid operator \'' + operator + '\' used.');
		}
	}
}