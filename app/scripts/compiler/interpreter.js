'use strict';

var processesMap;
var variableMap;
var nextProcessId;

function interpret(processes, variables){
	reset();
	variableMap = variables;

	for(var i = 0; i < processes.length; i++){
		var type = processes[i].processType;
		if(type === 'automata'){
			interpretAutomaton(processes[i], processesMap, variableMap, nextProcessId++);
		}
		else if(type === 'petrinet'){
			interpretPetriNet(processes[i], processesMap, variableMap, nextProcessId++);
		}
		else{
			// throw error
		}
	}

	return processesMap;

	function reset(){
		processesMap = {};
		variableMap = {};
		nextProcessId = 0;
	}
}