'use strict';

describe('Grammar to AST Function Tests', function(){

		before(function(){
			baseProcess = constructProcesses('automata A = (a -> b -> c -> STOP).');
			baseProcess = baseProcess[0].process;
		});

	for(var i = 0; i < PROCESS_TYPES.length; i++){
		var processType = PROCESS_TYPES[i];
		var process;
		var baseProcess;

		it('[' + processType + ' test] - Should parse ' + processType + ' A = abs(a -> b -> c -> STOP). correctly', function(){
			process = constructProcesses(processType + ' A = abs(a -> b -> c -> STOP).');
			testFunctionNode(processes[0].process, 'abs');
			expect(baseProcess).to.eql(processes[0].process.process);
		});

		it('[' + processType + ' test] - Should parse ' + processType + ' A = simp(a -> b -> c -> STOP). correctly', function(){
			process = constructProcesses(processType + ' A = simp(a -> b -> c -> STOP).');
			testFunctionNode(processes[0].process, 'simp');
			expect(baseProcess).to.eql(processes[0].process.process);
		});

		it('[' + processType + ' test] - Should parse ' + processType + ' A = abs(simp(a -> b -> c -> STOP)). correctly', function(){
			process = constructProcesses(processType + ' A = abs(simp(a -> b -> c -> STOP)).');
			testFunctionNode(processes[0].process, 'abs');
			testFunctionNode(processes[0].process.process, 'simp');
			expect(baseProcess).to.eql(processes[0].process.process.process);
		});

		it('[' + processType + ' test] - Should parse ' + processType + ' A = simp(abs(a -> b -> c -> STOP)). correctly', function(){
			process = constructProcesses(processType + ' A = simp(abs(a -> b -> c -> STOP)).');
			testFunctionNode(processes[0].process, 'simp');
			testFunctionNode(processes[0].process.process, 'abs');
			expect(baseProcess).to.eql(processes[0].process.process.process);
		});

		it('[' + processType + ' test] - Should parse ' + processType + ' A = abs(abs(a -> b -> c -> STOP)). correctly', function(){
			process = constructProcesses(processType + ' A = abs(abs(a -> b -> c -> STOP)).');
			testFunctionNode(processes[0].process, 'abs');
			testFunctionNode(processes[0].process.process, 'abs');
			expect(baseProcess).to.eql(processes[0].process.process.process);
		});

		it('[' + processType + ' test] - Should parse ' + processType + ' A = simp(simp(a -> b -> c -> STOP)). correctly', function(){
			process = constructProcesses(processType + ' A = simp(simp(a -> b -> c -> STOP)).');
			testFunctionNode(processes[0].process, 'simp');
			testFunctionNode(processes[0].process.process, 'simp');
			expect(baseProcess).to.eql(processes[0].process.process.process);
		});
	}

});
