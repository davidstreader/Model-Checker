'use strict';

/**
 * Unit tests to ensure that parsing singular terminals produces the correct abstract
 * syntax trees.
 */

/**
 * Test that the parsing of a stop terminal produces the correct abstract syntax tree.
 */
describe('Grammar to AST Stop Terminal Tests', function(){

	/**
	 * Run tests for all process types
	 */
	for(var i = 0; i < PROCESS_TYPES.length; i++){
		var processType = PROCESS_TYPES[i];
		var processes;

		/**
		 * Tests that parsing a singular stop terminal within parentheses produces the correct abstract
		 * syntax tree.
		 */
		it('[' + processType + ' test] - Should parse a process containing a single stop terminal within parentheses successfully', function(){
			processes = constructProcesses(processType + ' A = (STOP).');
			testTerminalNode(processes[0].process, 'STOP');
		});

		/**
		 * Tests that parsing a singular stop terminal without parentheses produces the correct abstract
		 * syntax tree.
		 */
		it('[' + processType + ' test] - Should parse a process containing a single stop terminal without parentheses successfully', function(){
			processes = constructProcesses(processType + ' A = STOP.');
			testTerminalNode(processes[0].process, 'STOP');
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
			testProcessNode(processes[0], processType, 'A', false);
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
	for(var i = 0; i < PROCESS_TYPES.length; i++){
		var processType = PROCESS_TYPES[i];
		var processes;

		/**
		 * Tests that parsing a singular error terminal within parentheses produces the correct abstract
		 * syntax tree.
		 */
		it('[' + processType + ' test] - Should parse a process containing a single error terminal within parentheses successfully', function(){
			processes = constructProcesses(processType + ' A = (ERROR).');
			testTerminalNode(processes[0].process, 'ERROR');
		});

		/**
		 * Tests that parsing a singular error terminal without parentheses produces the correct abstract
		 * syntax tree.
		 */
		it('[' + processType + ' test] - Should parse a process containing a single error terminal without parentheses successfully', function(){
			processes = constructProcesses(processType + ' A = ERROR.');
			testTerminalNode(processes[0].process, 'ERROR');
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
			testProcessNode(processes[0], processType, 'A', false);
		});
	}
});