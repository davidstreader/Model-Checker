'use strict';

var processesMap;
var variableMap;

function interpret(processes, variables){
	reset();
	variableMap = variables;

	for(var i = 0; i < processes.length; i++){
		var type = processes[i].type;
		if(type === 'automata'){
			interpretAutomaton(processes[i], processesMap, variableMap);
		}
		else if(type === 'petrinet'){
			interpretPetriNet(processes[i], processesMap, variableMap);
		}
		else{
			// throw error
			if(i % 2 === 0){
				interpretAutomaton(processes[i], processesMap, variableMap);
			}
			else{
				interpretPetriNet(processes[i], processesMap, variableMap);
			}
		}
	}

	return processesMap;

	function reset(){
		processesMap = {};
		variableMap = {};
	}
}