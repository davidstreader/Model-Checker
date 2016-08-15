'use strict';

describe('Gramman to AST Process Definition Block Tests', function(){

	
	var sequences = [['a', 'b', 'c'], ['x', 'y', 'z'], ['i', 'j', 'k']];
	var identifiers = ['A', 'B', 'C'];


	for(var i = 0; i < PROCESS_TYPES.length; i++){
		var processType = PROCESS_TYPES[i];

		it('[' + processType + '-test] - Should parse a single process definition correctly', function(){
			var processes = constructProcesses(processType + '{ A = (a -> b -> c -> STOP). }');
			expect(processes).to.have.lengthOf(1);
			testProcessNode(processes[0], processType, identifiers[0], false);
			testSequenceNode(processes[0].process, sequences[0]);
		});

		it('[' + processType + '-test] - Should parse two process definitions correctly', function(){
			var processes = constructProcesses(processType + '{ A = (a -> b -> c -> STOP). B = (x -> y -> z -> STOP). }');
			expect(processes).to.have.lengthOf(2);
			for(var j = 0; j < 2; j++){
				testProcessNode(processes[j], processType, identifiers[j], false);
				testSequenceNode(processes[j].process, sequences[j]);
			}
		});

		it('[' + processType + '-test] - Should parse two process definitions correctly', function(){
			var processes = constructProcesses(processType + '{ A = (a -> b -> c -> STOP). B = (x -> y -> z -> STOP). C = (i -> j -> k -> STOP). }');
			expect(processes).to.have.lengthOf(3);
			for(var j = 0; j < 3; j++){
				testProcessNode(processes[j], processType, identifiers[j], false);
				testSequenceNode(processes[j].process, sequences[j]);
			}
		});

	}

});