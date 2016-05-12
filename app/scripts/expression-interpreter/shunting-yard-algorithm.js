'use strict';

var OPEN_PAREN = '(';
var CLOSE_PAREN = ')';

/**
 * Processes the specified infix notation input into reverse polish notation 
 * using the shunting yard algorithm and returns the result. 
 * The precedence of operators is determined by specified precedence map.
 *
 * @param {string} input - the expression to process
 * @param {object} precedenceMap - mapping of operators to their precedence
 * @returns {string} - the input converted to reverse polish notation
 */
function processShuntingYardAlgorithm(input, precedenceMap){
	var output = '';
	var operatorStack = [];
	input = input.split(' ');
	
	// process each element in the array
	for(var i = 0; i < input.length; i++){
		var element = input[i];
		
		if(element == OPEN_PAREN){
			operatorStack.push(element);
		}
		else if(element == CLOSE_PAREN){
			processClosedParenthesis();
		}
		else if(!isOperator(element)){
			output = (output == '') ? element : output + ' ' + element;
		}
		else{
			processOperators();
		}
	}

	// push remaining operators to the output stack
	while(operatorStack.length > 0){
		output += ' ' + operatorStack.pop();
	}

	return output;

	/**
	 * Processes a closed parenthesis operation. This pushes operators
	 * from the operation stack to the final output until an open
	 * parenthesis is found.
	 */
	function processClosedParenthesis(){
		// push operators onto the output stack
		while(operatorStack.length > 0){
			var next = operatorStack.pop();	
			
			// stop if the next element is an open parenthesis
			if(next == OPEN_PAREN){
				break;
			}

			output = (output == '') ? next : output + ' ' + next;
		}
	}

	/**
	 * Before pushing the next operator to the operator stack, pushes operations
	 * from the stack onto the final output until an operator is found that has
	 * less precedence than the current operator.
	 */
	function processOperators(){
		// push operators onto the output stack
		while(operatorStack.length > 0){
			var next = operatorStack[operatorStack.length - 1];
			
			// break if the next operator has less precedence
			if(precedenceMap[element] < precedenceMap[next] || next == OPEN_PAREN){
				break;
			}

			output = (output == '') ? operatorStack.pop() : output + ' ' + operatorStack.pop();
		}

		operatorStack.push(element);
	}
}