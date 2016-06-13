'use strict';

/**
 * Unit tests to ensure that comments are ignored when parsing.
 */

var processTypes = ['automata', 'petrinet'];

describe('Grammar to AST Single Lined Comments Tests', function(){
	
	for(var i = 0; i < processTypes.length; i++){
		var processType = processTypes[i];

		// the expected abstract syntax tree
		var expected;

		/**
		 * Generates an abstract syntax tree to compare unit tests to.
		 */
		beforeEach(function(){
			expected = constructProcesses(processType + ' A = (a -> STOP).');
		});

		it('[' + processType + ' test] - Should parse leading single lined comments successfully', function(){
			var received = constructProcesses('// test \n' + processType + ' A = (a -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse trailing single lined comments successfully (1)', function(){
			var received = constructProcesses(processType + ' A = (a -> STOP). // test');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse trailing single lined comments successfully (2)', function(){
			var received = constructProcesses(processType + ' A = (a -> STOP).\n// test');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse leading and trailing single lined comments successfully (1)', function(){
			var received = constructProcesses('// test \n' + processType + ' A = (a -> STOP). // test');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse leading and trailing single lined comments successfully (2)', function(){
			var received = constructProcesses('// test \n' + processType + ' A = (a -> STOP).\n// test');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal single lined comments successfully (1)', function(){
			var received = constructProcesses(processType + '//test\n A = (a -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal single lined comments successfully (2)', function(){
			var received = constructProcesses(processType + ' A // test\n = (a -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal single lined comments successfully (3)', function(){
			var received = constructProcesses(processType + ' A = // test\n (a -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal single lined comments successfully (4)', function(){
			var received = constructProcesses(processType + ' A = (a // test\n -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal single lined comments successfully (5)', function(){
			var received = constructProcesses(processType + ' A = (a -> // test\n STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal single lined comments successfully (6)', function(){
			var received = constructProcesses(processType + ' A = (a -> STOP // test\n).');
			expect(expected).to.eql(received);
		});
	}
});

describe('Grammar to AST Multi Lined Comments Tests', function(){
	
	for(var i = 0; i < processTypes.length; i++){
		var processType = processTypes[i];

		// the expected abstract syntax tree
		var expected;

		/**
		 * Generates an abstract syntax tree to compare unit tests to.
		 */
		beforeEach(function(){
			expected = constructProcesses(processType + ' A = (a -> STOP).');
		});

		it('[' + processType + ' test] - Should parse leading multi lined comments successfully', function(){
			var received = constructProcesses('/*\ntest\n*/' + processType + ' A = (a -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse trailing single multi comments successfully', function(){
			var received = constructProcesses(processType + ' A = (a -> STOP). /*\ntest\n*/');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse leading and trailing multi lined comments successfully', function(){
			var received = constructProcesses('/*\ntest\n*/' + processType + ' A = (a -> STOP). /*\ntest\n*/');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal multi lined comments successfully (1)', function(){
			var received = constructProcesses(processType + '/*\ntest\n*/ A = (a -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal multi lined comments successfully (2)', function(){
			var received = constructProcesses(processType + ' A /*\ntest\n*/ = (a -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal multi lined comments successfully (3)', function(){
			var received = constructProcesses(processType + ' A = (/*\ntest\n*/a -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal multi lined comments successfully (4)', function(){
			var received = constructProcesses(processType + ' A = (a /*\ntest\n*/ -> STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal multi lined comments successfully (5)', function(){
			var received = constructProcesses(processType + ' A = (a -> /*\ntest\n*/ STOP).');
			expect(expected).to.eql(received);
		});

		it('[' + processType + ' test] - Should parse internal multi lined comments successfully (6)', function(){
			var received = constructProcesses(processType + ' A = (a -> STOP /*\ntest\n*/).');
			expect(expected).to.eql(received);
		});
	}
});