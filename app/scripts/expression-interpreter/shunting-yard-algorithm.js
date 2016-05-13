'use strict';

/**
 * Processes the specified infix notation input into reverse polish notation 
 * using the shunting yard algorithm and returns the result. 
 * The precedence of operators is determined by specified precedence map.
 *
 * @param {token[]} tokens - the expression to process
 * @param {object} precedenceMap - mapping of operators to their precedence
 * @returns {string} - the input converted to reverse polish notation
 */
function processShuntingYardAlgorithm(tokens, precedenceMap){
	var output = [];
	var operatorStack = [];
	
	// process each element in the array
	for(var i = 0; i < tokens.length; i++){
		
		if(tokens[i].value === '('){
			operatorStack.push(tokens[i]);
		}
		else if(tokens[i].value === ')'){
			processClosedParenthesis();
		}
		else if(tokens[i].type !== 'operator'){
			output.push(tokens[i]);
		}
		else{
			processOperators();
		}
	}

	// push remaining operators to the output stack
	while(operatorStack.length > 0){
		output.push(operatorStack.pop());
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
			if(next.value === '('){
				break;
			}

			output.push(next);
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
			if(precedenceMap[tokens[i]] < precedenceMap[next] || next.value === '('){
				break;
			}

			output.push(operatorStack.pop());
		}

		operatorStack.push(tokens[i]);
	}
}