'use-strict';

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
 * Determines whether to process an expression as a boolean.
 */
var AS_BOOLEAN = true;

/**
 * Interprets the specified expression tree and returns the
 * result.
 *
 * @public
 * @param {!object} expTree - the expression tree to interpret
 * returns {!boolean|number} - overall result of expression tree
 */
function interpretExpression(expTree){
	var result = processExpression(expTree);
	return result;

	/**
	 * Processes the specified expression and returns the
	 * result
	 *
	 * @private
	 * @param {!object} expression - the expression to process
	 * @returns {!boolean|number} - result of the expression
	 */
	function processExpression(expression){
		var operator = expression.operator;

		if(operator == undefined){
			return expression;
		}
		else if(operator == OR){
			return processOrExpression(expression.operand1, expression.operand2);
		}
		else if(operator == AND){
			return processAndExpression(expression.operand1, expression.operand2);
		}
		else if(operator == BIT_OR){
			return processBitwiseOrExpression(expression.operand1, expression.operand2);
		}
		else if(operator == BIT_EXCL_OR){
			return processBitwiseExclusiveOrExpression(expression.operand1, expression.operand2);
		}
		else if(operator == BIT_AND){
			return processBitwiseAndExpression(expression.operand1, expression.operand2);
		}
		else if(operator == EQUIVALENT){
			return processEquivalentExpression(expression.operand1, expression.operand2);
		}
		else if(operator == NOT_EQUIVALENT){
			return processNotEquivalentExpression(expression.operand1, expression.operand2);
		}
		else if(operator == LESS_THAN){
			return processLessThanExpression(expression.operand1, expression.operand2);
		}
		else if(operator == LESS_THAN_OR_EQUAL){
			return processLessThanorEqualExpression(expression.operand1, expression.operand2);
		}
		else if(operator == GREATER_THAN){
			return processGreaterThanExpression(expression.operand1, expression.operand2);
		}
		else if(operator == GREATER_THAN_OR_EQUAL){
			return processGreaterThanOrEqualExpression(expression.operand1, expression.operand2);
		}
		else if(operator == RIGHT_SHIFT){
			return processRightShiftExpression(expression.operand1, expression.operand2);
		}
		else if(operator == LEFT_SHIFT){
			return processLeftShiftExpression(expression.operand1, expression.operand2);
		}
		else if(operator == ADD){
			return processAddExpression(expression.operand1, expression.operand2);
		}
		else if(operator == SUBTRACT){
			return processSubtractExpression(expression.operand1, expression.operand2);
		}
		else if(operator == MULTIPLY){
			return processMultiplyExpression(expression.operand1, expression.operand2);
		}
		else if(operator == DIVIDE){
			return processDivideExpression(expression.operand1, expression.operand2);
		}
		else if(operator == MODULO){
			return processModuloExpression(expression.operand1, expression.operand2);
		}
		else{
			var error = 'Attempting to process invalid operator \'' + operator +'\'';
			throw error;
		}
	}

	/**
	 * Process the specified operand and returns the result.
	 * Returns a boolean value if the 'asBoolean' parameter is true,
	 * otherwise returns a number value.
	 *
	 * @private
	 * @param {!object|number} operand - the operand to process
	 * @param {!boolean} asBoolean - whether operand is to be processed as boolean or not
	 */
	function processOperand(operand, asBoolean){
		var result = processExpression(operand);
		// process as boolean if specified
		if(asBoolean){
			return result != 0;
		}

		return result;
	}

	/**
	 * Processes the specified operands as an or expression and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processOrExpression(operand1, operand2){
		var operand1 = processOperand(operand1, AS_BOOLEAN);
		var operand2 = processOperand(operand2, AS_BOOLEAN);
		return (operand1 || operand2);
	}

	/**
	 * Processes the specified operands as an and expression and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processAndExpression(operand1, operand2){
		var operand1 = processOperand(operand1, AS_BOOLEAN);
		var operand2 = processOperand(operand2, AS_BOOLEAN);
		return (operand1 && operand2);
	}

	/**
	 * Processes the specified operands as a bitwise or expression and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processBitwiseOrExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 | operand2);
	}

	/**
	 * Processes the specified operands as a bitwise exclusive or expression and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processBitwiseExclusiveOrExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 ^ operand2);
	}

	/**
	 * Processes the specified operands as a bitwise and expression and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processBitwiseAndExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 & operand2);
	}

	/**
	 * Processes the specified operands, determines whether they are equivalent and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processEquivalentExpression(operand1, operand2){
		var operand1 = processOperand(operand1, AS_BOOLEAN);
		var operand2 = processOperand(operand2, AS_BOOLEAN);
		return (operand1 == operand2);
	}

	/**
	 * Processes the specified operands, determines whether they are not equivalent and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processNotEquivalentExpression(operand1, operand2){
		var operand1 = processOperand(operand1, AS_BOOLEAN);
		var operand2 = processOperand(operand2, AS_BOOLEAN);
		return (operand1 != operand2);
	}

	/**
	 * Processes the specified operands, determines whether the first operand is
	 * less than the second operator and returns the result
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processLessThanExpression(operand1, operand2){
		var operand1 = processOperand(operand1, AS_BOOLEAN);
		var operand2 = processOperand(operand2, AS_BOOLEAN);
		return (operand1 < operand2);
	}

	/**
	 * Processes the specified operands, determines whether the first operand is
	 * less than or equal to the second operator and returns the result
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processLessThanOrEqualExpression(operand1, operand2){
		var operand1 = processOperand(operand1, AS_BOOLEAN);
		var operand2 = processOperand(operand2, AS_BOOLEAN);
		return (operand1 <= operand2);
	}

	/**
	 * Processes the specified operands, determines whether the first operand is
	 * greater than the second operator and returns the result
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processGreaterThanExpression(operand1, operand2){
		var operand1 = processOperand(operand1, AS_BOOLEAN);
		var operand2 = processOperand(operand2, AS_BOOLEAN);
		return (operand1 > operand2);
	}

	/**
	 * Processes the specified operands, determines whether the first operand is
	 * greater than or equal to the second operator and returns the result
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!boolean} - the result of the expression
	 */
	function processGreaterThanOrEqualExpression(operand1, operand2){
		var operand1 = processOperand(operand1, AS_BOOLEAN);
		var operand2 = processOperand(operand2, AS_BOOLEAN);
		return (operand1 >= operand2);
	}

	/**
	 * Processes the specified operands as a right shift operation and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!number} - the result of the operation
	 */
	function processRightShiftExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 >> operand2);
	}

	/**
	 * Processes the specified operands as a left shift operation and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!number} - the result of the operation
	 */
	function processLeftShiftExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 << operand2);
	}

	/**
	 * Processes the specified operands as an add operation and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!number} - the result of the operation
	 */
	function processAddExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 + operand2);
	}

	/**
	 * Processes the specified operands as a subtract operation and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!number} - the result of the operation
	 */
	function processSubtractExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 - operand2);
	}

	/**
	 * Processes the specified operands as a multiplication operation and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!number} - the result of the operation
	 */
	function processMultiplyExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 * operand2);
	}

	/**
	 * Processes the specified operands as a division operation and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!number} - the result of the operation
	 */
	function processDivideExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 / operand2);
	}

	/**
	 * Processes the specified operands as a modulo operation and returns
	 * the result.
	 *
	 * @private
	 * @param {!object|number} operand1 - the first operand
	 * @param {!object|number} operand2 - the second operand
	 * @returns {!number} - the result of the operation
	 */
	function processModuloExpression(operand1, operand2){
		var operand1 = processOperand(operand1, !AS_BOOLEAN);
		var operand2 = processOperand(operand2, !AS_BOOLEAN);
		return (operand1 % operand2);
	}
}