"use strict";

var CONST = 'const';
var SET = 'set';
var RANGE = 'range';

var IDENTIFIER = 'identifier';
var ACTION = 'action';
var INTEGER = 'integer';
var VARIABLE = 'action';
var OPERATOR = 'operator';
var COMMENT = 'comment';
var SYMBOL = 'symbol';
var OPERATOR = 'operator';
var OPERATION = 'operation';
var KEYWORD = 'keyword';
var TERMINAL = 'terminal';

var PROCESS = 'process';
var ACTION_LABEL = 'action-label';
var IF_STATEMENT = 'if-statement';
var FUNCTION = 'function';
var CHOICE = 'choice';
var SEQUENCE = 'sequence';
var IF = 'if';
var ELSE = 'else';

var ASSIGN = '=';
var DOT = '.';
var COMMA = ',';
var COLON = ':';
var BAR = '|';

var LEFT_PAREN = '(';
var RIGHT_PAREN = ')';
var LEFT_BRACE = '{';
var RIGHT_BRACE = '}';
var LEFT_SQ_BRACKET = '[';
var RIGHT_SQ_BRACKET = ']';
var RANGE_SEP = '..';
var TRANSITION = '->';

/**
 *
 */
function compile(code){
	try{
	// convert code into an array of tokens
	var tokens = lexer.parse(code); // lexer.parse function in 'lexer.js'
	//
	var output = parse(tokens); // parse function in 'parser.js'
	//
	var results = interpret(output.processes, output.variableMap);

	return results;
	}catch(error){
		error.type = 'error';
		return error;
	}
}