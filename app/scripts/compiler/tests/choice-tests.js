var processTypes = ['automata', 'petrinet'];

describe('Grammar to AST Choice Tests', function(){

	for(var i = 0; i < processTypes.length; i++){
		var processType = processTypes[i];
		var processes;

		it('[' + processType + ' test] - Should parse A = STOP | STOP. correctly', function(){
			processes = constructProcesses(processType + ' A = STOP | STOP.');
			testChoiceNode(processes[0].process);
			testTerminalNode(processes[0].process.process1, 'STOP');
			testTerminalNode(processes[0].process.process2, 'STOP');
		});

		it('[' + processType + ' test] - Sould parse A = a -> STOP | STOP. correctly', function(){
			processes = constructProcesses(processType + ' A = a -> STOP | STOP.');
			testChoiceNode(processes[0].process);
			var baseProcess = testSequenceNode(processes[0].process.process1, ['a']);
			testTerminalNode(baseProcess, 'STOP');
			testTerminalNode(processes[0].process.process2, 'STOP');
		});

		it('[' + processType + ' test] - Should parse A = STOP | b -> STOP. correctly', function(){
			processes = constructProcesses(processType + ' A = STOP | b -> STOP.');
			testChoiceNode(processes[0].process);
			testTerminalNode(processes[0].process.process1, 'STOP');
			var baseProcess = testSequenceNode(processes[0].process.process2, ['b']);
			testTerminalNode(baseProcess, 'STOP');
		});

		it('[' + processType + ' test] - Should parse A = a -> STOP | b -> STOP. correctly', function(){
			processes = constructProcesses(processType + ' A = a -> STOP | b -> STOP.');
			testChoiceNode(processes[0].process);
			var baseProcess1 = testSequenceNode(processes[0].process.process1, ['a']);
			testTerminalNode(baseProcess1, 'STOP');
			var baseProcess2 = testSequenceNode(processes[0].process.process2, ['b']);
			testTerminalNode(baseProcess2, 'STOP');
		});

		it('[' + processType + ' test] - Should parse A = a -> b -> STOP | c -> STOP. correctly', function(){
			processes = constructProcesses(processType + ' A = a -> b -> STOP | c -> STOP.');
			testChoiceNode(processes[0].process);
			var baseProcess1 = testSequenceNode(processes[0].process.process1, ['a', 'b']);
			testTerminalNode(baseProcess1, 'STOP');
			var baseProcess2 = testSequenceNode(processes[0].process.process2, ['c']);
			testTerminalNode(baseProcess2, 'STOP');
		});

		it('[' + processType + ' test] - Should parse A = a -> STOP | b -> c -> STOP. correctly', function(){
			processes = constructProcesses(processType + ' A = a -> STOP | b -> c -> STOP.');
			testChoiceNode(processes[0].process);
			var baseProcess1 = testSequenceNode(processes[0].process.process1, ['a']);
			testTerminalNode(baseProcess1, 'STOP');
			var baseProcess2 = testSequenceNode(processes[0].process.process2, ['b', 'c']);
			testTerminalNode(baseProcess2, 'STOP');
		});

		it('[' + processType + ' test] - Should parse A = a -> b -> STOP | c -> d -> STOP. correctly', function(){
			processes = constructProcesses(processType + ' A = a -> b -> STOP | c -> d -> STOP.');
			testChoiceNode(processes[0].process);
			var baseProcess1 = testSequenceNode(processes[0].process.process1, ['a', 'b']);
			testTerminalNode(baseProcess1, 'STOP');
			var baseProcess2 = testSequenceNode(processes[0].process.process2, ['c', 'd']);
			testTerminalNode(baseProcess2, 'STOP');
		});
	}
});