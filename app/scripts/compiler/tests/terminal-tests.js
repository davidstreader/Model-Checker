'use strict';

/**
 * Tests to ensure that parsing singular terminals produces the correct abstract
 * syntax trees.
 */
function runTerminalTests(){

// fields
var assert = chai.assert;
var processTypes = ['automata', 'petrinet'];

/**
 * Runs the lexer over the specified code and parses the result of the lexer 
 * into an abstract syntax tree. Only returns the processes parsed as no
 * constants or variables are expected.
 */
function constructProcesses(code){
	var tokens = lexer.parse(code);
	return parse(tokens).processes;
}

/**
 * Test that the parsing of a stop terminal produces the correct abstract syntax tree.
 */
describe('Grammar to AST Stop Terminal Tests', function(){

	/**
	 * Run tests for all process types
	 */
	for(var i = 0; i < processTypes.length; i++){
		var processType = processTypes[i];
		var processes;

		/**
		 * Tests that parsing a singular stop terminal within parentheses produces the correct abstract
		 * syntax tree.
		 */
		it('[' + processType + ' test] - Should parse a process containing a single stop terminal within parentheses successfully', function(){
			processes = constructProcesses(processType + ' A = (STOP).');
			expect(processes[0]).to.have.property('process');
			expect(processes[0].process).to.have.property('type', 'terminal');
			expect(processes[0].process).to.have.property('terminal', 'STOP');
		});

		/**
		 * Tests that parsing a singular stop terminal without parentheses produces the correct abstract
		 * syntax tree.
		 */
		it('[' + processType + ' test] - Should parse a process containing a single stop terminal without parentheses successfully', function(){
			processes = constructProcesses(processType + ' A = STOP.');
			expect(processes[0]).to.have.property('process');
			expect(processes[0].process).to.have.property('type', 'terminal');
			expect(processes[0].process).to.have.property('terminal', 'STOP');
		});

		/**
		 * Tests that parsing a STOP terminal with or without parentheses produces the same
		 * abstract syntax tree
		 */
		it('[' + processType + ' test] - A single stop terminal with or without parentheses should construct an equivalent abstract syntax tree', function(){
			var processes1 = constructProcesses(processType + ' A = (STOP).');
			var processes2 = constructProcesses(processType + ' A = STOP.');

			expect(processes1).to.eql(processes2);
		});

		/**
		 * Runs this test after each of the other tests in this block has run to
		 * ensure that processes array has been constructed correctly by the parser.
		 */
		after(function(){
			expect(processes).to.have.lengthOf(1);
			expect(processes[0]).to.have.property('type', 'process');
			expect(processes[0]).to.have.property('processType', processType);
			expect(processes[0]).to.have.property('ident', 'A');
			expect(processes[0]).to.have.property('local');
			expect(processes[0].local).to.eql([]);
		});
	}
});

/**
 * Test that the parsing of an error terminal produces the correct abstract syntax tree.
 */
describe('Grammar to AST Error Terminal Tests', function(){
	
	/**
	 * Run tests for all process types
	 */
	for(var i = 0; i < processTypes.length; i++){
		var processType = processTypes[i];
		var processes;

		/**
		 * Tests that parsing a singular error terminal within parentheses produces the correct abstract
		 * syntax tree.
		 */
		it('[' + processType + ' test] - Should parse a process containing a single error terminal within parentheses successfully', function(){
			processes = constructProcesses(processType + ' A = (ERROR).');
			expect(processes[0]).to.have.property('process');
			expect(processes[0].process).to.have.property('type', 'terminal');
			expect(processes[0].process).to.have.property('terminal', 'ERROR');
		});

		/**
		 * Tests that parsing a singular error terminal without parentheses produces the correct abstract
		 * syntax tree.
		 */
		it('[' + processType + ' test] - Should parse a process containing a single error terminal without parentheses successfully', function(){
			processes = constructProcesses(processType + ' A = ERROR.');
			expect(processes[0]).to.have.property('process');
			expect(processes[0].process).to.have.property('type', 'terminal');
			expect(processes[0].process).to.have.property('terminal', 'ERROR');
		});

		/**
		 * Tests that parsing a STOP terminal with or without parentheses produces the same
		 * abstract syntax tree
		 */
		it('[' + processType + ' test] - A single error terminal with or without parentheses should construct an equivalent abstract syntax tree', function(){
			var processes1 = constructProcesses(processType + ' A = (ERROR).');
			var processes2 = constructProcesses(processType + ' A = ERROR.');

			expect(processes1).to.eql(processes2);
		});

		/**
		 * Runs this test after each of the other tests in this block has run to
		 * ensure that processes array has been constructed correctly by the parser.
		 */
		after(function(){
			expect(processes).to.have.lengthOf(1);
			expect(processes[0]).to.have.property('type', 'process');
			expect(processes[0]).to.have.property('processType', processType);
			expect(processes[0]).to.have.property('ident', 'A');
			expect(processes[0]).to.have.property('local');
			expect(processes[0].local).to.eql([]);
		});
	}
});

}

// run the test suite
runTerminalTests();