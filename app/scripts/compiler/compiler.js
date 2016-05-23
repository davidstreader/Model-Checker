"use strict";

/**
 *
 */
function compile(code){
	try{
	// convert code into an array of tokens
	var tokens = lexer.parse(code); // lexer.parse function in 'lexer.js'
	//
	var ast = parse(tokens); // parse function in 'parser.js'
	//
	var processes = interpret(ast.processes, ast.variableMap);
	//
	var graphs = constructGraphs(processes);

	return graphs;
	}catch(error){
		error.type = 'error';
		return error;
	}
}