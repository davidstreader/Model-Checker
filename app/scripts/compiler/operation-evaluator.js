'use strict';

var results;
var id;
var idents;

function evaluateOperations(operations, processesMap, variableMap){
	reset();

	for(var i = 0; i < operations.length; i++){
		var {process1, process2} = operations[i];
		var graph1 = interpretOneOff(generateProcessIdent(), process1, 'automata', processesMap, variableMap);
		var graph2 = interpretOneOff(generateProcessIdent(), process2, 'automata', processesMap, variableMap);

		var result = areBisimular([graph1, graph2], 'automata');
		if(operations[i].isNegated){
			result = !result;
		}

		process1 = reconstruct(process1);
		process2 = reconstruct(process2);

		results.push(process1 + ' ~ ' + process2 + ' = ' + result);
	}

	for(var i = 0; i < idents.length; i++){
		delete processesMap[idents[i]];
	}

	return results;

	function generateProcessIdent(){
		var ident = 'op' + id++;
		idents.push(ident);
		return ident;
	}

	function reset(){
		results = [];
		id = 0;
		idents = [];
	}
}