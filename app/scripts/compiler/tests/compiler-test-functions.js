'use strict';

/**
 * These serve as helper function for compiler tests. They are used
 * to eliminate duplicating the same code that is used throughout
 * the tests.
 */

/**
 * PARSING HELPER FUNCTIONS
 *
 * The following functions are used to help test the parsing of the
 * process grammar.
 */

/**
 * Parses the specified code into an abstract syntax tree and
 * returns the processes that were parsed.
 */
function constructConstantsMap(code){
	var tokens = lexer.parse(code);
	return parse(tokens).constantsMap;
}

/**
 * Parses the specified code into an abstract syntax tree and
 * returns the constants that were parsed.
 */
function constructProcesses(code){
	var tokens = lexer.parse(code);
	return parse(tokens).processes;
}

/**
 * The following functions test explicitly if an ast node was constructed
 * correctly.
 */

/**
 * Tests that the specified const node was constructed correctly by
 * the parser.
 *
 * @param {astNode} node - the const node
 * @param {int} value - the expected value
 */
function testConstNode(node, value){
	expect(node).to.have.property('type', 'const');
	expect(node).to.have.property('value', value);
}

/**
 * Tests that the specified range node was constructed correctly by
 * the parser.
 *
 * @param {astNode} node - the range node
 * @param {int} start - the expected start value
 * @param {int} end - the expected end value
 */
function testRangeNode(node, start, end){
	expect(node).to.have.property('type', 'range');
	expect(node).to.have.property('start', start);
	expect(node).to.have.property('end', end);
}

/**
 * Tests that the specified set node was constructed correctly by
 * the parser.
 *
 * @param {astNode} node - the set node
 * @param {string[]} set - the expected set
 */
function testSetNode(node, set){
	expect(node).to.have.property('type', 'set');
	expect(node).to.have.property('set');
	// have to use deep equals to test the set as they are defined as arrays
	expect(node.set).to.eql(set);
}

/**
 * Tests that the specified action label node was constructed correctly
 * by the parser.
 *
 * @param {astNode} node - the action label node
 * @param {string} action - the expected action label
 */
function testActionLabelNode(node, action){
	expect(node).to.have.property('type', 'action-label');
	expect(node).to.habe.property('action', action);
}

/**
 * Tests that the specified terminal node was constructed correctly by
 * the parser.
 *
 * @param {astNode} node - the terminal node
 * @param {string} terminal - the expected terminal
 */
function testTerminalNode(node, terminal){
	expect(node).to.have.property('type', 'terminal');
	expect(node).to.have.property('terminal', terminal);
}

/**
 * Tests that the specified identifier node was constructed correctly by
 * the parser.
 *
 * @param {astNode} node - the identifier node
 * @param {string} ident - the expected identifier
 */
function testIdentifierNode(node, ident){
	expect(node).to.have.property('type', 'identifier');
	expect(node).to.have.property('ident', ident);
}

/** 
 * The following functions do not test explicitly that the ast nodes were
 * constructed correctly. This is because they have nested ast nodes within
 * them. They test the shallow values of the node and leave the deeper tests
 * to the unit tests calling them.
 */

/**
 * Tests that the shallow values in the specified process node were
 * constructed correctly.
 *
 * @param {astNode} node - the process node
 * @param {string} processType - the expected process type
 * @param {string} ident - the expected identifier
 * @param {boolean} hasLocalProcesses - 
 */
function testProcessNode(node, processType, ident, hasLocalProcesses){
	expect(node).to.have.property('type', 'process');
	expect(node).to.have.property('processType', processType);
	expect(node).to.have.property('ident', ident);
	expect(node).to.have.property('process');

	// if the process node has local processes they will be tested by the unit test
	if(!hasLocalProcesses){
		expect(node).to.have.property('local');
		expect(node.local).to.eql([]);
	}
}

/**
 * Tests that the shallow values in the specified if statment node were
 * constructed correctly.
 *
 * @param {astNode} node - the if statement node
 * @param {string} condition - the expected condition
 * @param (boolean) hasFalseBranch -
 */
function testIfStatementNode(node, condition, hasFalseBranch){
	expect(node).to.have.property('type', 'if-statement');
	expect(node).to.have.property('guard', condition);
	expect(node).to.have.property('trueBranch');
	
	// test for false branch based on the specified boolean
	if(hasFalseBranch){
		expect(node).to.have.property('falseBranch');
	}
	else{
		expect(node).to.not.have.property('falseBranch');
	}
}