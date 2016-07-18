'use strict';

describe('Grammar to AST Hiding Tests', function(){
	
	for(var i = 0; i < PROCESS_TYPES.length; i++){
		var processType = PROCESS_TYPES[i];
		var processes;
		
		it('[' + processType + '-test] - Should parse \'\\{a}\' correctly', function(){
			processes = constructProcesses(processType + ' A = (STOP)\\{a}.');
			testHidingNode(processes[0].hiding, 'includes', ['a']);
		});

		it('[' + processType + '-test] - Should parse \'\\{a, b, c}\' correctly', function(){
			processes = constructProcesses(processType + ' A = (STOP)\\{a, b, c}.');
			testHidingNode(processes[0].hiding, 'includes', ['a', 'b', 'c']);
		});

		it('[' + processType + '-test] - Should parse \'\\{[0..2]}\' correctly', function(){
			processes = constructProcesses(processType + ' A = (STOP)\\{[0], [1], [2]}.');
			testHidingNode(processes[0].hiding, 'includes', ['[0]', '[1]', '[2]']);
		});

		it('[' + processType + '-test] - Should parse \'\\{a[0..2]}\' correctly', function(){
			processes = constructProcesses(processType + ' A = (STOP)\\{a[0], a[1], a[2]}.');
			testHidingNode(processes[0].hiding, 'includes', ['a[0]', 'a[1]', 'a[2]']);
		});

		it('[' + processType + '-test] - Should parse \'@{a}\' correctly', function(){
			processes = constructProcesses(processType + ' A = (STOP)@{a}.');
			testHidingNode(processes[0].hiding, 'excludes', ['a']);
		});

		it('[' + processType + '-test] - Should parse \'@{a, b, c}\' correctly', function(){
			processes = constructProcesses(processType + ' A = (STOP)@{a, b, c}.');
			testHidingNode(processes[0].hiding, 'excludes', ['a', 'b', 'c']);
		});

		it('[' + processType + '-test] - Should parse \'@{[0..2]}\' correctly', function(){
			processes = constructProcesses(processType + ' A = (STOP)@{[0], [1], [2]}.');
			testHidingNode(processes[0].hiding, 'excludes', ['[0]', '[1]', '[2]']);
		});

		it('[' + processType + '-test] - Should parse \'@{a[0..2]}\' correctly', function(){
			processes = constructProcesses(processType + ' A = (STOP)@{a[0], a[1], a[2]}.');
			testHidingNode(processes[0].hiding, 'excludes', ['a[0]', 'a[1]', 'a[2]']);
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