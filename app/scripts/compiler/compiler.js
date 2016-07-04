"use strict";

/**
 * The results from the last compilation.
 */
var lastTokens = [];
var lastAst = {};
var lastAnalysis = {};
var lastProcesses = [];
var lastGraphs = [];

/**
 * Runs the compilation process for the model checker. This process is broken
 * down into the following steps:
 *
 * 1. Lexing
 *		- Takes the specified code and constructs an array of tokens
 * 2. Parsing
 *		- Parses the tokens to form an abstract syntax tree
 * 3. Analysis
 *		- Determines what processes need to be re-interpreted and eliminates
 *		  redundant code
 * 4. Interpeting
 *		- Interprets the abstract syntax tree into data structures representing
 *		  the processes that are being modelled
 * 5. Graph Construction
 *		- Converts the data structures into a graph structure defined by dagre
 *		  which can be rendered by dagreD3
 *
 * @param {string} code - code to be processed
 * @throws {exception} - throws an exception specific to the stage where the error
 *						 took place
 */
function compile(code){
	try{
		// convert code into an array of tokens
		var tokens = lexer.parse(code); // lexer.parse function in 'lexer.js'
		
		// convert code into an abstract syntax tree of the defined processes  
		var ast = parse(tokens); // parse function in 'parser.js'
		
		// perform analysis to see which processes need to be re-interpreted
		var analysis = performAnalysis(ast.processes, lastAnalysis); // performAnalysis function in 'analyser.js'
		
		// convert the processes from the ast into their appropriate data structures
		var processes = interpret(ast.processes, ast.variableMap, analysis, lastProcesses); // interpret function in 'interpreter.js'
		
		// convert the process data structures into dagre graphs that can
		// be rendered by dagreD3
		var graphs = constructGraphs(processes, analysis, lastGraphs); // construct graph function in 'graphConstructor.js'

		// store results of this compilation
		lastTokens = tokens;
		lastAst = ast;
		lastAnalysis = analysis;
		lastProcesses = processes;
		lastGraphs = graphs;

		return graphs;
	}catch(error){
		error.type = 'error';
		return error;
	}
}