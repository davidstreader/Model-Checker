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

// precedence map
var precedenceMap = constructPrecedenceMap();

// regex for finding variables in a given input
var regex = '[\$][v<]*[a-zA-Z0-9]*[>]*';

/**
 * Interprets the specified input and returns the result.
 *
 * @param {string} input - the expression to interpret
 * @param {object} variableMap - mapping of variables to their values
 * @param {boolean} asBoolean - determines whether a boolean result is returned.
 */
function interpretExpression(input, variableMap, asBoolean){
	// check if 'asBoolean' has been defined
	asBoolean = (asBoolean == undefined) ? false : asBoolean;
	
	// if input is a number then return
	if(typeof(input) == 'number'){
		return input;
	}

  var match = input.match(regex);
      
  // continue to process while matches can still be found
  while(match != undefined){
  	var expression = match[0];
    input = input.replace(expression, variableMap[expression]);
    match = input.match(regex);
  }

	// interpret the expression
	var result = processShuntingYardAlgorithm(input, precedenceMap);
	result = processReversePolishNotation(result);

	// return the result
	return (asBoolean) ? result != 0 : result;
}

/**
 * Constructs and returns a precedence map which maps an operator to
 * its precedence level.
 *
 * @returns {object} - map of operators to their precedence levels
 */
function constructPrecedenceMap(){
	var precedenceMap = {};
	
	precedenceMap[OR] = 10;
	precedenceMap[AND] = 9;
	precedenceMap[BIT_OR] = 8;
	precedenceMap[BIT_EXCL_OR] = 7;
	precedenceMap[BIT_AND] = 6;
	precedenceMap[EQUIVALENT] = 5;
	precedenceMap[NOT_EQUIVALENT] = 5;
	precedenceMap[LESS_THAN] = 4;
	precedenceMap[LESS_THAN_OR_EQUAL] = 4;
	precedenceMap[GREATER_THAN] = 4;
	precedenceMap[GREATER_THAN_OR_EQUAL] = 4;
	precedenceMap[RIGHT_SHIFT] = 3;
	precedenceMap[LEFT_SHIFT] = 3;
	precedenceMap[ADD] = 2;
	precedenceMap[SUBTRACT] = 2;
	precedenceMap[MULTIPLY] = 1;
	precedenceMap[DIVIDE] = 1;
	precedenceMap[MODULO] = 1;
	precedenceMap['('] = 0;
	precedenceMap[')'] = 0;

	return precedenceMap;
}

/**
 * Determines whether the specified input is an operator. Returns
 * true if it is an operator, otherwise returns false.
 * 
 * @param {string} input - string to check
 * @returns {boolean} - true if input is operator, otherwise false
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