'use strict';

/**
 * Unit tests to test that an error is thrown if the end of the file is reached
 * prematurely while parsing.
 */

var processTypes = ['automata', 'petrinet'];

describe('Grammar to AST EOF Tests', function(){
	
	for(var i = 0; i < processTypes.length; i++){
		var processType = processTypes[i];

		it('[' + processType + ' test] - Should throw an EOF error (1)', function(){
			try{
				var processes = constructProcesses(processType);
				assert.fail('Received an AST', 'Expected an error');
			}catch(error){
				// expecting an error therefore test has passed
			}
		});

		it('[' + processType + ' test] - Should throw an EOF error (2)', function(){
			try{
				var processes = constructProcesses(processType + 'A');
				assert.fail('Received an AST', 'Expected an error');
			}catch(error){
				// expecting an error therefore test has passed
			}
		});

		it('[' + processType + ' test] - Should throw an EOF error (3)', function(){
			try{
				var processes = constructProcesses(processType + 'A = ');
				assert.fail('Received an AST', 'Expected an error');
			}catch(error){
				// expecting an error therefore test has passed
			}
		});

		it('[' + processType + ' test] - Should throw an EOF error (4)', function(){
			try{
				var processes = constructProcesses(processType + 'A = (');
				assert.fail('Received an AST', 'Expected an error');
			}catch(error){
				// expecting an error therefore test has passed
			}
		});

		it('[' + processType + ' test] - Should throw an EOF error (5)', function(){
			try{
				var processes = constructProcesses(processType + 'A = (a');
				assert.fail('Received an AST', 'Expected an error');
			}catch(error){
				// expecting an error therefore test has passed
			}
		});

		it('[' + processType + ' test] - Should throw an EOF error (6)', function(){
			try{
				var processes = constructProcesses(processType + 'A = (a ->');
				assert.fail('Received an AST', 'Expected an error');
			}catch(error){
				// expecting an error therefore test has passed
			}
		});

		it('[' + processType + ' test] - Should throw an EOF error (7)', function(){
			try{
				var processes = constructProcesses(processType + 'A = (a -> STOP');
				assert.fail('Received an AST', 'Expected an error');
			}catch(error){
				// expecting an error therefore test has passed
			}
		});

		it('[' + processType + ' test] - Should throw an EOF error (8)', function(){
			try{
				var processes = constructProcesses(processType + 'A = (a -> STOP)');
				assert.fail('Received an AST', 'Expected an error');
			}catch(error){
				// expecting an error therefore test has passed
			}
		});
	}

	it('Empty file should not throw an EOF error', function(){
		try{
			var processes = constructProcesses('');
			expect(processes).to.eql([]);
		}catch(error){
			assert.fail('Received an error', 'Expected an empty processes array');
		}
	});

	it('File containing only a comment should not throw an EOF error', function(){
		try{
			var processes = constructProcesses('// test');
			expect(processes).to.eql([]);
		}catch(error){
			assert.fail('Received an error', 'Expected an empty processes array');
		}
	});

	it('File containing multiple comments should not throw an EOF error', function(){
		try{
			var processes = constructProcesses('// test1\n /*TEST2*/\n// test3');
			expect(processes).to.eql([]);
		}catch(error){
			assert.fail('Received an error', 'Expected an empty processes array');
		}
	});
});